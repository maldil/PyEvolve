package com;

import com.adaptrule.AdaptRule;
import com.inferrules.comby.jsonResponse.CombyRewrite;
import com.inferrules.comby.operations.BasicCombyOperations;
import com.inferrules.core.RewriteRule;
import com.inferrules.core.languageAdapters.Language;
import com.matching.fgpdg.MatchPDG;
import com.matching.fgpdg.MatchedNode;
import com.matching.fgpdg.PDGBuildingContext;
import com.matching.fgpdg.PDGGraph;
import com.matching.fgpdg.nodes.Guards;
import com.matching.fgpdg.nodes.TypeInfo.TypeWrapper;
import io.vavr.control.Try;
import org.python.antlr.ast.*;
import org.python.antlr.ast.Module;
import org.python.antlr.base.stmt;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class MainAdaptor {
    public String adaptFunction(List<stmt> imports,Guards guard, Module pLeft,Module pRight, Module function) throws Exception {
        List<MatchedNode> matchedNodes = getMatchedNodes(imports,guard,function, pLeft);
        FunctionDef func = (FunctionDef) function.getInternalBody().stream().filter(x-> x instanceof FunctionDef).findFirst().get();
        List<MatchedNode> allMatchedGraphs = matchedNodes.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList());
        AdaptRule aRule= new AdaptRule(allMatchedGraphs.get(0),pLeft,pRight);
        BasicCombyOperations op = new BasicCombyOperations();
        Try<CombyRewrite> changedCode = op.rewrite(aRule.getAdaptedRule().getLHS(), aRule.getAdaptedRule().getRHS(), func.toString(), ".python");
        return changedCode.get().getRewrittenSource();

    }

    public static List<MatchedNode> getMatchedNodes(List<stmt> imports,Guards guard, Module codeModule, Module lpatternModule) throws IOException {
        List<MatchedNode> graphs;
        FunctionDef func=null;
        for (org.python.antlr.base.stmt stmt : codeModule.getInternalBody()) {
            if (stmt instanceof FunctionDef){
                func= (FunctionDef) stmt;
                break;
            }
            else if (stmt instanceof ClassDef){
                for (org.python.antlr.base.stmt stmt1 : ((ClassDef) stmt).getInternalBody()) {
                    if (stmt1 instanceof FunctionDef){
                        func= (FunctionDef) stmt1;
                        break;
                    }
                }

            }
        }
        PDGBuildingContext fcontext = null;
        fcontext = new PDGBuildingContext(imports, "");
        PDGGraph fpdg = new PDGGraph(func,fcontext);
        TypeWrapper wrapper = new TypeWrapper(guard);
        PDGBuildingContext mcontext = new PDGBuildingContext(lpatternModule.getInternalBody().stream().filter(x -> x instanceof Import
                || x instanceof ImportFrom).collect(Collectors.toList()),wrapper);
        PDGGraph mpdg = new PDGGraph(lpatternModule,mcontext);
        MatchPDG match = new MatchPDG();
        graphs=match.getSubGraphs(mpdg,fpdg,mcontext,fcontext );
        graphs.forEach(x->x.updateAllMatchedNodes(x,mpdg));
        List<MatchedNode> finalPatterns = graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList());
        return finalPatterns;
    }

}
