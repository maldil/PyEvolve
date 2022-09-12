package com.matching.fgpdg;

import com.matching.ConcreatePythonParser;
import com.matching.fgpdg.nodes.*;
import com.matching.fgpdg.nodes.TypeInfo.TypeWrapper;
import com.utils.DotGraph;
import com.utils.FileIO;
import org.inferrules.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.python.antlr.Visitor;
import org.python.antlr.ast.*;
import org.python.antlr.ast.Module;
import org.python.antlr.base.stmt;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.inferrules.Utils.getMatchedNodes;
import static org.inferrules.Utils.getPythonModule;

class MatchedNodeTest {

    @Test
    void testSubGraphs1()  throws Exception{
        String filename="testm";
        String patternname = "pattern1";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        graphs.get(0).isAllMatchedGraph();
        Assertions.assertTrue(graphs.get(0).isAllMatchedGraph());
        Assertions.assertEquals(7,graphs.get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs2()  throws Exception{
        String filename="testm1";
        String patternname = "pattern1";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
    }

//    @Test
//    void testSubGraphs3() {
//        String filename="testm2";
//        String patternname = "pattern";
//        List<MatchedNode> graphs =null;
//        graphs = getMatchedNodes(filename, patternname, graphs);
//        Assertions.assertTrue(graphs.get(0).isAllMatchedGraph());
//        Assertions.assertEquals(16,graphs.get(0).getCodePDGNodes().size());
//    }

    @Test
    void testSubGraphs4()  throws Exception{
        String filename="test1";
        String patternname = "pattern";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertTrue(graphs.get(0).isAllMatchedGraph());
        Assertions.assertEquals(17,graphs.get(0).getCodePDGNodes().size());
    }

//
//    @Test
//    void testSubGraphs5() {
//        String filename="testm2";
//        String patternname = "pattern";
//        List<MatchedNode> graphs =null;
//        graphs = getMatchedNodes(filename, patternname, graphs);
//        Assertions.assertTrue(graphs.get(0).isAllMatchedGraph());
//        Assertions.assertEquals(16,graphs.get(0).getCodePDGNodes().size());
//    }

    @Test
    void testSubGraphs6()  throws Exception{
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
        Assertions.assertEquals(17,nodes);
    }


    @Test
    void testSubGraphs7()  throws Exception{
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
        Assertions.assertEquals(17,nodes);
    }

    @Test
    void testSubGraphs8()  throws Exception{
        String filename="testm5";
        String patternname = "pattern";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(2,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
    }

    @Test
    void testSubGraphs9() throws Exception {
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
        Assertions.assertEquals(17,graphs.get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs10()  throws Exception{
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
    void testSubGraphs11()  throws Exception{
        String filename="testm8";
        String patternname = "pattern3";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertTrue(graphs.get(0) .isAllMatchedGraph());
        Assertions.assertEquals(5,graphs.get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs12()  throws Exception{
        String filename="testm9";
        String patternname = "pattern2";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs13()  throws Exception{
        String filename="testm10";
        String patternname = "pattern3";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
        Assertions.assertEquals(9,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs14()  throws Exception{
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
    void testSubGraphs15()  throws Exception{
        String filename="testm12";
        String patternname = "pattern2";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs16()  throws Exception{
        String filename="testm13";
        String patternname = "pattern2";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs17()  throws Exception{
        String filename="testm14";
        String patternname = "pattern6";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs18()  throws Exception{
        String filename="testm15";
        String patternname = "pattern7";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs19() throws Exception {
        String filename="testm15";
        String patternname = "pattern";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs20() throws Exception {
        String filename="testm16";
        String patternname = "pattern";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs21() throws Exception {
        String filename="testm17";
        String patternname = "pattern";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(0,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs22() throws Exception {
        String filename="test33";
        String patternname = "pattern_v1";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs23() throws Exception {
        String filename="testm18";
        String patternname = "pattern";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs24() throws Exception {
        String filename="testm19";
        String patternname = "GroupOfPatterns/p1";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs25() throws Exception {
        String filename="testm20";
        String patternname = "pattern8";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs26() throws Exception {
        String filename="testm23";
        String patternname = "pattern9";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs27() throws Exception {
        String filename="testm22";
        String patternname = "pattern9";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs28() throws Exception {
        String filename="testm23";
        String patternname = "pattern9";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs29() throws Exception {
        String filename="testm24";
        String patternname = "pattern10";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs30() throws Exception {
        String filename="test26";
        String patternname = "pattern12";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs31() throws Exception {
        String filename="testm27";
        String patternname = "pattern12";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs32() throws Exception {
        String filename="testm28";
        String patternname = "pattern13";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs33() throws Exception {
        String filename="testm29";
        String patternname = "pattern9";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs34() throws Exception {
        String filename="testm30";
        String patternname = "pattern";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs35() throws Exception {
        String filename="testm31";
        String patternname = "pattern14";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }


    @Test
    void testSubGraphs36() throws Exception {
        String filename="test34";
        String patternname = "pattern15";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }



    @Test
    void testSubGraphs37() throws Exception {
        String filename="test35";
        String patternname = "pattern16";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs38() throws Exception {
        String filename="testm32";
        String patternname = "pattern15";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs39() throws Exception {
        String filename="test26";
        String patternname = "pattern12";
        List<MatchedNode> graphs =null;
        graphs = getMatchedNodes(filename, patternname, graphs);
        Assertions.assertEquals(1,graphs.stream().filter(MatchedNode::isAllChildsMatched).count());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testSubGraphs40() throws Exception {
        String filename="test32";
        String patternname = "pattern15";
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
        PDGGraph flowMatching = mNode.getSubGraphForDifferentDataFlowMatching(mpdg.getNodes().stream().filter(x->x instanceof PDGActionNode &&
                x.getOutEdges().size()==0).collect(Collectors.toList()).get(0), visitedNotes,mcontext);

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
        PDGGraph flowMatching = mNode.getSubGraphForDifferentDataFlowMatching(mpdg.getNodes().stream().filter(x->x instanceof PDGActionNode &&
                x.getInEdges().size()==3 && x.getOutEdges().size()==1).collect(Collectors.toList()).get(0), visitedNotes,mcontext);

        DotGraph dg1 = new DotGraph(flowMatching);
        dg1.toDotFile(new File(dirPath  +"__removed__file___"+".dot"));
        Assertions.assertEquals(9,flowMatching.getNodes().size());
    }

//    private String getPathToResources(String name){
//        File f = new File(name);
//        if (f.exists()) {
//            return f.getAbsolutePath();
//        }
//        return getClass().getClassLoader().getResource(name).getPath();
//
//    }

//    private List<MatchedNode> getMatchedNodes(String filename, String patternname, List<MatchedNode> graphs) throws Exception {
//        Module codeModule = getPythonModule("author/project/"+filename+".py");
//        Module patternModule = getPythonModuleForTemplate(getPathToResources("author/project/"+patternname+".py"));
//        patternModule.toString();
//        FunctionDef func=null;
//        for (org.python.antlr.base.stmt stmt : codeModule.getInternalBody()) {
//            if (stmt instanceof FunctionDef){
//                func= (FunctionDef) stmt;
//                break;
//            }
//            else if (stmt instanceof ClassDef){
//                for (org.python.antlr.base.stmt stmt1 : ((ClassDef) stmt).getInternalBody()) {
//                    if (stmt1 instanceof FunctionDef){
//                        func= (FunctionDef) stmt1;
//                        break;
//                    }
//                }
//
//            }
//        }
//        PDGBuildingContext fcontext = null;
//            fcontext = new PDGBuildingContext(codeModule.getInternalBody().stream().filter(x-> x instanceof Import
//                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/"+filename+".py");
//            PDGGraph fpdg = new PDGGraph(func,fcontext);
//            fpdg.getNodes().forEach(x-> System.out.println(x.getId()));
//            Guards guards = new Guards(com.utils.Utils.getFileContent(getPathToResources("author/project/"+patternname+".py")),patternModule);
//            TypeWrapper wrapper = new TypeWrapper(guards);
//            PDGBuildingContext mcontext = new PDGBuildingContext(patternModule.getInternalBody().stream().filter(x -> x instanceof Import
//                || x instanceof ImportFrom).collect(Collectors.toList()),wrapper);
//            PDGGraph mpdg = new PDGGraph(patternModule,mcontext);
//            MatchPDG match = new MatchPDG();
//            graphs=match.getSubGraphs(mpdg,fpdg,mcontext,fcontext );
//            graphs.forEach(x->x.updateAllMatchedNodes(x,mpdg));
//            List<MatchedNode> finalPatterns = graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList());
//
//            match.drawMatchedGraphs(fpdg,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()),"OUTPUT/matches/"+filename+".dot");
//            Utils.markNodesInCode("src/test/resources/author/project/"+filename+".py",
//                    graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()),"OUTPUT/matches/"+filename+".html","","x@x");;
//
//        return graphs;
//    }
//
//
//    private Module getPythonModule(String fileName){
//        ConcreatePythonParser parser = new ConcreatePythonParser();
//        return parser.parse(fileName);
//    }
//
//    private Module getPythonModuleForTemplate(String fileName) throws Exception {
//        ConcreatePythonParser parser = new ConcreatePythonParser();
//        return parser.parseTemplates(FileIO.readStringFromFile(fileName));
//    }

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