package com.matching.fgpdg;

import com.ibm.wala.util.collections.Pair;
import com.matching.fgpdg.nodes.*;
import com.utils.DotGraph;
import com.utils.Utils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static com.matching.fgpdg.Configurations.CREATE_DEBUG_IMAGES;

public class MatchPDG {
    public HashSet<PDGNode> visitedASTNodes= new HashSet<>();
    PDGBuildingContext patternContext;PDGBuildingContext codeContext;
    public List<MatchedNode> getSubGraphs(PDGGraph pattern, PDGGraph code,PDGBuildingContext patternContext,PDGBuildingContext cContext )  {
        this.patternContext=patternContext;
        this.codeContext=cContext;
        PDGGraph _pattern=pruneAndCleanPatternPDG(pattern);
        HashSet<PDGNode> patternNodes = _pattern.getNodes();
        DotGraph dg = new DotGraph(_pattern);
        if (CREATE_DEBUG_IMAGES) {
            dg.toDotFile(new File("./OUTPUT/"  +"____pruned_pattern__file___"+".dot"));
            DotGraph dgc = new DotGraph(code);
            dgc.toDotFile(new File("./OUTPUT/"  +"____code__file___"+".dot"));
        }
        HashSet<PDGNode> codeNodes = code.getNodes();
        ArrayList<Pair<PDGNode,PDGNode>> startNodes = new ArrayList<>();
        PDGNode maxDOF = Utils.getMaxDOF(patternNodes);
        if (maxDOF!=null){
            for (PDGNode codeNode : codeNodes) {
                    if (isEqualNodes(codeNode,maxDOF)){
                        startNodes.add(Pair.make(codeNode,maxDOF));
                    }
            }
            if (startNodes.size()!=0) {
                return startNodes.stream().map(x-> new MatchedNode(x.fst,x.snd,new HashSet<>(),patternContext,cContext)).collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    protected List<MatchedNode> getSubGraphs(PDGGraph pattern, PDGGraph code,PDGNode startNode){
        PDGGraph _pattern=pruneAndCleanPatternPDG(pattern);
        HashSet<PDGNode> patternNodes = _pattern.getNodes();
//        DotGraph dg = new DotGraph(_pattern);
//        dg.toDotFile(new File("./OUTPUT/"  +"____pruned_pattern__file___"+".dot"));
//        DotGraph dgc = new DotGraph(code);
//        dgc.toDotFile(new File("./OUTPUT/"  +"____code__file___"+".dot"));


        HashSet<PDGNode> codeNodes = code.getNodes();
        ArrayList<Pair<PDGNode,PDGNode>> startNodes = new ArrayList<>();
        ArrayList<ArrayList<PDGNode>> matched = new ArrayList<>();
        if (startNode !=null){
            for (PDGNode codeNode : codeNodes) {
                if (isEqualNodes(codeNode, startNode)){
                    startNodes.add(Pair.make(codeNode, startNode));
                }
            }
            if (startNodes.size()!=0) {
                return startNodes.stream().map(x-> new MatchedNode(x.fst,x.snd,new HashSet<>(),this.patternContext,this.codeContext)).collect(Collectors.toList());
            }
        }
        return null;

    }


    public PDGGraph pruneAndCleanPatternPDG(PDGGraph pattern) {
        ArrayList<PDGNode> remove = new ArrayList<>();
        for (PDGNode node : pattern.nodes) {
            if (node instanceof PDGActionNode && node.getLabel().equals("empty")){
                remove.add(node);
                for (PDGEdge inEdge : node.getInEdges()) {
                    inEdge.getSource().getOutEdges().remove(inEdge);
                }
                for (PDGEdge outEdge : node.getOutEdges()) {
                    outEdge.getTarget().getInEdges().remove(outEdge);
                }
            }
            else if (node instanceof PDGDataNode && node.getLabel().equals("self(self)")){
                remove.add(node);
                for (PDGEdge inEdge : node.getInEdges()) {
                    inEdge.getSource().getOutEdges().remove(inEdge);
                }
                for (PDGEdge outEdge : node.getOutEdges()) {
                    outEdge.getTarget().getInEdges().remove(outEdge);
                }
            }
            List<PDGEdge> depEdges = node.getInEdges().stream().
                    filter(x -> x instanceof PDGDataEdge && ((PDGDataEdge) x).getType().equals(PDGDataEdge.Type.DEPENDENCE)).collect(Collectors.toList());
            node.getInEdges().removeAll(depEdges);
            for (PDGEdge edge : depEdges) {
                edge.getSource().getOutEdges().remove(edge);
            }


        }
        remove.forEach(k-> pattern.nodes.remove(k));
        return pattern;
    }

    private HashMap<PDGNode, ArrayList<PDGNode>> getNextMatchedNodePairs(Pair<PDGNode, PDGNode> startNodes) {
        HashMap<PDGNode,ArrayList<PDGNode>> matchedNodesLists = new HashMap<>();
        HashSet<PDGNode> visitedNodes = new HashSet<>();
        Map<String, List<PDGEdge>> codeOutEdges = startNodes.fst.getOutEdges().stream()
                    .collect(Collectors.groupingBy(PDGEdge::getLabel));
        Map<String, List<PDGEdge>> patternOutEdges = startNodes.snd.getOutEdges().stream()
                    .collect(Collectors.groupingBy(PDGEdge::getLabel));

        for (Map.Entry<String, List<PDGEdge>> entry : patternOutEdges.entrySet()){
                List<PDGEdge> pdgEdgesCode = codeOutEdges.get(entry.getKey());
                for (PDGEdge patternEdge : entry.getValue()) {
                    for (PDGEdge codeEdge : pdgEdgesCode) {
                        if (!visitedNodes.contains(codeEdge.getTarget())  && isEqualNodes(codeEdge.getTarget(),patternEdge.getTarget())){
                            if (matchedNodesLists.get(patternEdge.getTarget())==null){
                                ArrayList<PDGNode> nodeList= new ArrayList<>();
                                nodeList.add(codeEdge.getTarget());
                                matchedNodesLists.put(patternEdge.getTarget(),nodeList);
                            }
                            else {
                                matchedNodesLists.get(patternEdge.getTarget()).add(codeEdge.getTarget());
                            }
                            visitedNodes.add(codeEdge.getTarget());
                        }
                    }
                }
        }
        for (PDGNode node : startNodes.snd.getOutEdges().stream().map(PDGEdge::getTarget).collect(Collectors.toList())) {
            if (!matchedNodesLists.containsKey(node)){
                return null;
            }
        }
        return matchedNodesLists;
    }

    private HashMap<PDGNode, ArrayList<PDGNode>> getNextMatchedNodePairs(ArrayList<Pair<PDGNode, PDGNode>> startNodes) {
        ArrayList<Pair<PDGNode,PDGNode>> matchedNodes = new ArrayList<>();
        HashMap<PDGNode,ArrayList<PDGNode>> matchedNodesLists = new HashMap<>();
        HashSet<PDGNode> visitedNodes = new HashSet<>();
        for (Pair<PDGNode, PDGNode> nodePairs : startNodes) {
            Map<String, List<PDGEdge>> codeOutEdges = nodePairs.fst.getOutEdges().stream()
                    .collect(Collectors.groupingBy(PDGEdge::getLabel));

            Map<String, List<PDGEdge>> patternOutEdges = nodePairs.snd.getOutEdges().stream()
                    .collect(Collectors.groupingBy(PDGEdge::getLabel));

            for (Map.Entry<String, List<PDGEdge>> entry : patternOutEdges.entrySet()){
                List<PDGEdge> pdgEdgesCode = codeOutEdges.get(entry.getKey());
                for (PDGEdge patternEdge : entry.getValue()) {
                    for (PDGEdge codeEdge : pdgEdgesCode) {
                        if (!visitedNodes.contains(codeEdge.getTarget())  && isEqualNodes(codeEdge.getTarget(),patternEdge.getTarget())){
                            matchedNodes.add(Pair.make(codeEdge.getTarget(),patternEdge.getTarget()));
                            if (matchedNodesLists.get(patternEdge.getTarget())==null){
                                ArrayList<PDGNode> nodeList= new ArrayList<>();
                                nodeList.add(codeEdge.getTarget());
                                matchedNodesLists.put(patternEdge.getTarget(),nodeList);
                            }
                            else {
                                matchedNodesLists.get(patternEdge.getTarget()).add(patternEdge.getTarget());
                            }
                            visitedNodes.add(codeEdge.getTarget());
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
        for (String edgeLabel : subsetGroup.keySet()) {
            if (!compared_nodes.containsKey(edgeLabel) ){
                return new HashMap<>();
            }
        }
        return compared_nodes;
    }

    public void drawMatchedGraphs(PDGGraph fpdg, List<MatchedNode> graphs,String fileName) {
        DotGraph dg = new DotGraph(fpdg,graphs);
        dg.toDotFile(new File( fileName));
    }
}
