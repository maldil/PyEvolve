package com.matching.fgpdg;

import com.matching.ConcreatePythonParser;
import com.matching.fgpdg.nodes.PDGActionNode;
import com.matching.fgpdg.nodes.PDGDataNode;
import com.matching.fgpdg.nodes.PDGEdge;
import com.matching.fgpdg.nodes.PDGNode;
import com.utils.DotGraph;
import com.utils.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.python.antlr.Visitor;
import org.python.antlr.ast.*;
import org.python.antlr.ast.Module;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
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
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
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
        Assertions.assertEquals(2,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
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
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs13() {
        String filename="testm10";
        String patternname = "pattern3";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
        Assertions.assertEquals(9,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs14() {
        String filename="testm11";
        String patternname = "pattern4";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        int nodes = Math.max(graphs.get(0).getCodePDGNodes().size(), graphs.get(1).getCodePDGNodes().size());
        Assertions.assertTrue(graphs.get(0) .isAllMatchedGraph());
        Assertions.assertEquals(7,graphs.get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs15() {
        String filename="testm12";
        String patternname = "pattern2";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs16() {
        String filename="testm13";
        String patternname = "pattern2";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testGetPatternGraphForMatching1() {
        Module codeModule = getPythonModule("author/project/pattern2.py");
        PDGBuildingContext mcontext = null;
        try {
            mcontext = new PDGBuildingContext(codeModule.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()),"author/project/pattern2.py");
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert mcontext != null;
        PDGGraph mpdg = new PDGGraph(codeModule,mcontext);
        MatchPDG pdg = new MatchPDG();
        mpdg=pdg.pruneAndCleanPatternPDG(mpdg);

        PDGNode startNode= mpdg.getNodes().stream().filter(x->x instanceof PDGActionNode && x.getOutEdges().size()==1).collect(Collectors.toList()).get(0);
        List<PDGNode> visitedNotes = startNode.getInEdges().stream().map(PDGEdge::getSource).filter(y->!(y instanceof PDGActionNode)).collect(Collectors.toList());
        visitedNotes.addAll(startNode.getOutEdges().stream().map(PDGEdge::getTarget).filter(y->!(y instanceof PDGActionNode)).collect(Collectors.toList()));
        visitedNotes.add(startNode);
        MatchedNode mNode = new MatchedNode();
        PDGGraph flowMatching = mNode.getSubGraphForDifferentDataFlowMatching(mpdg.getNodes().stream().filter(x->x instanceof PDGActionNode &&   x.getOutEdges().size()==0).collect(Collectors.toList()).get(0), visitedNotes);

        Assertions.assertEquals(3,flowMatching.getNodes().size());
        DotGraph dg = new DotGraph(flowMatching);
        String dirPath = "./OUTPUT/";
        dg.toDotFile(new File(dirPath  +"__removed__file___"+".dot"));
    }


    @Test
    void testGetPatternGraphForMatching2() {
        Module codeModule = getPythonModule("author/project/pattern5.py");
        PDGBuildingContext mcontext = null;
        try {
            mcontext = new PDGBuildingContext(codeModule.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()),"author/project/pattern5.py");
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert mcontext != null;
        PDGGraph mpdg = new PDGGraph(codeModule,mcontext);
        MatchPDG pdg = new MatchPDG();
        mpdg=pdg.pruneAndCleanPatternPDG(mpdg);
        DotGraph dg = new DotGraph(mpdg);
        String dirPath = "./OUTPUT/";
        dg.toDotFile(new File(dirPath  +"__before_removed__file___"+".dot"));

        PDGNode startNode= mpdg.getNodes().stream().filter(x->x instanceof PDGActionNode && x.getOutEdges().size()==0).collect(Collectors.toList()).get(0);
        List<PDGNode> visitedNotes = startNode.getInEdges().stream().map(PDGEdge::getSource).filter(y->!(y instanceof PDGActionNode)).collect(Collectors.toList());
        visitedNotes.addAll(startNode.getOutEdges().stream().map(PDGEdge::getTarget).filter(y->!(y instanceof PDGActionNode)).collect(Collectors.toList()));
        visitedNotes.add(startNode);
        MatchedNode mNode = new MatchedNode();
        PDGGraph flowMatching = mNode.getSubGraphForDifferentDataFlowMatching(mpdg.getNodes().stream().filter(x->x instanceof PDGActionNode &&   x.getInEdges().size()==3 && x.getOutEdges().size()==1).collect(Collectors.toList()).get(0), visitedNotes);

        DotGraph dg1 = new DotGraph(flowMatching);
        dg1.toDotFile(new File(dirPath  +"__removed__file___"+".dot"));
        Assertions.assertEquals(9,flowMatching.getNodes().size());
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
            match.drawMatchedGraphs(fpdg,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()),"OUTPUT/matches/"+filename+".dot");
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

    @Test
    void canWalkFromNodeToNode() {
        Module codeModule = getPythonModule("author/project/testm9.py");
        FunctionDef func=null;
        for (org.python.antlr.base.stmt stmt : codeModule.getInternalBody()) {
            if (stmt instanceof FunctionDef){
                func= (FunctionDef) stmt;
            }
        }
        PDGBuildingContext mcontext = null;
        try {
            mcontext = new PDGBuildingContext(codeModule.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()),"author/project/testm9.py");
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert mcontext != null;
        PDGGraph mpdg = new PDGGraph(func,mcontext);
//        MatchPDG pdg = new MatchPDG();
//        mpdg=pdg.pruneAndCleanPatternPDG(mpdg);
        DotGraph dg = new DotGraph(mpdg);
        String dirPath = "./OUTPUT/";
        dg.toDotFile(new File(dirPath  +"__walk__code___"+".dot"));
        PDGNode startNode=null;
        PDGNode endNode=null;
        for (PDGNode node : mpdg.getNodes()) {
            if (node instanceof PDGActionNode){
                if(((PDGActionNode) node).getName().equals("dot")){
                    PDGNode dep = node.getInEdges().stream().filter(x -> x.getLabel().equals("para")).map(PDGEdge::getSource).collect(Collectors.toList()).get(0);
                    if(((PDGDataNode)dep).getDataName().equals("W")){
                        startNode=node;
                    }else{
                        endNode=node;
                    }
                }
            }
        }
        MatchedNode matchNode = new MatchedNode();
        Assertions.assertTrue(matchNode.canWalkFromNodeToNode(startNode, endNode, MatchedNode.DIRECTION.TO, new HashSet<>()));
        Assertions.assertEquals(false,matchNode.canWalkFromNodeToNode(startNode, endNode, MatchedNode.DIRECTION.FROM, new HashSet<>()));
        Assertions.assertTrue(matchNode.canWalkFromNodeToNode(endNode,startNode,  MatchedNode.DIRECTION.FROM, new HashSet<>()));
        Assertions.assertEquals(false,matchNode.canWalkFromNodeToNode(endNode,startNode,  MatchedNode.DIRECTION.TO, new HashSet<>()));
        System.out.println();


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