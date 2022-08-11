package com.adaptrule;

import com.matching.fgpdg.MatchedNode;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.Hole;
import org.python.antlr.ast.Module;
import org.python.antlr.ast.Name;
import org.python.antlr.base.expr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdaptRule {
    MatchedNode graph;
    Module targetCodeAST;
    Module rhsAST;
    Map<PythonTree, Hole> nameToHole;
    public AdaptRule(MatchedNode graph, Module targetCodeAST, Module rpatternModule) {
        this.graph = graph;
        this.targetCodeAST = targetCodeAST;
        this.rhsAST = rpatternModule;
    }

    public Module getAdaptedRule() {
        Module lhsSubstitutedCode = substituteLHStoTargetCode();
        System.out.println(lhsSubstitutedCode);
        Module renamedNames = renameRestOfTheRenamedVarsWithHoles(lhsSubstitutedCode);
        Module lhs = normalizeLHSContext(renamedNames);
        System.out.println(lhs);
        Module rhs = createRHS(renamedNames,rhsAST);
        System.out.println(rhs);
        return rhs;
    }

    private Module createRHS(Module lhs, Module rhs) {
        FindDeletesFromLHS deletes = new FindDeletesFromLHS();
        try {
            deletes.visit(lhs);
            while (true){
                PythonTree updateTree= checkFinalDeleteNodeIsAChildOfOtherDeletes(deletes.deletes,deletes.finalDeletedNode);
                if (updateTree==deletes.finalDeletedNode)
                    break;
                else{
                    deletes.finalDeletedNode=updateTree;
                }
            }

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

    private Module normalizeLHSContext(Module code){
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

    private Module substituteLHStoTargetCode(){
        Map<PythonTree, List<PythonTree>> codeAndParaNode = new HashMap<>();
        for (MatchedNode matchedNode : graph.getAllMatchedNodes()) {
            PythonTree pASTNode = (PythonTree)matchedNode.getPatternNode().getAstNode();
            if (pASTNode!=null)
                pASTNode.isPatternNode=true;
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

    private Module renameRestOfTheRenamedVarsWithHoles(Module targetCode){
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
