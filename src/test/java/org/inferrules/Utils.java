package org.inferrules;

import com.google.common.collect.Streams;
import com.google.gson.Gson;
import com.inferrules.comby.jsonResponse.CombyMatch;
import com.inferrules.core.Template;
import com.inferrules.utils.Utilities;
import com.matching.fgpdg.TestPDGOfProjects;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import org.python.antlr.Visitor;
import org.python.antlr.ast.FunctionDef;
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
            e.printStackTrace();
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


}
