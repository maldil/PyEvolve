package com.matching.fgpdg;

import com.ibm.wala.util.collections.Pair;
import com.matching.fgpdg.nodes.PDGEdge;
import com.matching.fgpdg.nodes.PDGNode;

import java.util.*;
import java.util.stream.Collectors;

public class MatchedNode {
    protected PDGNode codeNode;
    protected PDGNode patternNode;
    protected HashSet<MatchedNode> matchedChildNodes = new HashSet<>();
    public MatchedNode(PDGNode codeNode, PDGNode patternNode,HashSet<PDGNode> visitedASTNodes) {
        this.codeNode = codeNode;
        this.patternNode = patternNode;
        System.out.println(codeNode+"===="+patternNode);

        Pair<HashMap<PDGNode, HashSet<PDGNode>>, HashMap<PDGNode, HashSet<PDGNode>>> nextMatchedNodePairs
                = getNextMatchedNodePairs(Pair.make(codeNode, patternNode));

        HashSet<PDGNode> parentVisits = new HashSet<>(visitedASTNodes);
        visitedASTNodes.add(codeNode);
        if (nextMatchedNodePairs!=null){
            for (Map.Entry<PDGNode, HashSet<PDGNode>> entry : nextMatchedNodePairs.fst.entrySet()) {
                for (PDGNode node : entry.getValue()) {
                    if (!parentVisits.contains(node)) {
                        matchedChildNodes.add(new MatchedNode(node, entry.getKey(), visitedASTNodes));
                    }
                }
            }
            for (Map.Entry<PDGNode, HashSet<PDGNode>> entry : nextMatchedNodePairs.snd.entrySet()) {
                for (PDGNode node : entry.getValue()) {
                    if (!visitedASTNodes.contains(node)) {
                        matchedChildNodes.add(new MatchedNode(node, entry.getKey(), visitedASTNodes));
                    }
                }
            }
        }

    }

    private Pair<HashMap<PDGNode, HashSet<PDGNode>>, HashMap<PDGNode, HashSet<PDGNode>>>
    getNextMatchedNodePairs(Pair<PDGNode, PDGNode> startNodes) {
        HashMap<PDGNode,HashSet<PDGNode>> matchedOutNodesLists = new HashMap<>();
        HashMap<PDGNode,HashSet<PDGNode>> matchedInNodesLists = new HashMap<>();
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

        getMatchersForOutPaths(matchedOutNodesLists, visitedOutNodes , codeOutEdges, patternOutEdges);
        getMatchersForInPaths(matchedInNodesLists, visitedInNodes, codeInEdges, patternInEdges);

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

    private void getMatchersForOutPaths(HashMap<PDGNode, HashSet<PDGNode>> matchedNodesLists, HashSet<PDGNode> visitedNodes, Map<String, List<PDGEdge>> codeOutEdges, Map<String, List<PDGEdge>> patternOutEdges) {
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
    }

    private void getMatchersForInPaths(HashMap<PDGNode, HashSet<PDGNode>> matchedNodesLists, HashSet<PDGNode> visitedNodes, Map<String, List<PDGEdge>> codeOutEdges, Map<String, List<PDGEdge>> patternOutEdges) {
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
    }

    private boolean isEqualNodes(PDGNode codeNode, PDGNode patternNode){
        boolean equalNodes = codeNode.isEqualNodes(patternNode);
        if (equalNodes){
            Map<String, List<PDGEdge>> inEdges = getTheMappedEdges(codeNode.getInEdges(), patternNode.getInEdges());
            if (inEdges.size()>0){
                Map<String, List<PDGEdge>> outEdges = getTheMappedEdges(codeNode.getOutEdges(), patternNode.getOutEdges());
                if(outEdges.size()>0){
                    return true;
                }
            }
        }
        return false;
    }

    private Map<String, List<PDGEdge>> getTheMappedEdges(ArrayList<PDGEdge> fullEdges, ArrayList<PDGEdge> subset){
        Map<String, List<PDGEdge>> compared_nodes=new HashMap<>();
        if (subset.size()==0){
            compared_nodes.put("EMPTY",new ArrayList<>());
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
                compared_nodes.put(k,fullEdgesGroup.get(k));
            }
        });
        return compared_nodes;
    }

    public ArrayList<PDGNode> getPDGNodes(){
        ArrayList<PDGNode> nodes = new ArrayList<>();
        nodes.add(codeNode);
        matchedChildNodes.forEach(x->nodes.addAll(x.getPDGNodes()) );
        return nodes;
    }

}
