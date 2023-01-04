package com;

import com.adaptrule.AdaptRule;
import com.adaptrule.Rule;
import com.inferrules.comby.jsonResponse.CombyRewrite;
import com.inferrules.comby.operations.BasicCombyOperations;
import com.inferrules.core.RewriteRule;
import com.inferrules.core.languageAdapters.Language;
import com.matching.fgpdg.*;
import com.matching.fgpdg.nodes.Guards;
import com.matching.fgpdg.nodes.TypeInfo.TypeWrapper;
import com.utils.Utils;
import io.vavr.control.Try;
import org.python.antlr.ast.*;
import org.python.antlr.ast.Module;
import org.python.antlr.base.stmt;
import org.w3c.dom.ls.LSOutput;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static com.utils.Utils.getPathToResources;

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

    public static List<MatchedNode> getMatchedNodes(String filename, String lpatternname,  Module codeModule, Module lpatternModule){
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
        fcontext = Try.of(()-> new PDGBuildingContext(codeModule.getInternalBody().stream().filter(x-> x instanceof Import
                || x instanceof ImportFrom).collect(Collectors.toList()), filename)).onFailure(x-> System.err.println()).get();
        PDGGraph fpdg = new PDGGraph(func,fcontext);
//        fpdg.getNodes().forEach(x-> System.out.println(x.getId()));
        Guards guards = new Guards(Try.of(()->com.utils.Utils.getFileContent(getPathToResources(lpatternname)))
                .onFailure(System.out::println).get(),lpatternModule);
        TypeWrapper wrapper = new TypeWrapper(guards);
        PDGBuildingContext mcontext = new PDGBuildingContext(lpatternModule.getInternalBody().stream().filter(x -> x instanceof Import
                || x instanceof ImportFrom).collect(Collectors.toList()),wrapper);
        PDGGraph mpdg = new PDGGraph(lpatternModule,mcontext);
        MatchPDG match = new MatchPDG();
        graphs=match.getSubGraphs(mpdg,fpdg,mcontext,fcontext );
        graphs.forEach(x->x.updateAllMatchedNodes(x,mpdg));
        List<MatchedNode> finalPatterns = graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList());

        Try.of(()->Files.createDirectories(Paths.get(new File("OUTPUT/matches/"+filename.
                substring(0, filename.lastIndexOf('.'))+".dot").getParent() ))).onFailure(System.err::println);

        match.drawMatchedGraphs(fpdg,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()),
                "OUTPUT/matches/"+filename.substring(0, filename.lastIndexOf('.'))+".dot");
        com.utils.Utils.markNodesInCode(Configurations.PROJECT_REPOSITORY+ filename,
                graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()),"OUTPUT/matches/"+
                        filename.substring(0, filename.lastIndexOf('.'))+".html","","x@x");
        return graphs;
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

    public static String transplantPatternToFile(String filename, String LHS,String RHS) {
        BasicCombyOperations op = new BasicCombyOperations();
        Module codeModule = Utils.getPythonModule(filename);
        String code = codeModule.getInternalBody().get(1).toString();
        Module lpatternModule = Utils.getPythonModuleForTemplate(LHS).onFailure(System.err::println).get();
        Module rpatternModule = Utils.getPythonModuleForTemplate(RHS).onFailure(System.err::println).get();
        List<MatchedNode> matchedNodes = getMatchedNodes(filename, LHS, codeModule, lpatternModule);
        List<MatchedNode> allMatchedGraphs = matchedNodes.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList());
        AdaptRule aRule = new AdaptRule(allMatchedGraphs.get(0), codeModule, rpatternModule);
        Rule rule = aRule.getAdaptedRule();
        Try<CombyRewrite> changedCode = op.rewrite(rule.getLHS(), rule.getRHS(), code, ".python");
        return changedCode.get().getRewrittenSource();
    }
}
