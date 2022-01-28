package com.prune;

import com.google.common.collect.Iterators;
import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.cast.python.client.PythonAnalysisEngine;
import com.ibm.wala.cast.python.ipa.callgraph.PythonSSAPropagationCallGraphBuilder;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.*;
import com.ibm.wala.util.CancelException;
import org.python.core.PyObject;


import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

public class MainPruneEngine {
    public void getAllDataDependentNodes(PyObject node, String filePath) {
        WALAEngine engine = new WALAEngine();
        IRFactory<IMethod> irFac = new AstIRFactory<IMethod>();
        PythonAnalysisEngine<?> E = null;
        try {
            E = engine.makeEngine(filePath);
            PythonSSAPropagationCallGraphBuilder B = E.defaultCallGraphBuilder();
            IClassHierarchy classHierarchy = B.getClassHierarchy();
            for (IClass iClass : classHierarchy) {
                if (iClass != null && iClass.getDeclaredMethods() != null) {
                    for (IMethod method : iClass.getDeclaredMethods()) {
                        if (method instanceof AstMethod) {
                            IR ir = irFac.makeIR(method, Everywhere.EVERYWHERE, SSAOptions.defaultOptions());
                            SymbolTable symbolTable = ir.getSymbolTable();
                            DefUse dUse = new DefUse(ir);
                            AstMethod.DebuggingInformation debuggingInfo = ((AstMethod) method).debugInfo();
                            for (SSAInstruction instruction : ir.getInstructions()) {
                                if (instruction != null && instruction.iIndex() >= 0) {
                                    for (int j = 0; j < instruction.getNumberOfUses(); j++) {
                                        int use = instruction.getUse(j);
                                        String[] names = ir.getLocalNames(instruction.iIndex(), use);
                                        Object constantValue = "";
                                        if (symbolTable.isConstant(use)) {
                                            constantValue = symbolTable.getConstantValue(use);
//                                            System.out.println("ConstantValue " + symbolTable.getConstantValue(use));
                                        }
                                        if (instruction.getDef() != -1) {
                                            Iterator<SSAInstruction> uses = dUse.getUses(instruction.getDef());
//                                            System.out.println("Size : "+Iterators.size(uses));
                                            System.out.println("instruction : "+instruction+" Names " + Arrays.toString(names)+" constantValue :"+constantValue+" InstructionPosition : " +debuggingInfo.getInstructionPosition(instruction.iIndex()));

                                            while (uses.hasNext()) {
                                                SSAInstruction next = uses.next();
                                                if (next.iIndex() != -1) {
                                                    int usenext = next.getUse(j);
                                                    String[] namesnext = ir.getLocalNames(next.iIndex(), usenext);
                                                    System.out.println("instruction : "+instruction+" Names " + Arrays.toString(names)+" constantValue :"+constantValue+" InstructionPosition : " +debuggingInfo.getInstructionPosition(instruction.iIndex()));
                                                    System.out.println("Usageinstruction : "+ next+" Names :"+Arrays.toString(namesnext)+debuggingInfo.getInstructionPosition(next.iIndex()));
                                                }
                                                else{
                                                    System.out.println("next.iIndex() is -1");
                                                }
                                            }
                                        } else {
                                            System.out.println("ssaIns.getDef() is -1");
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        } catch (ClassHierarchyException | IOException | CancelException e) {
            e.printStackTrace();
        }
    }
}
