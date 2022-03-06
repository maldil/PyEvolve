package com.matching.fgpdg;

import com.matching.ConcreatePythonParser;
import com.matching.fgpdg.nodes.PDGNode;
import com.utils.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.python.antlr.Visitor;
import org.python.antlr.ast.*;
import org.python.antlr.ast.Module;

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
        graphs.get(0).isAllMatchedGraph();
        Assertions.assertTrue(graphs.get(0).isAllMatchedGraph());
        Assertions.assertEquals(7,graphs.get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs2() {
        String filename="testm1";
        String patternname = "pattern1";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertFalse(graphs.get(0).isAllMatchedGraph());
        Assertions.assertEquals(1,graphs.get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs3() {
        String filename="testm2";
        String patternname = "pattern";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertTrue(graphs.get(0).isAllMatchedGraph());
        Assertions.assertEquals(16,graphs.get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs4() {
        String filename="test1";
        String patternname = "pattern";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertTrue(graphs.get(0).isAllMatchedGraph());
        Assertions.assertEquals(16,graphs.get(0).getCodePDGNodes().size());
    }


    @Test
    void testSubGraphs5() {
        String filename="testm2";
        String patternname = "pattern";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertTrue(graphs.get(0).isAllMatchedGraph());
        Assertions.assertEquals(16,graphs.get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs6() {
        String filename="testm3";
        String patternname = "pattern";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        for (MatchedNode graph : graphs) {
            if (graph.getAllMatchedNodes().size()==1)
                Assertions.assertFalse(graph.isAllMatchedGraph());
            else
                Assertions.assertTrue(graph.isAllMatchedGraph());

        }
        int nodes = Math.max(graphs.get(0).getCodePDGNodes().size(), graphs.get(1).getCodePDGNodes().size());
        Assertions.assertEquals(16,nodes);
    }

    @Test
    void testSubGraphs7() {
        String filename="testm4";
        String patternname = "pattern";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        for (MatchedNode graph : graphs) {
            if (graph.getAllMatchedNodes().size()==1)
                Assertions.assertFalse(graph.isAllMatchedGraph());
            else
                Assertions.assertTrue(graph.isAllMatchedGraph());
        }
        int nodes = Math.max(graphs.get(0).getCodePDGNodes().size(), graphs.get(1).getCodePDGNodes().size());
        Assertions.assertEquals(16,nodes);
    }

    @Test
    void testSubGraphs8() {
        String filename="testm5";
        String patternname = "pattern";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        for (MatchedNode graph : graphs) {
            if (graph.getAllMatchedNodes().size()==1)
                Assertions.assertFalse(graph.isAllMatchedGraph());
            else
                Assertions.assertTrue(graph.isAllMatchedGraph());
        }
        int nodes = Math.max(graphs.get(0).getCodePDGNodes().size(), graphs.get(1).getCodePDGNodes().size());
        Assertions.assertEquals(16,nodes);
    }

    @Test
    void testSubGraphs9() {
        String filename="testm6";
        String patternname = "pattern";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertTrue(graphs.get(0).isAllMatchedGraph());
        for (MatchedNode graph : graphs) {
            if (graph.getAllMatchedNodes().size()==1)
                Assertions.assertFalse(graph.isAllMatchedGraph());
            else
                Assertions.assertTrue(graph.isAllMatchedGraph());
        }
        Assertions.assertEquals(16,graphs.get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs10() {
        String filename="testm7";
        String patternname = "pattern2";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        for (MatchedNode graph : graphs) {
            if (graph.getAllMatchedNodes().size()==1)
                Assertions.assertFalse(graph.isAllMatchedGraph());
            else
                Assertions.assertTrue(graph.isAllMatchedGraph());
        }
        int nodes = Math.max(graphs.get(0).getCodePDGNodes().size(), graphs.get(1).getCodePDGNodes().size());
        Assertions.assertEquals(9,nodes);
    }

    @Test
    void testSubGraphs11() {
        String filename="testm8";
        String patternname = "pattern3";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertTrue(graphs.get(0) .isAllMatchedGraph());
        Assertions.assertEquals(5,graphs.get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs12() {
        String filename="testm9";
        String patternname = "pattern2";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        int nodes = Math.max(graphs.get(0).getCodePDGNodes().size(), graphs.get(1).getCodePDGNodes().size());
        Assertions.assertEquals(9,nodes);
    }

    @Test
    void testSubGraphs13() {
        String filename="testm10";
        String patternname = "pattern3";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        int nodes = Math.max(graphs.get(0).getCodePDGNodes().size(), graphs.get(1).getCodePDGNodes().size());
        Assertions.assertEquals(9,nodes);
    }

    @Test
    void testSubGraphs14() {
        String filename="testm11";
        String patternname = "pattern4";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
//        int nodes = Math.max(graphs.get(0).getCodePDGNodes().size(), graphs.get(1).getCodePDGNodes().size());
        Assertions.assertTrue(graphs.get(0) .isAllMatchedGraph());
        Assertions.assertEquals(7,graphs.get(0).getCodePDGNodes().size());
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
            fpdg.getNodes().forEach(x-> System.out.println(x.getId()));
            PDGBuildingContext mcontext = new PDGBuildingContext(patternModule.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()),"author/project/"+patternname+".py");
            PDGGraph mpdg = new PDGGraph(patternModule,mcontext);

            MatchPDG match = new MatchPDG();
            graphs=match.getSubGraphs(mpdg,fpdg );
            graphs.forEach(x->x.updateAllMatchedNodes(x));
            match.drawMatchedGraphs(fpdg,graphs,"OUTPUT/matches/"+filename+".dot");
            Utils.markNodesInCode("src/test/resources/author/project/"+filename+".py",
                    graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()),"OUTPUT/matches/"+filename+".html");;
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