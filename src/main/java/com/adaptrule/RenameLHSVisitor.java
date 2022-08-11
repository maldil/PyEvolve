package com.adaptrule;

import com.matching.fgpdg.nodes.ast.LazyHole;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.*;
import org.python.antlr.ast.Module;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;
import org.python.core.PyObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RenameLHSVisitor extends Visitor {
    Map<PythonTree,List<PythonTree>> codeAndPara;
    Map<PythonTree,PythonTree> holeAndCode = new HashMap<>();
    Map<PythonTree, Hole> nameToHole = new HashMap<>();
    int largestHoleID=0;
    @Override
    public Object visitAssign(Assign node)  throws Exception {
        for (Map.Entry<PythonTree, List<PythonTree>> entry : codeAndPara.entrySet()) {
            PythonTree child = entry.getKey();
            List<PythonTree> replacement = entry.getValue();
            if (node.getInternalValue().equals(child)){
                node.setValue(replacement.get(0));
            }else if (node.getInternalTargets().contains(child)){
                int childIndex= node.getInternalTargets().indexOf(child);
                node.getInternalTargets().remove(child);
                node.getInternalTargets().add(childIndex, (expr) replacement.get(0));
            }
            if(node.getChildren().contains(child)){
                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                int childIndex= node.getChildren().indexOf(child);
                node.getChildren().remove(child);
                node.getChildren().add(childIndex,replacement.get(0));
            }
        }
        return super.visitAssign(node);
    }

    @Override
    public Object visitFor(For node)  throws Exception {
        for (Map.Entry<PythonTree, List<PythonTree>> entry : codeAndPara.entrySet()) {
            PythonTree child = entry.getKey();
            List<PythonTree> replacement = entry.getValue();
            if (node.getInternalIter().equals(child)){
                node.setIter(replacement.get(0));
            }else if (node.getInternalTarget().equals(child)){
                node.setTarget(replacement.get(0));
            }
            else if (node.getInternalBody().contains(child)){
                int childIndex= node.getInternalBody().indexOf(child);
                node.getInternalBody().remove(child);
                node.getInternalBody().add(childIndex, (stmt) replacement.get(0));
            }
            if(node.getChildren().contains(child)){
                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                int childIndex= node.getChildren().indexOf(child);
                node.getChildren().remove(child);
                node.getChildren().add(childIndex,replacement.get(0));
            }
        }
        return super.visitFor(node);
    }

    @Override
    public Object visitReturn(Return node)  throws Exception {
        for (Map.Entry<PythonTree, List<PythonTree>> entry : codeAndPara.entrySet()) {
            PythonTree child = entry.getKey();
            List<PythonTree> replacement = entry.getValue();
            if (node.getInternalValue().equals(child)){
                node.setValue(replacement.get(0));
            }
            if(node.getChildren().contains(child)){
                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                int childIndex= node.getChildren().indexOf(child);
                node.getChildren().remove(child);
                node.getChildren().add(childIndex,replacement.get(0));
            }
        }
        return super.visitReturn(node);

    }

    @Override
    public Object visitModule(Module node)  throws Exception {
        for (Map.Entry<PythonTree, List<PythonTree>> entry : codeAndPara.entrySet()) {
            PythonTree child = entry.getKey();
            List<PythonTree> replacement = entry.getValue();
            if(node.getInternalBody().contains(child)){
                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                int childIndex= node.getInternalBody().indexOf(child);
                node.getInternalBody().remove(child);
                node.getInternalBody().add(childIndex, (stmt) replacement.get(0));
            }
            if(node.getChildren().contains(child)){
                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));

                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                int childIndex= node.getChildren().indexOf(child);
                node.getChildren().remove(child);
                node.getChildren().add(childIndex,replacement.get(0));
            }
        }
        return super.visitModule(node);
    }

    @Override
    public Object visitFunctionDef(FunctionDef node)  throws Exception {
        for (Map.Entry<PythonTree, List<PythonTree>> entry : codeAndPara.entrySet()) {
            PythonTree child = entry.getKey();
            List<PythonTree> replacement = entry.getValue();
            if(node.getInternalBody().contains(child)){
                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                int childIndex= node.getInternalBody().indexOf(child);
                node.getInternalBody().remove(child);
                if (replacement.get(0) instanceof stmt){
                    node.getInternalBody().add(childIndex, (stmt) replacement.get(0));
                }
                else{
                    Expr e = new Expr();
                    e.setValue(replacement.get(0));
                    e.setParent(node);
                    node.getInternalBody().add(childIndex, e);
                }
            }
            if(node.getChildren().contains(child)){
                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                HoleSearcher searcher = new HoleSearcher();
                searcher.visit(replacement.get(0));
                for (expr hole : searcher.getHoles()) {
                    if (holeAndCode.containsKey(hole)){
                        nameToHole.put(holeAndCode.get(hole), (Hole) hole);
                    }
                }
                int childIndex= node.getChildren().indexOf(child);
                node.getChildren().remove(child);
                node.getChildren().add(childIndex,replacement.get(0));
            }
        }
        return super.visitFunctionDef(node);
    }



    public Map<PythonTree, Hole> getNameToHole() {
        return nameToHole;
    }

    public Map<PythonTree,List<PythonTree>> getRenameChild() {
        return codeAndPara;
    }

    public void setRenameChild(Map<PythonTree,List<PythonTree>> renameChild) {
        this.codeAndPara = renameChild;
        for (Map.Entry<PythonTree, List<PythonTree>> entry : renameChild.entrySet()) {
            if(!codeAndPara.containsKey(entry.getValue().get(0))){
                holeAndCode.put(entry.getValue().get(0),entry.getKey());
            }
        }
    }

    public int getLargestHoleID() {
        return largestHoleID;
    }
}
