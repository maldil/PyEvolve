package com.matching.fgpdg;

import com.ibm.icu.impl.Assert;
import com.matching.ConcreatePythonParser;
import com.utils.DotGraph;
import com.utils.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.python.antlr.Visitor;
import org.python.antlr.ast.*;
import org.python.antlr.ast.Module;
import org.python.antlr.base.stmt;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

class MatchedNodeTest {

    @Test
    void testSubGraphs1() {
        String filename="testm";
        String patternname = "pattern1";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(7,graphs.get(0).getPDGNodes().size());
    }

    @Test
    void testSubGraphs2() {
        String filename="testm1";
        String patternname = "pattern1";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(7,graphs.get(0).getPDGNodes().size());
    }

    @Test
    void testSubGraphs3() {
        String filename="testm2";
        String patternname = "pattern";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(7,graphs.get(0).getPDGNodes().size());
    }

    @Test
    void testSubGraphs4() {
        String filename="test1";
        String patternname = "pattern";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(12,graphs.get(0).getPDGNodes().size());
    }


    @Test
    void testSubGraphs5() {
        String filename="testm2";
        String patternname = "pattern";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(7,graphs.get(0).getPDGNodes().size());
    }

    private List<MatchedNode> getMatchedNodes(String filename, String patternname, List<MatchedNode> graphs) {
        Module codeModule = getPythonModule("author/project/"+filename+".py");
        Module patternModule = getPythonModule("author/project/"+patternname+".py");
        FunctionDef func=null;
        for (org.python.antlr.base.stmt stmt : codeModule.getInternalBody()) {
            if (stmt instanceof FunctionDef){
                func= (FunctionDef) stmt;
            }
        }
        PDGBuildingContext fcontext = null;
        try {
            fcontext = new PDGBuildingContext(codeModule.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/"+filename+".py");
            PDGGraph fpdg = new PDGGraph(func,fcontext);
            PDGBuildingContext mcontext = new PDGBuildingContext(patternModule.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()),"author/project/"+patternname+".py");
            PDGGraph mpdg = new PDGGraph(patternModule,mcontext);

            MatchPDG match = new MatchPDG();
            graphs=match.getSubGraphs(mpdg,fpdg );

            match.drawMatchedGraphs(fpdg,graphs,"OUTPUT/matches/"+filename+".dot");
            Utils.markNodesInCode("src/test/resources/author/project/"+filename+".py",graphs,"OUTPUT/matches/"+filename+".html");;
        } catch (IOException e) {
            System.err.println("Type information can not be performed");
            e.printStackTrace();
        }
        return graphs;
    }


    private Module getPythonModule(String fileName){
        ConcreatePythonParser parser = new ConcreatePythonParser();
        return parser.parse(fileName);
    }

    class PyASTVisitor extends Visitor {
        private int classDef= 0;
        private int funcDef= 0;
        @Override
        public Object visitClassDef(ClassDef node) throws Exception {
            classDef+=1;
            return super.visitClassDef(node);
        }
        @Override
        public Object visitFunctionDef(FunctionDef node) throws Exception {
            classDef+=1;
            return super.visitFunctionDef(node);
        }
    }
}