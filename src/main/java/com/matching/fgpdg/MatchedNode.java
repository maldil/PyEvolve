package com.matching.fgpdg;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.ibm.wala.util.collections.Pair;
import com.matching.fgpdg.nodes.*;
import com.utils.Assertions;
import com.utils.DotGraph;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MatchedNode {
    protected PDGNode codeNode;
    protected PDGNode codeParaNode=null;
    protected PDGNode patternNode;
    protected MatchedNode parentNode=null;
    protected HashSet<MatchedNode> matchedChildNodes = new HashSet<>();
    private boolean allChildsMatched=false;
    private boolean updateAllMatchedNodesInvoked=false;
    public enum DIRECTION {FROM,TO};
    public enum NODE_PROPERTIES {CLONE,SWAPPED};
    public enum SWAPPED {YES,NO};
    PDGBuildingContext patternContext;
    PDGBuildingContext codeContext;
    public MatchedNode(){

    }

    public PDGNode getCodeParaNode() {
        return codeParaNode;
    }

    public void setCodeParaNode(PDGNode codeParN) {
         this.codeParaNode=codeParN;
    }

    public MatchedNode(PDGNode codeNode, PDGNode patternNode,HashSet<PDGNode> visitedASTNodes, PDGBuildingContext pContex, PDGBuildingContext cContex) {
        this.codeNode = codeNode;
        this.patternNode = patternNode;
        this.codeContext = cContex;
        this.patternContext = pContex;

//        System.out.println(codeNode+"===="+patternNode);

        Pair<HashMap<PDGNode, HashSet<PDGNode>>, HashMap<PDGNode, HashSet<PDGNode>>> nextMatchedNodePairs
                = getNextMatchedNodePairs(Pair.make(codeNode, patternNode));
        visitedASTNodes.add(codeNode);
        HashSet<PDGNode> parentVisits = new HashSet<>(visitedASTNodes);


        if (nextMatchedNodePairs!=null){
            for (Map.Entry<PDGNode, HashSet<PDGNode>> entry : nextMatchedNodePairs.fst.entrySet()) {
                for (PDGNode node : entry.getValue()) {
                    if (!parentVisits.contains(node)) {
                        MatchedNode matchedNode = new MatchedNode(node, entry.getKey(), visitedASTNodes,patternContext,codeContext);
                        matchedNode.setParentNode(this);
                        matchedChildNodes.add(matchedNode);
                    }
                }
            }
            for (Map.Entry<PDGNode, HashSet<PDGNode>> entry : nextMatchedNodePairs.snd.entrySet()) {
                for (PDGNode node : entry.getValue()) {
                    if (!parentVisits.contains(node)) {
                        MatchedNode matchedNode = new MatchedNode(node, entry.getKey(), visitedASTNodes,patternContext,codeContext);
                        matchedNode.setParentNode(this);
                        matchedChildNodes.add(matchedNode);
                    }
                }
            }
        }

        // 1. Following section should only be executed if the pattern node maintains parameter edge with its children
        // 2. Get all unvisited sub-graphs that has parameter edges to the code node
        // 3. Mine sub-graphs
        // 3. If there is a fully matched sub graph, add it to the matchedChildNodes
        if (Configurations.CONSIDER_DATA_FLOW){
            Pair<HashMap<PDGNode, HashSet<PDGNode>>, HashMap<PDGNode, HashSet<PDGNode>>> matchedNodesFromTheChildren = getMatchedNodesFromTheChildren(Pair.make(codeNode, patternNode));
            List<PDGNode> matchedCodeNodes = new ArrayList<>();
            matchedNodesFromTheChildren.fst.values().forEach(matchedCodeNodes::addAll);
            matchedNodesFromTheChildren.snd.values().forEach(matchedCodeNodes::addAll);

            List<PDGNode> matchedPatternNodes = new ArrayList<>();
            matchedPatternNodes.addAll(matchedNodesFromTheChildren.fst.keySet());
            matchedPatternNodes.addAll(matchedNodesFromTheChildren.snd.keySet());

            List<PDGNode> codeParaNodes = codeNode.getInEdges().stream().filter(x -> x.getLabel().equals("para")).map(PDGEdge::getSource).filter(u->!matchedCodeNodes.contains(u)).collect(Collectors.toList());
            codeParaNodes.addAll(codeNode.getOutEdges().stream().filter(x -> x.getLabel().equals("para")).map(PDGEdge::getTarget).filter(u->!matchedCodeNodes.contains(u)).collect(Collectors.toList()));
            List<PDGNode> patternParaNodes = patternNode.getInEdges().stream().filter(x -> x.getLabel().equals("para")).map(PDGEdge::getSource).filter(u->!matchedPatternNodes.contains(u)).collect(Collectors.toList());
            patternParaNodes.addAll(patternNode.getOutEdges().stream().filter(x -> x.getLabel().equals("para")).map(PDGEdge::getTarget).filter(u->!matchedPatternNodes.contains(u)).collect(Collectors.toList()));


//            ArrayList<PDGNode> visitedCodeASTNodes = new ArrayList<>(visitedASTNodes);




            if (codeParaNodes.size()>0 && patternParaNodes.size()>0){
                for (PDGNode paraN : patternParaNodes) {
                    ArrayList<PDGNode> visitedCodeASTNodes = new ArrayList<>()  ;
                    visitedCodeASTNodes.add(codeNode);
                    visitedCodeASTNodes.addAll(codeParaNodes);
                    if (matchedChildNodes.stream().noneMatch(x -> x.getPatternNode() == paraN)){
                        ArrayList<PDGNode> visitedPatternNode = new ArrayList<>();
                        visitedPatternNode.add(this.patternNode);
                        PDGGraph patternPDG = getSubGraphForDifferentDataFlowMatching(paraN, visitedPatternNode,this.patternContext);
                        DotGraph dg = new DotGraph(patternPDG);
                        String dirPath = "./OUTPUT/";
                        dg.toDotFile(new File(dirPath  +"__patternPDG1__file___"+".dot"));
                        for (PDGNode codeN : codeParaNodes) {
                            PDGGraph codePDG = getSubGraphForDifferentDataFlowMatching(codeN, visitedCodeASTNodes,this.codeContext);
                            DotGraph dg1 = new DotGraph(codePDG);
                            dg1.toDotFile(new File(dirPath  +"__patternPDG2__file___"+".dot"));
                            MatchPDG match = new MatchPDG();
                            List<MatchedNode> subGraphs = match.getSubGraphs(patternPDG, codePDG, patternPDG.getPDGNode(paraN.getId()) );
                            if (subGraphs!=null){
                                subGraphs.forEach(x->x.updateAllMatchedNodes(x,patternPDG));
                                for (MatchedNode graph : subGraphs) {
                                    if (graph.isAllMatchedGraph()){
                                        /*
                                         * 1. get the code node matched to the pattern Node , paraN
                                         * 2. get the direction of the edge between this.patternNode and the paraN
                                         * 3. Use canWalkFromNodeToNode to check whether the we can walk from the this.codeNode to the node found in step 1
                                         * */
                                        ArrayList<PDGNode> codeNodes = graph.getMatchedCodeNodes(patternPDG.getPDGNode(paraN.getId()));
//                                    assert codeNodes.size()>1;

                                        PDGNode node = codeNodes.get(0);
                                        boolean connectionFound;
                                        if ( paraN.getInEdges().stream().map(PDGEdge::getSource).collect(Collectors.toList()).contains(patternNode)){
                                            connectionFound = canWalkFromNodeToNode(codeNode, (PDGNode) node.getProperty(NODE_PROPERTIES.CLONE), DIRECTION.TO, new HashSet<>());
                                        }else{
                                            connectionFound = canWalkFromNodeToNode(codeNode, (PDGNode) node.getProperty(NODE_PROPERTIES.CLONE), DIRECTION.FROM, new HashSet<>());
                                        }
                                        if (connectionFound){
                                            graph.updateCodeAndPatternNodes();
                                            matchedChildNodes.add(graph);
                                            graph.setCodeParaNode(codeN);

                                            for (Map.Entry<PDGNode, HashSet<PDGNode>> entry : matchedNodesFromTheChildren.fst.entrySet()) {
                                                for (PDGNode node1 : entry.getValue()) {
                                                    if (!parentVisits.contains(node1)) {
                                                        MatchedNode matchedNode = new MatchedNode(node1, entry.getKey(), visitedASTNodes,patternContext,codeContext);
                                                        matchedNode.setParentNode(this);
                                                        if (!matchedChildNodes.contains(matchedNode))     {
                                                            matchedChildNodes.add(matchedNode);
                                                        }
                                                    }
                                                }
                                            }
                                            for (Map.Entry<PDGNode, HashSet<PDGNode>> entry : matchedNodesFromTheChildren.snd.entrySet()) {
                                                for (PDGNode node1 : entry.getValue()) {
                                                    if (!parentVisits.contains(node1)) {
                                                         MatchedNode matchedNode = new MatchedNode(node1, entry.getKey(), visitedASTNodes,patternContext,codeContext);
                                                         matchedNode.setParentNode(this);
                                                         if (!matchedChildNodes.contains(matchedNode)){
                                                                matchedChildNodes.add(matchedNode);
                                                         }

                                                    }
                                                }
                                            }




                                            break;
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    /*
    * from: start node for the walk
    * to: end node for the walk
    * toDirection : the direction of the arrow for the to Node
    * visitedNodes : nodes that need to be avoided
    * */

    public boolean canWalkFromNodeToNode(PDGNode from, PDGNode to ,DIRECTION toDirection,HashSet<PDGNode> visitedNodes) {
        visitedNodes.add(from);
        if (from==to)
            return false;
        else if (toDirection==DIRECTION.FROM && from.getInEdges().stream().filter
                (x -> x.getLabel().equals("para")).
                map(PDGEdge::getSource).anyMatch(c -> c == to)){
            return true;
        }
        else if(toDirection==DIRECTION.TO && from.getOutEdges().stream().filter
                (x -> x.getLabel().equals("para")).
                map(PDGEdge::getTarget).anyMatch(c -> c == to)
        ){
            return true;
        }
        else{
            List<PDGNode> outNodes = from.getOutEdges().stream().filter(x -> x.getLabel().equals("para") ||
                    x.getLabel().equals("def") ||x.getLabel().equals("ref")).map(PDGEdge::getTarget).filter(c -> !visitedNodes.contains(c)).collect(Collectors.toList());
            for (PDGNode outNode : outNodes) {
                if (canWalkFromNodeToNode(outNode,to,toDirection,visitedNodes)){
                    return true;
                }
            }
            List<PDGNode> inNodes = from.getInEdges().stream().filter(x -> x.getLabel().equals("para") ||
                    x.getLabel().equals("def") || x.getLabel().equals("ref")).map(PDGEdge::getSource).filter(c -> !visitedNodes.contains(c)).collect(Collectors.toList());
            for (PDGNode node : inNodes) {
                if (canWalkFromNodeToNode(node,to,toDirection,visitedNodes)){
                    return true;
                }
            }
            return false;
        }
    }

    public void setParentNode(MatchedNode parentNode) {
        this.parentNode = parentNode;
    }


    /*
    node: the node that become the start of the sub-graph
    voidNode: nodes that have already been visited
    * */
    public PDGGraph getSubGraphForDifferentDataFlowMatching(PDGNode node,List<PDGNode> avoidNodes,PDGBuildingContext context ){
        PDGGraph grap = new PDGGraph(context);
        HashSet<PDGNode> nodes = node.getAllChildNodes(20,avoidNodes);
        HashMap<Integer,PDGNode> nodeMap=new HashMap<>();
        HashSet<PDGNode> newnodes = new HashSet<>();
        for (PDGNode pdgNode : nodes) {
            if (pdgNode instanceof PDGDataNode){
                PDGDataNode dataNode = new PDGDataNode((PDGDataNode) pdgNode);
                dataNode.setProperty(NODE_PROPERTIES.CLONE,pdgNode);
                dataNode.setProperty(NODE_PROPERTIES.SWAPPED,SWAPPED.NO);
                dataNode.setId(pdgNode.getId());
                newnodes.add(dataNode);
                nodeMap.put(pdgNode.getId(),dataNode);
            }
            else if (pdgNode instanceof PDGActionNode){
                PDGActionNode dataNode = new PDGActionNode(  pdgNode.getAstNode(),pdgNode.getAstNodeType(),pdgNode.getKey(),pdgNode.getDataType(),((PDGActionNode) pdgNode).getName());
                dataNode.setProperty(NODE_PROPERTIES.CLONE,pdgNode);
                dataNode.setProperty(NODE_PROPERTIES.SWAPPED,SWAPPED.NO);
                dataNode.setId(pdgNode.getId());
                newnodes.add(dataNode);
                nodeMap.put(pdgNode.getId(),dataNode);
            }
            else if (pdgNode instanceof PDGControlNode){
                PDGControlNode cNode= new PDGControlNode(pdgNode.getAstNode(),pdgNode.getAstNodeType(),pdgNode.getControl());
                cNode.setProperty(NODE_PROPERTIES.CLONE,pdgNode);
                cNode.setProperty(NODE_PROPERTIES.SWAPPED,SWAPPED.NO);
                cNode.setId(pdgNode.getId());
                newnodes.add(cNode);
                nodeMap.put(pdgNode.getId(),cNode);
            }
            else if (pdgNode instanceof PDGEntryNode){
                PDGEntryNode entryNodennode = new PDGEntryNode(pdgNode.getAstNode(),pdgNode.getAstNodeType(),pdgNode.getLabel());
                entryNodennode.setProperty(NODE_PROPERTIES.CLONE,pdgNode);
                entryNodennode.setProperty(NODE_PROPERTIES.SWAPPED,SWAPPED.NO);
                entryNodennode.setId(pdgNode.getId());
                newnodes.add(entryNodennode);
                nodeMap.put(pdgNode.getId(),entryNodennode);
            }
            else if (pdgNode instanceof PDGAlphHole){
                PDGAlphHole alpHole = new PDGAlphHole(pdgNode.getAstNode(),pdgNode.getAstNodeType(),
                        ((PDGAlphHole) pdgNode).getValue(),pdgNode.getKey(),pdgNode.getDataType(),pdgNode.getDataName(),
                        ((PDGAlphHole) pdgNode).isDataNode(),((PDGAlphHole) pdgNode).isActionNode(),
                        ((PDGAlphHole) pdgNode).isControlNode());
                alpHole.setProperty(NODE_PROPERTIES.CLONE,pdgNode);
                alpHole.setProperty(NODE_PROPERTIES.SWAPPED,SWAPPED.NO);
                alpHole.setId(pdgNode.getId());
                newnodes.add(alpHole);
                nodeMap.put(pdgNode.getId(),alpHole);
            }
            else if (pdgNode instanceof PDGLazyHole){
                PDGLazyHole lazyHole = new PDGLazyHole(pdgNode.getAstNode(),pdgNode.getAstNodeType(),
                        ((PDGLazyHole) pdgNode).getValue(),pdgNode.getKey(),pdgNode.getDataType(),pdgNode.getDataName(),
                        ((PDGLazyHole) pdgNode).isDataNode(),((PDGLazyHole) pdgNode).isActionNode(),
                        ((PDGLazyHole) pdgNode).isControlNode());
                lazyHole.setProperty(NODE_PROPERTIES.CLONE,pdgNode);
                lazyHole.setProperty(NODE_PROPERTIES.SWAPPED,SWAPPED.NO);
                lazyHole.setId(pdgNode.getId());
                newnodes.add(lazyHole);
                nodeMap.put(pdgNode.getId(),lazyHole);
            }
            else
                Assertions.UNREACHABLE("Unhandled node type"+pdgNode.getClass());
        }

        for (PDGNode pdgNode : nodes) {
            pdgNode.getInEdges().stream().filter(y->nodeMap.get(y.getTarget().getId())!=null&&  nodeMap.get(y.getSource().getId())!=null).forEach(x->createListNewOfEdges(x,nodeMap.get(x.getSource().getId()),nodeMap.get(x.getTarget().getId())));
            pdgNode.getOutEdges().stream().filter(y->nodeMap.get(y.getTarget().getId())!=null&&  nodeMap.get(y.getSource().getId())!=null).forEach(x->createListNewOfEdges(x,nodeMap.get(x.getSource().getId()),nodeMap.get(x.getTarget().getId())));

        }


//        newnodes.forEach(y->{
//            y.getInEdges().forEach(z->{
//                z.setSource(nodeMap.get(z.getSource().getId()));
//                z.setTarget(nodeMap.get(z.getTarget().getId()));
//            });
//            y.getOutEdges().forEach(z->{
//                z.setSource(nodeMap.get(z.getSource().getId()));
//                z.setTarget(nodeMap.get(z.getTarget().getId()));
//            });
//        });

        grap.nodes.addAll(newnodes);
        return grap;
    }

    public static PDGNode[] concatenate(PDGNode[] ...arrays)
    {
        return Stream.of(arrays)
                .flatMap(Stream::of)        // or, use `Arrays::stream`
                .toArray(PDGNode[]::new);
    }

    private void createListNewOfEdges(PDGEdge edge, PDGNode source, PDGNode target){
        List<PDGEdge> newEdges= new ArrayList<>();
        if (source.getInEdges().stream().map(PDGEdge::getSource).collect(Collectors.toList()).contains(target)&&
                source.getInEdges().stream().filter(x -> x.getSource() == target).anyMatch(y -> y.getLabel().equals(edge.getLabel())))
            return;
        List<PDGEdge> collect = source.getOutEdges().stream().filter(x -> x.getTarget() == target).collect(Collectors.toList());
        if (source.getOutEdges().stream().map(PDGEdge::getTarget).collect(Collectors.toList()).contains(target)&&
                source.getOutEdges().stream().filter(x -> x.getTarget() == target).anyMatch(y -> y.getLabel().equals(edge.getLabel())))
            return;


        if (edge instanceof PDGDataEdge){
                PDGDataEdge nEdge = new PDGDataEdge(source,target,((PDGDataEdge) edge).getType());
                newEdges.add(nEdge);
        }
        else if (edge instanceof PDGControlEdge){
                PDGControlEdge nEdge = new PDGControlEdge(source,target,edge.getLabel());
                newEdges.add(nEdge);
        }
        else  {
                Assertions.UNREACHABLE("Unhandled edge type");
        }

    }

//    public List<MatchedNode> getMatchedSubGraphs(PDGNode codeNode, PDGNode patternNode,PDGNode avoidCodeNode){
//        ArrayList<Pair<PDGNode,PDGNode>> startNodes = new ArrayList<>();
//        List<MatchedNode> subGraphs=null;
//        if (patternNode !=null){
//            for (PDGNode cnd : codeNode.getAllChildNodes(2,avoidCodeNode)) {
//                if (isEqualNodes(cnd, patternNode)){
//                    startNodes.add(Pair.make(cnd, patternNode));
//                }
//            }
//            if (startNodes.size()!=0) {
//                subGraphs = startNodes.stream().map(x -> new MatchedNode(x.fst, x.snd, new HashSet<>())).collect(Collectors.toList());
//                subGraphs.forEach(x->x.updateAllMatchedNodes(x));
//            }
//        }
//        return subGraphs;
//    }

    public void updateAllMatchedNodes(MatchedNode matchedGraph, PDGGraph patternPDG){
        if (this.allChildsMatched)
            return;
        for (PDGNode node : patternPDG.getNodes()) {
            if(!matchedGraph.getAllMatchedNodes().stream().map(MatchedNode::getPatternNode).collect(Collectors.toList()).contains(node)){
                this.allChildsMatched=false;
                return;
            }
        }
        this.allChildsMatched=true;
        List<PDGNode> childNodesofPatternNodes = patternNode.getInEdges().stream().map(PDGEdge::getSource).collect(Collectors.toList());
        childNodesofPatternNodes.addAll(patternNode.getOutEdges().stream().map(PDGEdge::getTarget).collect(Collectors.toList()));
        List<PDGNode> childNodesOfCodeNodes = codeNode.getInEdges().stream().map(PDGEdge::getSource).collect(Collectors.toList());
        childNodesOfCodeNodes.addAll(codeNode.getOutEdges().stream().map(PDGEdge::getTarget).collect(Collectors.toList()));
        for (PDGNode node : childNodesofPatternNodes) {
            List<PDGNode> collect1 = matchedGraph.getAllMatchedNodes().stream().filter(x -> x.getPatternNode() == node).map(MatchedNode::getCodeNode)
                    .collect(Collectors.toList());
            List<PDGNode> collect2 = matchedGraph.getAllMatchedNodes().stream().filter(x -> x.getPatternNode() == node).filter(y->y.getCodeParaNode()!=null).map(MatchedNode::getCodeParaNode)
                    .collect(Collectors.toList());
            if (collect1.size()>0){
//                get matched code nodes of he collect1
                boolean isChildNodeOfCodeNodeEqualTOChildNodeOfPatternNode=false;
                for (PDGNode matchedCodeNode : collect1) {
                    if (childNodesOfCodeNodes.contains(matchedCodeNode)){
                        isChildNodeOfCodeNodeEqualTOChildNodeOfPatternNode=true;
                        break;
                    }
                }
                for (PDGNode pdgNode : collect2) { //Check whether the respective child node of the code node is equal for the nodes matched through parameter edges
                    if (childNodesOfCodeNodes.contains(pdgNode)){
                        isChildNodeOfCodeNodeEqualTOChildNodeOfPatternNode=true;
                        break;
                    }
                }
                if (!isChildNodeOfCodeNodeEqualTOChildNodeOfPatternNode){
                    allChildsMatched=false;
                    break;
                }
            }
            else
            {
                allChildsMatched=false;
            }
        }
        matchedChildNodes.forEach(x->x.updateAllMatchedNodes(matchedGraph,patternPDG));
    }

    public boolean isAllMatchedGraph(){
        if (allChildsMatched) {
            Map<PDGNode, List<MatchedNode>> collect = matchedChildNodes.stream().collect(Collectors.groupingBy(MatchedNode::getPatternNode));
            List<MatchedNode> childsWithOneChilds = collect.values().stream().filter(x -> x.size() == 1).map(y -> y.get(0)).collect(Collectors.toList());
            Boolean singleMatchers = childsWithOneChilds.stream().reduce(true, (x1, x2) -> x1 && x2.isAllMatchedGraph(), (y1, y2) -> y1 && y2);//check the all child nodes maintain all matched children
            List<List<MatchedNode>> collect1 = collect.values().stream().filter(x -> x.size() > 1).collect(Collectors.toList());
            for (List<MatchedNode> combinations : Lists.cartesianProduct(collect1)) {
                Boolean multiMatchers = combinations.stream().reduce(true, (x1, x2) -> x1 && x2.isAllMatchedGraph(), (y1, y2) -> y1 && y2);
                if (multiMatchers&&singleMatchers){
                    return true;
                }
            }
            return false;
        }
        else{
            return false;
        }
    }

//    public MatchedNode getAllMatchedGraph(){
//        if (allChildsMatched) {
//
//
//        }
//        else
//            return null;
//    }

    public HashSet<MatchedNode> getMatchedChildNodes() {
        return matchedChildNodes;
    }

    public boolean isAllChildsMatched() {
        return allChildsMatched;
    }

    public PDGNode getCodeNode() {
        return codeNode;
    }

    public PDGNode getPatternNode() {
        return patternNode;
    }

    private boolean isAllChildesMatch(){
        List<PDGNode> collect = patternNode.getOutEdges().stream().map(PDGEdge::getTarget).collect(Collectors.toList());
        collect.addAll(patternNode.getInEdges().stream().map(PDGEdge::getSource).collect(Collectors.toList()));
        List<PDGNode> childPatternNodes= matchedChildNodes.stream().map(MatchedNode::getPatternNode).collect(Collectors.toList());
        for (PDGNode node : collect) {
            if (!childPatternNodes.contains(node)){
                return false;
            }
        }
        return true;
    }

    private Pair<HashMap<PDGNode, HashSet<PDGNode>>, HashMap<PDGNode, HashSet<PDGNode>>> getMatchedNodesFromTheChildren(Pair<PDGNode, PDGNode> startNodes){
        HashSet<PDGNode> visitedOutNodes = new HashSet<>();
        HashSet<PDGNode> visitedInNodes = new HashSet<>();
        Map<String, List<PDGEdge>> codeOutEdges = startNodes.fst.getOutEdges().stream()
                .collect(Collectors.groupingBy(PDGEdge::getLabel));
        Map<String, List<PDGEdge>> patternOutEdges = startNodes.snd.getOutEdges().stream()
                .collect(Collectors.groupingBy(PDGEdge::getLabel));

        Map<String, List<PDGEdge>> codeInEdges = startNodes.fst.getInEdges().stream()
                .collect(Collectors.groupingBy(PDGEdge::getLabel));
        Map<String, List<PDGEdge>> patternInEdges = startNodes.snd.getInEdges().stream()
                .collect(Collectors.groupingBy(PDGEdge::getLabel));

        HashMap<PDGNode,HashSet<PDGNode>> matchedOutNodesLists= getMatchersForOutPaths(  visitedOutNodes , codeOutEdges, patternOutEdges);
        HashMap<PDGNode,HashSet<PDGNode>> matchedInNodesLists = getMatchersForInPaths( visitedInNodes, codeInEdges, patternInEdges);
        return Pair.make(matchedOutNodesLists,matchedInNodesLists);
    }

    private Pair<HashMap<PDGNode, HashSet<PDGNode>>, HashMap<PDGNode, HashSet<PDGNode>>>
    getNextMatchedNodePairs(Pair<PDGNode, PDGNode> startNodes) {

        HashSet<PDGNode> visitedOutNodes = new HashSet<>();
        HashSet<PDGNode> visitedInNodes = new HashSet<>();
        Map<String, List<PDGEdge>> codeOutEdges = startNodes.fst.getOutEdges().stream()
                .collect(Collectors.groupingBy(PDGEdge::getLabel));
        Map<String, List<PDGEdge>> patternOutEdges = startNodes.snd.getOutEdges().stream()
                .collect(Collectors.groupingBy(PDGEdge::getLabel));

        Map<String, List<PDGEdge>> codeInEdges = startNodes.fst.getInEdges().stream()
                .collect(Collectors.groupingBy(PDGEdge::getLabel));
        Map<String, List<PDGEdge>> patternInEdges = startNodes.snd.getInEdges().stream()
                .collect(Collectors.groupingBy(PDGEdge::getLabel));

        HashMap<PDGNode,HashSet<PDGNode>> matchedOutNodesLists = getMatchersForOutPaths( visitedOutNodes , codeOutEdges, patternOutEdges);
        HashMap<PDGNode,HashSet<PDGNode>> matchedInNodesLists = getMatchersForInPaths( visitedInNodes, codeInEdges, patternInEdges);

        for (PDGNode node : startNodes.snd.getOutEdges().stream().map(PDGEdge::getTarget).collect(Collectors.toList())) {
            if (!matchedOutNodesLists.containsKey(node)){
                return null;
            }
        }
        for (PDGNode node : startNodes.snd.getInEdges().stream().map(PDGEdge::getSource).collect(Collectors.toList())) {
            if (!matchedInNodesLists.containsKey(node)){
                return null;
            }
        }
        return Pair.make(matchedOutNodesLists,matchedInNodesLists) ;
    }

    private HashMap<PDGNode, HashSet<PDGNode>> getMatchersForOutPaths(HashSet<PDGNode> visitedNodes, Map<String, List<PDGEdge>> codeOutEdges, Map<String, List<PDGEdge>> patternOutEdges) {
        HashMap<PDGNode, HashSet<PDGNode>> matchedNodesLists = new HashMap<>();
        for (Map.Entry<String, List<PDGEdge>> entry : patternOutEdges.entrySet()){
            List<PDGEdge> pdgEdgesCode = codeOutEdges.get(entry.getKey());
            for (PDGEdge patternEdge : entry.getValue()) {
                for (PDGEdge codeEdge : pdgEdgesCode) {
                    if (isEqualNodes(codeEdge.getTarget(),patternEdge.getTarget())){
                        if (matchedNodesLists.get(patternEdge.getTarget())==null){
                            HashSet<PDGNode> nodeList= new HashSet<>();
                            nodeList.add(codeEdge.getTarget());
                            matchedNodesLists.put(patternEdge.getTarget(),nodeList);
                        }
                        else {
                            matchedNodesLists.get(patternEdge.getTarget()).add(codeEdge.getTarget());
                        }
//                        visitedNodes.add(codeEdge.getTarget());
                    }
                }
            }
        }
        return matchedNodesLists;
    }

    private HashMap<PDGNode, HashSet<PDGNode>> getMatchersForInPaths(HashSet<PDGNode> visitedNodes, Map<String, List<PDGEdge>> codeOutEdges, Map<String, List<PDGEdge>> patternOutEdges) {
        HashMap<PDGNode, HashSet<PDGNode>> matchedNodesLists= new HashMap<>();
        for (Map.Entry<String, List<PDGEdge>> entry : patternOutEdges.entrySet()){
            List<PDGEdge> pdgEdgesCode = codeOutEdges.get(entry.getKey());
            for (PDGEdge patternEdge : entry.getValue()) {
                if (pdgEdgesCode!=null){
                    for (PDGEdge codeEdge : pdgEdgesCode) {
                        if (isEqualNodes(codeEdge.getSource(),patternEdge.getSource())){
                            if (matchedNodesLists.get(patternEdge.getSource())==null){
                                HashSet<PDGNode> nodeList= new HashSet<>();
                                nodeList.add(codeEdge.getSource());
                                matchedNodesLists.put(patternEdge.getSource(),nodeList);
                            }
                            else {
                                matchedNodesLists.get(patternEdge.getSource()).add(codeEdge.getSource());
                            }
//                        visitedNodes.add(codeEdge.getSource());
                        }
                    }
                }
            }
        }
        return matchedNodesLists;
    }

    private boolean isEqualNodes(PDGNode codeNode, PDGNode patternNode){
        boolean equalNodes = codeNode.isEqualNodes(patternNode);
        if (equalNodes){
            Map<String, Pair<List<PDGEdge>,List<PDGEdge>>> inEdges = getTheMappedEdges(codeNode.getInEdges(), patternNode.getInEdges());

            if (inEdges!=null){
                if (!isChildNodeEquivalant(inEdges,PDGEdge::getSource)){
                    return false;
                }
                Map<String, Pair<List<PDGEdge>,List<PDGEdge>>> outEdges = getTheMappedEdges(codeNode.getOutEdges(), patternNode.getOutEdges());
                if(outEdges!=null){
                    if (!isChildNodeEquivalant(outEdges,PDGEdge::getTarget)){
                        return false;
                    }
                    return true;
                }

            }

        }
        return false;
    }

    private boolean isChildNodeEquivalant(Map<String, Pair<List<PDGEdge>,List<PDGEdge>>> edgeGroups,Function<PDGEdge, PDGNode> func){
        for (Map.Entry<String, Pair<List<PDGEdge>, List<PDGEdge>>> pairEntry : edgeGroups.entrySet()) {
            List<PDGNode> codeNodes = pairEntry.getValue().fst.stream().map(func).collect(Collectors.toList());
            List<PDGNode> patternNodes = pairEntry.getValue().snd.stream().map(func).collect(Collectors.toList());
            Map<PDGNode,HashSet<PDGNode>> matchedNodes = new HashMap<>();
            for (PDGNode pNode : patternNodes) {
                for (PDGNode cNode : codeNodes) {
                    if (pNode.isEqualNodes(cNode)){
                        if (matchedNodes.get(pNode)==null){
                            HashSet<PDGNode> set=new HashSet<>();
                            set.add(cNode);
                            matchedNodes.put(pNode,set);
                        }
                        else{
                            matchedNodes.get(pNode).add(cNode);
                        }
                    }
                }
            }
            if (matchedNodes.size()<patternNodes.size()){
                return false;
            }
            for (int i =0;i<matchedNodes.size();i++){

                    //TODO extend this to get all the unique matched child nodes in the code and check whether each pattern child node match to code child nodes

            }
        }
        return true;
    }

    private List<List<PDGNode>> getAllNodePermutations(List<HashSet<PDGNode>> nodeList, int i){
        List<List<PDGNode>> perMutations= new ArrayList<>();
        if (i>1){
            for (PDGNode node : nodeList.get(i)) {
                List<PDGNode> list = new ArrayList<>();
                list.add(node);
                for (PDGNode cNode : nodeList.get(i-1)) {
                    list.add(cNode);
                    perMutations.add(list);
                }
            }
        }
        return perMutations;
    }

    private Map<String, Pair<List<PDGEdge>,List<PDGEdge>>> getTheMappedEdges(ArrayList<PDGEdge> fullEdges, ArrayList<PDGEdge> subset){
        Map<String, Pair<List<PDGEdge>,List<PDGEdge>>> compared_nodes=new HashMap<>();
        if (subset.size()==0){
            compared_nodes.put("EMPTY", Pair.make(new ArrayList<>(),new ArrayList<>()));
            return compared_nodes;
        }
        Map<String, List<PDGEdge>> fullEdgesGroup
                = fullEdges.stream()
                .collect(Collectors.groupingBy(PDGEdge::getLabel));
        Map<String, List<PDGEdge>> subsetGroup
                = subset.stream()
                .collect(Collectors.groupingBy(PDGEdge::getLabel));

        subsetGroup.forEach((k,v)->{
            if (fullEdgesGroup.get(k)!=null&&fullEdgesGroup.get(k).size()>=v.size()){
                compared_nodes.put(k,Pair.make(fullEdgesGroup.get(k),subsetGroup.get(k)));
            }
        });

        for (String pathLabel : subsetGroup.keySet()) {
            if (!compared_nodes.containsKey(pathLabel)){
                return null;
            }
        }
        return compared_nodes;
    }

    public ArrayList<PDGNode> getCodePDGNodes(){
        ArrayList<PDGNode> nodes = new ArrayList<>();
        nodes.add(codeNode);
        matchedChildNodes.forEach(x->nodes.addAll(x.getCodePDGNodes()) );
        return nodes;
    }

    public ArrayList<PDGNode> getPatternPDGNodes(){
        ArrayList<PDGNode> nodes = new ArrayList<>();
        nodes.add(patternNode);
        matchedChildNodes.forEach(x->nodes.addAll(x.getPatternPDGNodes()) );
        return nodes;
    }

    public ArrayList<MatchedNode> getAllMatchedNodes(){
        ArrayList<MatchedNode> nodes = new ArrayList<>();
        nodes.add(this);
        matchedChildNodes.forEach(x->nodes.addAll(x.getAllMatchedNodes()) );
        return nodes;
    }

    public ArrayList<PDGNode> getMatchedCodeNodes(PDGNode patternNode){
        ArrayList<PDGNode> matchedNodes = new ArrayList<>();
        if (patternNode==this.patternNode){
            matchedNodes.add(codeNode);
        }
        matchedChildNodes.forEach(x->matchedNodes.addAll(x.getMatchedCodeNodes(patternNode)));
        return matchedNodes;
    }

    public void updateCodeAndPatternNodes(){
        if (patternNode.getProperty(NODE_PROPERTIES.SWAPPED)==SWAPPED.NO){
            patternNode= (PDGNode) patternNode.getProperty(NODE_PROPERTIES.CLONE);
            patternNode.setProperty(NODE_PROPERTIES.SWAPPED,SWAPPED.YES);
        }
        if (codeNode.getProperty(NODE_PROPERTIES.SWAPPED)==SWAPPED.NO){
            codeNode= (PDGNode) codeNode.getProperty(NODE_PROPERTIES.CLONE);
            codeNode.setProperty(NODE_PROPERTIES.SWAPPED,SWAPPED.YES);
        }
        matchedChildNodes.forEach(MatchedNode::updateCodeAndPatternNodes);
    }
}
