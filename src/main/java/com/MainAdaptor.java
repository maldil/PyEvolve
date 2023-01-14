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
import org.antlr.runtime.Token;
import org.python.antlr.ast.*;
import org.python.antlr.ast.Module;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;
import org.python.core.PyObject;
import org.w3c.dom.ls.LSOutput;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.utils.Utils.getPathToResources;

public class MainAdaptor {
    public String adaptFunction(List<stmt> imports, Guards guard, Module pLeft, Module pRight, Module function) throws Exception {
        List<MatchedNode> matchedNodes = getMatchedNodes(imports, guard, function, pLeft);
        FunctionDef func = (FunctionDef) function.getInternalBody().stream().filter(x -> x instanceof FunctionDef).findFirst().get();
        List<MatchedNode> allMatchedGraphs = matchedNodes.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList());
        AdaptRule aRule = new AdaptRule(allMatchedGraphs.get(0), Utils.getAllFunctions(pLeft).get(0), pRight);
        BasicCombyOperations op = new BasicCombyOperations();
        Try<CombyRewrite> changedCode = op.rewrite(aRule.getAdaptedRule().getLHS(), aRule.getAdaptedRule().getRHS(), func.toString(), ".python");
        return changedCode.get().getRewrittenSource();

    }

    public static List<MatchedNode> getMatchedNodes(String filename, String lpatternname, FunctionDef func, List<stmt> importStmt, Module lpatternModule) {
        List<MatchedNode> graphs = new ArrayList<>();
        PDGBuildingContext fcontext = null;
        fcontext = Try.of(() -> new PDGBuildingContext(importStmt, filename)).onFailure(x -> System.err.println()).get();
        PDGGraph fpdg = new PDGGraph(func, fcontext);
//        fpdg.getNodes().forEach(x-> System.out.println(x.getId()));
        Guards guards = new Guards(Try.of(() -> com.utils.Utils.getFileContent(getPathToResources(lpatternname)))
                .onFailure(System.out::println).get(), lpatternModule);
        TypeWrapper wrapper = new TypeWrapper(guards);
        PDGBuildingContext mcontext = new PDGBuildingContext(lpatternModule.getInternalBody().stream().filter(x -> x instanceof Import
                || x instanceof ImportFrom).collect(Collectors.toList()), wrapper);
        PDGGraph mpdg = new PDGGraph(lpatternModule, mcontext);
        MatchPDG match = new MatchPDG();
        graphs = match.getSubGraphs(mpdg, fpdg, mcontext, fcontext);
        graphs.forEach(x -> x.updateAllMatchedNodes(x, mpdg));
        List<MatchedNode> finalPatterns = graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList());

        Try.of(() -> Files.createDirectories(Paths.get(new File("OUTPUT/matches/" + filename.
                substring(0, filename.lastIndexOf('.')) + ".dot").getParent()))).onFailure(System.err::println);

        match.drawMatchedGraphs(fpdg, graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()),
                "OUTPUT/matches/" + filename.substring(0, filename.lastIndexOf('.')) + ".dot");
        com.utils.Utils.markNodesInCode(Configurations.PROJECT_REPOSITORY + filename,
                graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()), "OUTPUT/matches/" +
                        filename.substring(0, filename.lastIndexOf('.')) + ".html", "", "x@x");
        return graphs;
    }


    public static List<MatchedNode> getMatchedNodes(String filename, String lpatternname, Module codeModule, Module lpatternModule) {
        List<MatchedNode> graphs;
        FunctionDef func = null;
        for (org.python.antlr.base.stmt stmt : codeModule.getInternalBody()) {
            if (stmt instanceof FunctionDef) {
                func = (FunctionDef) stmt;
                break;
            } else if (stmt instanceof ClassDef) {
                for (org.python.antlr.base.stmt stmt1 : ((ClassDef) stmt).getInternalBody()) {
                    if (stmt1 instanceof FunctionDef) {
                        func = (FunctionDef) stmt1;
                        break;
                    }
                }

            }
        }
        PDGBuildingContext fcontext = null;
        fcontext = Try.of(() -> new PDGBuildingContext(codeModule.getInternalBody().stream().filter(x -> x instanceof Import
                || x instanceof ImportFrom).collect(Collectors.toList()), filename)).onFailure(x -> System.err.println()).get();
        PDGGraph fpdg = new PDGGraph(func, fcontext);
//        fpdg.getNodes().forEach(x-> System.out.println(x.getId()));
        Guards guards = new Guards(Try.of(() -> com.utils.Utils.getFileContent(lpatternname))
                .onFailure(System.out::println).get(), lpatternModule);
        TypeWrapper wrapper = new TypeWrapper(guards);
        PDGBuildingContext mcontext = new PDGBuildingContext(lpatternModule.getInternalBody().stream().filter(x -> x instanceof Import
                || x instanceof ImportFrom).collect(Collectors.toList()), wrapper);
        PDGGraph mpdg = new PDGGraph(lpatternModule, mcontext);
        MatchPDG match = new MatchPDG();
        graphs = match.getSubGraphs(mpdg, fpdg, mcontext, fcontext);
        graphs.forEach(x -> x.updateAllMatchedNodes(x, mpdg));
        List<MatchedNode> finalPatterns = graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList());

        Try.of(() -> Files.createDirectories(Paths.get(new File("OUTPUT/matches/" + filename.
                substring(0, filename.lastIndexOf('.')) + ".dot").getParent()))).onFailure(System.err::println);

        match.drawMatchedGraphs(fpdg, graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()),
                "OUTPUT/matches/" + filename.substring(0, filename.lastIndexOf('.')) + ".dot");
        com.utils.Utils.markNodesInCode(Configurations.PROJECT_REPOSITORY + filename,
                graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()), "OUTPUT/matches/" +
                        filename.substring(0, filename.lastIndexOf('.')) + ".html", "", "x@x");
        return graphs;
    }

    public static List<MatchedNode> getMatchedNodes(List<stmt> imports, Guards guard, Module codeModule, Module lpatternModule) throws IOException {
        List<MatchedNode> graphs;
        FunctionDef func = null;
        for (org.python.antlr.base.stmt stmt : codeModule.getInternalBody()) {
            if (stmt instanceof FunctionDef) {
                func = (FunctionDef) stmt;
                break;
            } else if (stmt instanceof ClassDef) {
                for (org.python.antlr.base.stmt stmt1 : ((ClassDef) stmt).getInternalBody()) {
                    if (stmt1 instanceof FunctionDef) {
                        func = (FunctionDef) stmt1;
                        break;
                    }
                }

            }
        }
        PDGBuildingContext fcontext = null;
        fcontext = new PDGBuildingContext(imports, "");
        PDGGraph fpdg = new PDGGraph(func, fcontext);
        TypeWrapper wrapper = new TypeWrapper(guard);
        PDGBuildingContext mcontext = new PDGBuildingContext(lpatternModule.getInternalBody().stream().filter(x -> x instanceof Import
                || x instanceof ImportFrom).collect(Collectors.toList()), wrapper);
        PDGGraph mpdg = new PDGGraph(lpatternModule, mcontext);
        MatchPDG match = new MatchPDG();
        graphs = match.getSubGraphs(mpdg, fpdg, mcontext, fcontext);
        graphs.forEach(x -> x.updateAllMatchedNodes(x, mpdg));
        List<MatchedNode> finalPatterns = graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList());
        return finalPatterns;
    }

    public static String transplantPatternToFile(String filename, String LHS, String RHS) {
        BasicCombyOperations op = new BasicCombyOperations();
        Module codeModule = Utils.getPythonModule(Configurations.PROJECT_REPOSITORY + filename);
        String sourceCode = FileIO.readFile(Configurations.PROJECT_REPOSITORY + filename);
        List<org.python.antlr.base.stmt> imports = codeModule.getInternalBody().stream().filter(x -> x instanceof Import
                || x instanceof ImportFrom).collect(Collectors.toList());
        String adaptedFunction = "";
        Map<Integer, String> functionStarts = new HashMap<>();
        StringBuilder adaptedFile = new StringBuilder();
        List<String> splittedFile = new ArrayList<>();
        int previousStop = 0;
        for (stmt stmt : Utils.getAllFunctions(codeModule)) {
            if (((FunctionDef) stmt).getInternalBody().size()>0){
                int charStartIndex = stmt.getCharStartIndex();
                int charStopIndex = ((FunctionDef) stmt).getInternalBody().get(((FunctionDef) stmt).getInternalBody().size() - 1).getCharStopIndex();
                adaptedFunction = MainAdaptor.transplantPatternToFunction(filename, (FunctionDef) stmt, imports, LHS, RHS,sourceCode);

                int numberOfTrailingSpacesAndNewLines = sourceCode.substring(charStartIndex,charStopIndex).length() - sourceCode.substring(charStartIndex,charStopIndex).trim().length();

                if (!adaptedFunction.equals("")) {
                    adaptedFile.append(sourceCode, previousStop, charStartIndex).append(adaptedFunction);
                    previousStop = charStopIndex-numberOfTrailingSpacesAndNewLines;
                } else if(previousStop<=charStopIndex) {
                    adaptedFile.append(sourceCode, previousStop, charStopIndex);
                    previousStop = charStopIndex-numberOfTrailingSpacesAndNewLines;
                }
            }
        }
        adaptedFile.append(sourceCode, previousStop, sourceCode.length() - 1);
        FileIO.writeStringToFile(adaptedFile.toString(),Configurations.PROJECT_REPOSITORY +filename);

        return adaptedFile.toString();
    }

    public static String transplantPatternToFunction(String filename, FunctionDef def, List<stmt> imports, String LHS, String RHS, String codeInFile) {
        BasicCombyOperations op = new BasicCombyOperations();
        String code = def.toString();
        Module lpatternModule = Utils.getPythonModuleForTemplate(LHS).onFailure(System.err::println).get();
        Module rpatternModule = Utils.getPythonModuleForTemplate(RHS).onFailure(System.err::println).get();

        List<MatchedNode> matchedNodes = getMatchedNodes(filename, LHS, def, imports, lpatternModule);
        List<MatchedNode> allMatchedGraphs = matchedNodes.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList());

        if (allMatchedGraphs.size() != 0) {
            List<PyObject> subtree = Utils.getMatchedASTSubGraph(allMatchedGraphs.get(0), def);
            List<stmt> continuousStmts = Utils.getContinousStatments(subtree).stream().map(x -> (stmt) x).collect(Collectors.toList());

            int charStartIndex = continuousStmts.get(0).getCharStartIndex();
            int charStopIndex = continuousStmts.get(continuousStmts.size() - 1).getCharStopIndex();

            int spaceForIndentation = getSpaceForIndentation(def,continuousStmts);

            FunctionDef newdef = new FunctionDef(def.getToken(), def.getInternalName(), def.getInternalArgs(), continuousStmts, def.getInternalDecorator_list(), def.getInternalReturns());


            String refactorableCode = newdef.getInternalBody().stream().map(Object::toString).collect(Collectors.joining("\n"));

            AdaptRule aRule = new AdaptRule(allMatchedGraphs.get(0), newdef, rpatternModule); //If you pass def instead of newdef, the adapted rule will be generated for whole function
            Rule rule = aRule.getAdaptedRule();

            Try<CombyRewrite> changedCode = op.rewrite(rule.getLHS(), rule.getRHS(), refactorableCode, ".python");


//            StringBuilder adaptedFile = new StringBuilder();
//            List<String> splittedFile = new ArrayList<>();
//            int previousStop = 0;
//            for (stmt stmt : Utils.getAllFunctions(codeModule)) {
//                int charStartIndex = stmt.getCharStartIndex();
//                int charStopIndex = ((FunctionDef)stmt).getInternalBody().get(((FunctionDef)stmt).getInternalBody().size()-1).getCharStopIndex();
//                adaptedFunction = MainAdaptor.transplantPatternToFunction(filename, (FunctionDef) stmt,imports,LHS, RHS);
//                if (!adaptedFunction.equals("")){
//                    adaptedFile.append(sourceCode, previousStop, charStartIndex).append(adaptedFunction);
            StringBuilder adaptedFunction = new StringBuilder();
            adaptedFunction.append(codeInFile, def.getCharStartIndex(), charStartIndex).append
                    (Arrays.stream(changedCode.get().getRewrittenSource().strip().split("\n")).collect(
                            Collectors.joining("\n" + " ".repeat(spaceForIndentation)))).append('\n');

            if (String.valueOf(codeInFile.charAt(charStopIndex + 1)).equals(" ")) {
                adaptedFunction
                        .append(codeInFile, charStopIndex + 1, def.getInternalBody().get(def.getInternalBody().size() - 1).getCharStopIndex());
            }
            else{
                adaptedFunction.append(" ".repeat(spaceForIndentation))
                        .append(codeInFile, charStopIndex + 1, def.getInternalBody().get(def.getInternalBody().size() - 1).getCharStopIndex());

            }

            return adaptedFunction.toString().strip();
        }
        return "";

    }

    private static int getSpaceForIndentation(FunctionDef def, List<stmt> continuousStmts) {
        if (continuousStmts.size()>1){
            return continuousStmts.get(1).getCharStartIndex()-continuousStmts.get(0).getCharStopIndex()-1;
        }
        else if(def.getInternalBody().size()>1){
            int i = continuousStmts.get(0).getParent().getChildren().indexOf(continuousStmts.get(0));
            if (i==0){
                return 0;
            }
            else{
                return continuousStmts.get(0).getCharStartIndex()-continuousStmts.get(0).getParent().getChildren().get(i-1).getCharStopIndex()-1;
            }
        }
        return 0;
    }

}


