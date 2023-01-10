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
import com.utils.FileIO;
import com.utils.Utils;
import io.vavr.control.Try;
import org.python.antlr.ast.*;
import org.python.antlr.ast.Module;
import org.python.antlr.base.stmt;
import org.python.core.PyObject;
import org.w3c.dom.ls.LSOutput;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.utils.Utils.getPathToResources;

public class MainAdaptor {
    public String adaptFunction(List<stmt> imports,Guards guard, Module pLeft,Module pRight, Module function) throws Exception {
        List<MatchedNode> matchedNodes = getMatchedNodes(imports,guard,function, pLeft);
        FunctionDef func = (FunctionDef) function.getInternalBody().stream().filter(x-> x instanceof FunctionDef).findFirst().get();
        List<MatchedNode> allMatchedGraphs = matchedNodes.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList());
        AdaptRule aRule= new AdaptRule(allMatchedGraphs.get(0),Utils.getAllFunctions(pLeft).get(0),pRight);
        BasicCombyOperations op = new BasicCombyOperations();
        Try<CombyRewrite> changedCode = op.rewrite(aRule.getAdaptedRule().getLHS(), aRule.getAdaptedRule().getRHS(), func.toString(), ".python");
        return changedCode.get().getRewrittenSource();

    }

    public static List<MatchedNode> getMatchedNodes(String filename, String lpatternname,  FunctionDef func, List<stmt> importStmt, Module lpatternModule){
        List<MatchedNode> graphs = new ArrayList<>();
        PDGBuildingContext fcontext = null;
        fcontext = Try.of(()-> new PDGBuildingContext(importStmt, filename)).onFailure(x-> System.err.println()).get();
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
        Guards guards = new Guards(Try.of(()->com.utils.Utils.getFileContent(lpatternname))
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
        Module codeModule = Utils.getPythonModule(Configurations.PROJECT_REPOSITORY +filename);
        String sourceCode = FileIO.readFile(Configurations.PROJECT_REPOSITORY + filename);
        List<org.python.antlr.base.stmt> imports = codeModule.getInternalBody().stream().filter(x -> x instanceof Import
                || x instanceof ImportFrom).collect(Collectors.toList());
        String adaptedFunction ="";
        Map<Integer,String> functionStarts = new HashMap<>();
        StringBuilder adaptedFile = new StringBuilder();
        List<String> splittedFile = new ArrayList<>();
        int previousStop = 0;
        for (stmt stmt : Utils.getAllFunctions(codeModule)) {
            int charStartIndex = stmt.getCharStartIndex();
            int charStopIndex = ((FunctionDef)stmt).getInternalBody().get(((FunctionDef)stmt).getInternalBody().size()-1).getCharStopIndex();
            adaptedFunction = MainAdaptor.transplantPatternToFunction(filename, (FunctionDef) stmt,imports,LHS, RHS);
            if (!adaptedFunction.equals("")){
                adaptedFile.append(sourceCode, previousStop, charStartIndex).append(adaptedFunction);
                previousStop = charStopIndex;
            }else{
                adaptedFile.append(sourceCode, previousStop, charStopIndex);
                previousStop = charStopIndex;
            }
        }
        adaptedFile.append(sourceCode, previousStop, sourceCode.length()-1);


        return adaptedFile.toString();
    }

    public static String transplantPatternToFunction(String filename,FunctionDef def,List<stmt> imports,String LHS,String RHS ){
        BasicCombyOperations op = new BasicCombyOperations();
        String code = def.toString();
        Module lpatternModule = Utils.getPythonModuleForTemplate(LHS).onFailure(System.err::println).get();
        Module rpatternModule = Utils.getPythonModuleForTemplate(RHS).onFailure(System.err::println).get();

        List<MatchedNode> matchedNodes = getMatchedNodes(filename,LHS,def,imports,lpatternModule);
        List<MatchedNode> allMatchedGraphs = matchedNodes.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList());

        if (allMatchedGraphs.size()!=0){
            List<PyObject> subtree= Utils.getMatchedASTSubGraph(allMatchedGraphs.get(0), def);
            List<PyObject> continuousStmts = Utils.getContinousStatments(subtree);
            FunctionDef newdef = new FunctionDef();


            AdaptRule aRule = new AdaptRule(allMatchedGraphs.get(0), def , rpatternModule);
            Rule rule = aRule.getAdaptedRule();

            Try<CombyRewrite> changedCode = op.rewrite(rule.getLHS(), rule.getRHS(), code, ".python");
            return changedCode.get().getRewrittenSource().strip();
        }
        return "";

    }
}
