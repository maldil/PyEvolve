package com.adaptrule;

import com.matching.fgpdg.MatchedNode;
import com.matching.fgpdg.nodes.Guards;
import com.matching.fgpdg.nodes.PDGNode;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.*;
import org.python.antlr.ast.Module;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;
import org.python.core.PyObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdaptRule {
    MatchedNode graph;
    FunctionDef targetCodeAST;
    Module rhsAST;
    Map<PythonTree, Hole> nameToHole;
    public AdaptRule(MatchedNode graph, FunctionDef targetCodeAST, Module rpatternModule) {
        this.graph = graph;
        this.targetCodeAST = targetCodeAST;
        this.rhsAST = rpatternModule;
    }

    public Rule getAdaptedRule() {
        Rule rule=new Rule();
        FunctionDef lhsSubstitutedCode = substituteLHStoTargetCode();
        System.out.println(lhsSubstitutedCode);
        FunctionDef renamedNames = renameRestOfTheRenamedVarsWithHoles(lhsSubstitutedCode);
        FunctionDef lhs = normalizeLHSContext(renamedNames);
        System.out.println(lhs);
        List<PyObject> collect = this.graph.getAllMatchedNodes().stream().map(MatchedNode::getPatternNode).
                map(PDGNode::getAstNode).collect(Collectors.toList());
        collect.addAll(this.graph.getAllMatchedNodes().stream().map(MatchedNode::getCodeNode).
                map(PDGNode::getAstNode).collect(Collectors.toList()));
        rule.setLHS(lhs.getInternalBody().stream().map(Object::toString).collect(Collectors.joining("\n")));
        FunctionDef rhs = createRHS(renamedNames,rhsAST,collect);
        rule.setRHS(rhs.getInternalBody().stream().map(Object::toString).collect(Collectors.joining("\n")));
//        System.out.println(getFunctionDef(rhs).toString());
        return rule;
    }

    public FunctionDef getFunctionDef(Module md){
        for (stmt stmt : md.getInternalBody()) {
            if (stmt instanceof FunctionDef)
                return (FunctionDef)stmt;
        }
        return null;
    }

    private FunctionDef createRHS(FunctionDef lhs, Module rhs, List<PyObject> matchedNode) {
        FindDeletesFromLHS deletes = new FindDeletesFromLHS(matchedNode);
        try {
            deletes.visit(lhs);
            while (true){
                PythonTree updateTree = checkFinalDeleteNodeIsAChildOfOtherDeletes(deletes.deletes,deletes.finalDeletedNode);
                if (updateTree==deletes.finalDeletedNode)
                    break;
                else{
                    if (updateTree instanceof Call && updateTree.getParent()!=null && updateTree.getParent() instanceof Expr){
                        deletes.finalDeletedNode=updateTree.getParent();
                    }
                    else{
                        deletes.finalDeletedNode=updateTree;
                    }



                }
            }

//            List<PythonTree> deletesCopy = deletes.deletes;
//            for (PythonTree de1 : deletes.deletes) {
//                for (PythonTree de2 : deletes.deletes) {
//                    if (de1!=de2 && Util.isChildNode(de1,de2)){
//                        deletesCopy.remove(de1);
//                    }
//                }
//            }
            DeleteAndUpdateVisitor updator = new DeleteAndUpdateVisitor(deletes.deletes,deletes.finalDeletedNode,rhs);
            updator.visit(lhs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lhs;
    }

    private PythonTree checkFinalDeleteNodeIsAChildOfOtherDeletes(List<PythonTree> childs,PythonTree finalNode){
        for (PythonTree tree : childs) {
            for (PythonTree child : tree.getChildren()) {
                if (child==finalNode){
                    return tree;
                 }
            }
        }
        return finalNode;
    }

    private FunctionDef normalizeLHSContext(FunctionDef code){
        HoleSearcher searcher = new HoleSearcher();
        try {
            searcher.visit(code);
            LHSNormalizer normalizer = new LHSNormalizer(searcher.getLargestHoleID()+1);
            normalizer.visit(code);
            RenameLHSVisitor renameLHSVisitor = new RenameLHSVisitor();
            renameLHSVisitor.setRenameChild(normalizer.codeAndHole);
            renameLHSVisitor.visit(code);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    private FunctionDef substituteLHStoTargetCode(){
        Map<PythonTree, List<PythonTree>> codeAndParaNode = new HashMap<>();
        for (MatchedNode matchedNode : graph.getAllMatchedNodes()) {
            PythonTree pASTNode = (PythonTree)matchedNode.getPatternNode().getAstNode();
            if (pASTNode!=null)
                pASTNode.isPatternNode=true;
            if (matchedNode.getCodeNode().getAstNode() instanceof stmt){
                continue;
            }
            if (codeAndParaNode.containsKey(matchedNode.getCodeNode().getAstNode())){
                codeAndParaNode.get(matchedNode.getCodeNode().getAstNode()).add(pASTNode);
            }
            else{
                List<PythonTree> matchedParaN = new ArrayList<>();
                matchedParaN.add(pASTNode);
                codeAndParaNode.put((PythonTree) matchedNode.getCodeNode().getAstNode(),matchedParaN);
            }
        }
        RenameLHSVisitor renameLHSVisitor = new RenameLHSVisitor();
        renameLHSVisitor.setRenameChild(codeAndParaNode);

        try {
            renameLHSVisitor.visit(targetCodeAST);
            nameToHole=renameLHSVisitor.getNameToHole();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return targetCodeAST;
    }

    private FunctionDef renameRestOfTheRenamedVarsWithHoles(FunctionDef targetCode){
        Map<PythonTree, List<PythonTree>> codeAndParaNode = new HashMap<>();
        CollectChangedNames changedNames = new CollectChangedNames(nameToHole.keySet());
        try {
            Map<PythonTree, List<PythonTree>> nodeAndHoleToRename = new HashMap<>();
            changedNames.visit(targetCode);
            for (Map.Entry<PythonTree, List<PythonTree>> entry : changedNames.getMatchedOtherNodes().entrySet()) {
                for (PythonTree tree : entry.getValue()) {
                    nodeAndHoleToRename.put(tree, List.of(nameToHole.get(entry.getKey())));
                }
            }
            RenameLHSVisitor renameLHSVisitor = new RenameLHSVisitor();
            renameLHSVisitor.setRenameChild(nodeAndHoleToRename);
            try {
                renameLHSVisitor.visit(targetCode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return targetCode;

    }

//    private Module RenameTemplateVars(List<PyObject> matchedCodeNodes, Module targetCodeAST){
//
//    }



}
