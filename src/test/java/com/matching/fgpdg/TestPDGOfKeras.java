package com.matching.fgpdg;

import com.matching.ConcreatePythonParser;
import com.utils.DotGraph;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.python.antlr.Visitor;
import org.python.antlr.ast.*;
import org.python.antlr.ast.Module;
import org.python.antlr.base.stmt;
import org.python.modules.thread._thread$exit_exposer;

import java.io.File;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.System.exit;

public class TestPDGOfKeras {
    public static ArrayList<FunctionDef>  getAllFunctions(Module  ast){
//        ArrayList<FunctionDef> funcDefs = new ArrayList<>();
//        for (stmt stmt : ast.getInternalBody()) {
//            if (stmt instanceof FunctionDef){
//                funcDefs.add((FunctionDef) stmt);
//            }
//            else if (stmt instanceof ClassDef){
//                for (org.python.antlr.base.stmt stmt1 : ((ClassDef) stmt).getInternalBody()) {
//                    if (stmt1 instanceof FunctionDef){
//                        funcDefs.add((FunctionDef) stmt1);
//                    }
//                }
//            }
//        }
        PyFuncDefVisitor fu = new PyFuncDefVisitor();
        try {
            fu.visit(ast);
            return fu.funcDefs;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

    }

    static class PyFuncDefVisitor extends Visitor {
        ArrayList<FunctionDef> funcDefs = new ArrayList<>();
        @Override
        public Object visitFunctionDef(FunctionDef node) throws Exception {
            funcDefs.add(node);
            return super.visitFunctionDef (node);
        }
    }

    @Test
    void testKerasPDG() {
        String projectPath = Configurations.PROJECT_REPOSITORY+"keras-team/keras/";
        File dir = new File(projectPath);
        ArrayList<File> files = getPythonFiles(Objects.requireNonNull(dir.listFiles()));

        for (File file : files) {
            System.out.println(file.getAbsolutePath());
            ConcreatePythonParser parser = new ConcreatePythonParser();
            Module parse = parser.parse(file.getAbsolutePath());
            List<stmt> collect = parse.getInternalBody().stream().filter(x -> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList());
            ArrayList<FunctionDef> functions = getAllFunctions(parse);
            for (FunctionDef function : functions) {
                try {
                    String relative = new File(Configurations.PROJECT_REPOSITORY).toURI().relativize(new File(file.getAbsolutePath()).toURI()).getPath();
                    PDGBuildingContext context =new PDGBuildingContext(collect, relative);
                    System.out.println(function.getInternalName());
                    PDGGraph pdg = new PDGGraph(function,context);


//                    DotGraph dg = new DotGraph(pdg);
//                    String dirPath = "./OUTPUT/";
//                    dg.toDotFile(new File(dirPath  +"file___"+".dot"));
//                    Assertions.assertEquals (pdg.parameters.length,2);
//                    Assertions.assertEquals (pdg.getNodes().size(),48);
//                    Assertions.assertEquals (pdg.statementNodes.size() ,19);
//                    Assertions.assertEquals (pdg.dataSources.size() ,10);

                } catch (IOException e) {
                    System.out.println("Type File is Not available");
                }


            }
        }

//        ConcreatePythonParser parser = new ConcreatePythonParser();
//        Module parse = parser.parse("author/project/test1.py");
//        FunctionDef func = (FunctionDef) parse.getInternalBody().get(1);
//        PDGBuildingContext context = null;
//        try {
//            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
//                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test1.py");
//            PDGGraph pdg = new PDGGraph(func,context);
//            DotGraph dg = new DotGraph(pdg);
//            String dirPath = "./OUTPUT/";
//            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
//            Assertions.assertEquals (pdg.parameters.length,2);
//            Assertions.assertEquals (pdg.getNodes().size(),48);
//            Assertions.assertEquals (pdg.statementNodes.size() ,19);
//            Assertions.assertEquals (pdg.dataSources.size() ,10);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


    }

    public static ArrayList<File> getPythonFiles(File[] files) {
        ArrayList<File> pythonFiles = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                if (!file.getName().startsWith(".")) {
                    pythonFiles.addAll(getPythonFiles(Objects.requireNonNull(file.listFiles()))); // Calls same method again.
                }
            } else {
                if (file.getName().endsWith(".py")) {
                    pythonFiles.add(file);
                }
            }
        }
        return pythonFiles;
    }
}
