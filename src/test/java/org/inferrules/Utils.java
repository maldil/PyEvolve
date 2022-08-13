package org.inferrules;

import com.google.common.collect.Streams;
import com.google.gson.Gson;
import com.inferrules.comby.jsonResponse.CombyMatch;
import com.inferrules.core.Template;
import com.inferrules.utils.Utilities;
import com.matching.ConcreatePythonParser;
import com.matching.fgpdg.*;
import com.matching.fgpdg.nodes.Guards;
import com.matching.fgpdg.nodes.TypeInfo.TypeWrapper;
import com.utils.FileIO;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import org.python.antlr.Visitor;
import org.python.antlr.ast.*;
import org.python.antlr.ast.Module;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

public class Utils {
    private static final String matchCommand = "echo \"{0}\" | comby \"{1}\" -stdin -json-lines -match-newline-at-toplevel -match-only  -matcher {2} \"foo\"";
    private static final String defaultLanguage = ".java";

    public static CombyMatch getMatch(String template, String value, String language, boolean isPerfect) {
        Object[] arguments = {value, template, language == null ? defaultLanguage : language};
        return Utilities.runBashCommand(MessageFormat.format(matchCommand, arguments))
                .map(x -> new Gson().fromJson(x, CombyMatch.class))
                .onFailure(x -> System.out.println(x.toString()))
                .filter(x -> !isPerfect || x.isPerfect(value)).getOrNull();

    }

    public static List<String> tokenizeTemplate(String tempalte){
        CombyMatch m = getMatch(":[a.]", tempalte,".java", false);
        return m.getMatches().stream().map(x->x.getMatched()).collect(Collectors.toList());

    }

    public static boolean areAlphaEquivalent(String template1, String template2){
        return areAlphaEquivalent(tokenizeTemplate(template1), tokenizeTemplate(template2));
    }

    public static <T> boolean areAlphaEquivalent(List<T> ls1, List<T> ls2) {
        if(ls1.size()!= ls2.size())
            return false;
        return Streams.zip(ls1.stream(), ls2.stream(), Tuple::of)
                .collect(groupingBy(x->x._1(), collectingAndThen(toList(), xs -> xs.stream().map(x -> x._2()).distinct().count())))
                .entrySet().stream().allMatch(x->x.getValue() == 1)
                && Streams.zip(ls2.stream(), ls1.stream(), Tuple::of)
                .collect(groupingBy(x->x._1(), collectingAndThen(toList(), xs -> xs.stream().map(x -> x._2()).distinct().count())))
                .entrySet().stream().allMatch(x->x.getValue() == 1)
                ;
    }

    public static ArrayList<File> getPythonFiles(File[] files) {
        ArrayList<File> pythonFiles = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                if (!file.getName().startsWith(".")) {
                    pythonFiles.addAll(getPythonFiles(Objects.requireNonNull(file.listFiles()))); // Calls same method again.
                }
            } else {
                if (file.getName().endsWith(".py")) {
                    pythonFiles.add(file);
                }
            }
        }
        return pythonFiles;
    }

    public static ArrayList<FunctionDef>  getAllFunctions(Module ast){
        PyFuncDefVisitor fu = new PyFuncDefVisitor();
        try {
            fu.visit(ast);
            return fu.funcDefs;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    static class PyFuncDefVisitor extends Visitor {
        ArrayList<FunctionDef> funcDefs = new ArrayList<>();
        @Override
        public Object visitFunctionDef(FunctionDef node) throws Exception {
            funcDefs.add(node);
            return super.visitFunctionDef (node);
        }
    }

    public static List<MatchedNode> getMatchedNodes(String filename, String patternname, List<MatchedNode> graphs) throws Exception {
        Module codeModule = getPythonModule("author/project/"+filename+".py");
        Module patternModule = getPythonModuleForTemplate(getPathToResources("author/project/"+patternname+".py"));
        return getMatchedNodes(filename, patternname,null, codeModule, patternModule,null);
    }

    public static List<MatchedNode> getMatchedNodes(String filename, String lpatternname,String rpatternname,  Module codeModule, Module lpatternModule,Module rpatternModule) throws IOException {
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
        fcontext = new PDGBuildingContext(codeModule.getInternalBody().stream().filter(x-> x instanceof Import
                || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/"+filename+".py");
        PDGGraph fpdg = new PDGGraph(func,fcontext);
//        fpdg.getNodes().forEach(x-> System.out.println(x.getId()));
        Guards guards = new Guards(com.utils.Utils.getFileContent(getPathToResources("author/project/"+lpatternname+".py")),lpatternModule);
        TypeWrapper wrapper = new TypeWrapper(guards);
        PDGBuildingContext mcontext = new PDGBuildingContext(lpatternModule.getInternalBody().stream().filter(x -> x instanceof Import
                || x instanceof ImportFrom).collect(Collectors.toList()),wrapper);
        PDGGraph mpdg = new PDGGraph(lpatternModule,mcontext);
        MatchPDG match = new MatchPDG();
        graphs=match.getSubGraphs(mpdg,fpdg,mcontext,fcontext );
        graphs.forEach(x->x.updateAllMatchedNodes(x,mpdg));
        List<MatchedNode> finalPatterns = graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList());

        match.drawMatchedGraphs(fpdg,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()),"OUTPUT/matches/"+filename+".dot");
        com.utils.Utils.markNodesInCode("src/test/resources/author/project/"+filename+".py",
                graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()),"OUTPUT/matches/"+filename+".html","","x@x");
        ;

        return graphs;
    }

    public static Module getPythonModule(String fileName){
        ConcreatePythonParser parser = new ConcreatePythonParser();
        return parser.parse(fileName);
    }

    public static Module getPythonModuleForTemplate(String fileName) throws Exception {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        return parser.parseTemplates(FileIO.readStringFromFile(fileName));
    }

    public static String getPathToResources(String name){
        File f = new File(name);
        if (f.exists()) {
            return f.getAbsolutePath();
        }
        return Utils.class.getClassLoader().getResource(name).getPath();

    }


}
