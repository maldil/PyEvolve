package com.matching.fgpdg;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.ibm.wala.util.collections.Pair;
import com.matching.fgpdg.nodes.*;
import com.utils.Utils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MatchedNode {
    protected PDGNode codeNode;
    protected PDGNode patternNode;
    protected MatchedNode parentNode=null;
    protected HashSet<MatchedNode> matchedChildNodes = new HashSet<>();
    private boolean allChildsMatched=true;
    public MatchedNode(PDGNode codeNode, PDGNode patternNode,HashSet<PDGNode> visitedASTNodes) {
        this.codeNode = codeNode;
        this.patternNode = patternNode;
        this.allChildsMatched=true;
        System.out.println(codeNode+"===="+patternNode);

        Pair<HashMap<PDGNode, HashSet<PDGNode>>, HashMap<PDGNode, HashSet<PDGNode>>> nextMatchedNodePairs
                = getNextMatchedNodePairs(Pair.make(codeNode, patternNode));
        visitedASTNodes.add(codeNode);
        HashSet<PDGNode> parentVisits = new HashSet<>(visitedASTNodes);




        if (nextMatchedNodePairs!=null){
            for (Map.Entry<PDGNode, HashSet<PDGNode>> entry : nextMatchedNodePairs.fst.entrySet()) {
                for (PDGNode node : entry.getValue()) {
                    if (!parentVisits.contains(node)) {
                        MatchedNode matchedNode = new MatchedNode(node, entry.getKey(), visitedASTNodes);
                        matchedNode.setParentNode(this);
                        matchedChildNodes.add(matchedNode);
                    }
                }
            }
            for (Map.Entry<PDGNode, HashSet<PDGNode>> entry : nextMatchedNodePairs.snd.entrySet()) {
                for (PDGNode node : entry.getValue()) {
                    if (!parentVisits.contains(node)) {
                        MatchedNode matchedNode = new MatchedNode(node, entry.getKey(), visitedASTNodes);
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

            List<PDGNode> codeParaGraphs = codeNode.getInEdges().stream().filter(x -> x.getLabel().equals("para")).map(PDGEdge::getSource).filter(u->!matchedCodeNodes.contains(u)).collect(Collectors.toList());
            codeParaGraphs.addAll(codeNode.getOutEdges().stream().filter(x -> x.getLabel().equals("para")).map(PDGEdge::getTarget).filter(u->!matchedCodeNodes.contains(u)).collect(Collectors.toList()));
            List<PDGNode> patternParaGraphs = patternNode.getInEdges().stream().filter(x -> x.getLabel().equals("para")).map(PDGEdge::getSource).filter(u->!matchedPatternNodes.contains(u)).collect(Collectors.toList());
            patternParaGraphs.addAll(patternNode.getOutEdges().stream().filter(x -> x.getLabel().equals("para")).map(PDGEdge::getTarget).filter(u->!matchedPatternNodes.contains(u)).collect(Collectors.toList()));

            if (codeParaGraphs.size()>0 && patternParaGraphs.size()>0){
                for (PDGNode paraN : patternParaGraphs) {
                    if (matchedChildNodes.stream().noneMatch(x -> x.getPatternNode() == paraN)){
                        for (PDGNode codeN : codeParaGraphs) {
                            List<MatchedNode> subGraphs = getMatchedSubGraphs(codeN, paraN,codeNode);
                            if (subGraphs!=null)
                                matchedChildNodes.addAll(subGraphs);
                        }
                    }
                }
            }
        }
    }

    public void setParentNode(MatchedNode parentNode) {
        this.parentNode = parentNode;
    }

    public PDGGraph getPatternGraphForMatching(PDGNode node,List<PDGNode> avoidNodes){
        try {
            PDGBuildingContext context = new PDGBuildingContext(new ArrayList<>(),"");
            PDGGraph grap = new PDGGraph(context);
            List<PDGNode> nodes = node.getAllChildNodes(20).stream().filter(x -> !avoidNodes.contains(x)).collect(Collectors.toList());
            for (PDGNode pdgNode : nodes) {
                if (pdgNode instanceof PDGDataNode){
                    PDGDataNode dataNode = new PDGDataNode((PDGDataNode) pdgNode);
                    List<PDGEdge> inedges = pdgNode.getInEdges().stream().filter(x -> avoidNodes.contains(x.getSource())).collect(Collectors.toList());
                    dataNode.setInEdges((ArrayList<PDGEdge>) inedges);
                    List<PDGEdge> outedges = pdgNode.getOutEdges().stream().filter(x -> avoidNodes.contains(x.getTarget())).collect(Collectors.toList());
                    dataNode.setOutEdges((ArrayList<PDGEdge>) outedges);
                }
                else {
                    PDGControlNode cNode= new PDGControlNode(pdgNode.getAstNode(),pdgNode.getAstNodeType(),pdgNode.getControl());
                    List<PDGEdge> inedges = pdgNode.getInEdges().stream().filter(x -> avoidNodes.contains(x.getSource())).collect(Collectors.toList());
                    cNode.setInEdges((ArrayList<PDGEdge>) inedges);
                    List<PDGEdge> outedges = pdgNode.getOutEdges().stream().filter(x -> avoidNodes.contains(x.getTarget())).collect(Collectors.toList());
                    cNode.setOutEdges((ArrayList<PDGEdge>) outedges);
                }
            }
            return grap;
        } catch (IOException e) {
            e.printStackTrace();

        }
        return null;
    }

    public List<MatchedNode> getMatchedSubGraphs(PDGNode codeNode, PDGNode patternNode,PDGNode avoidCodeNode){
        ArrayList<Pair<PDGNode,PDGNode>> startNodes = new ArrayList<>();
        List<MatchedNode> subGraphs=null;
        if (patternNode !=null){
            for (PDGNode cnd : codeNode.getAllChildNodes(2,avoidCodeNode)) {
                if (isEqualNodes(cnd, patternNode)){
                    startNodes.add(Pair.make(cnd, patternNode));
                }
            }
            if (startNodes.size()!=0) {
                subGraphs = startNodes.stream().map(x -> new MatchedNode(x.fst, x.snd, new HashSet<>())).collect(Collectors.toList());
                subGraphs.forEach(x->x.updateAllMatchedNodes(x));
            }
        }
        return subGraphs;
    }

    public void updateAllMatchedNodes(MatchedNode matchedGraph){
        List<PDGNode> childNodesogPatternNodes = patternNode.getInEdges().stream().map(PDGEdge::getSource).collect(Collectors.toList());
        childNodesogPatternNodes.addAll(patternNode.getOutEdges().stream().map(PDGEdge::getTarget).collect(Collectors.toList()));
        List<PDGNode> childNodesOfCodeNodes = codeNode.getInEdges().stream().map(PDGEdge::getSource).collect(Collectors.toList());
        childNodesOfCodeNodes.addAll(codeNode.getOutEdges().stream().map(PDGEdge::getTarget).collect(Collectors.toList()));
        for (PDGNode node : childNodesogPatternNodes) {
            List<PDGNode> collect1 = matchedGraph.getAllMatchedNodes().stream().filter(x -> x.getPatternNode() == node).map(MatchedNode::getCodeNode)
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
        matchedChildNodes.forEach(x->x.updateAllMatchedNodes(matchedGraph));
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



}
