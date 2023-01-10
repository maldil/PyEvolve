package com;

import com.inferrules.core.RewriteRule;
import com.inferrules.core.languageAdapters.Language;
import com.matching.ConcreatePythonParser;
import com.matching.fgpdg.nodes.Guards;
import org.inferrules.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.Module;
import org.python.antlr.base.stmt;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.inferrules.Utils.*;
import static org.junit.jupiter.api.Assertions.*;

class MainAdaptorTest {
    @Test
    void transplantPatternToFile1() {
        String projectFile = "author/project/test26.py";
        String LHS = getPathToResources("author/project/pattern12.py") ;
        String RHS = getPathToResources("author/project/r_pattern12.py");
        String s = MainAdaptor.transplantPatternToFile(projectFile, LHS, RHS);
        Assertions.assertEquals("import numpy as np\n" +
                "\n" +
                "def function1(sentence, callbacks):\n" +
                "    ff = {one:1,two:2}\n" +
                "    print(ff)\n" +
                "    z = np.sum(ff.values())\n" +
                "return z\n" +
                "#Test\n" +
                "#Test again\n" +
                "def function2(sentence, callbacks):\n" +
                "    ff = {one:1,two:2}\n" +
                "    print(ff)\n" +
                "    z = np.sum(ff.values())\n" +
                "return z\n" +
                "#Somecomment\n" +
                "\n" +
                "def function1(sentence,callbacks):\n" +
                "    ff = {\"one\":1,\"two\":2}\n" +
                "    z=0\n" +
                "    print(ff)\n" +
                "    return z\n" +
                "\n" +
                "\n" +
                "def function3(sentence, callbacks):\n" +
                "    ff = {one:1,two:2}\n" +
                "    print(ff)\n" +
                "    z = np.sum(ff.values())\n" +
                "return z\n" +
                "\n" +
                "# Done",s);
    }

    @Test
    void transplantPatternToFile2() {
        String projectFile = "dipy/dipy/dipy/reconst/forecast.py";
        String LHS = getPathToResources("author/project/pattern17.py");
        String RHS = getPathToResources("author/project/r_pattern17.py");
        String s = MainAdaptor.transplantPatternToFile(projectFile, LHS, RHS);
        Assertions.assertEquals(" ",s);
    }

    @Test
    void transplantPatternToFunction() {
        String projectFile = "author/project/test26.py";
        String LHS = "/Users/malinda/Documents/Research3/InferRules/src/test/resources/author/project/pattern12.py";
        String RHS = "/Users/malinda/Documents/Research3/InferRules/src/test/resources/author/project/r_pattern12.py";
        Module codeModule = com.utils.Utils.getPythonModule(projectFile);
        stmt stmt = codeModule.getInternalBody().get(1);
        List<org.python.antlr.base.stmt> imports = codeModule.getInternalBody().stream().filter(x -> x instanceof Import
                || x instanceof ImportFrom).collect(Collectors.toList());
        String s = MainAdaptor.transplantPatternToFunction(projectFile, (FunctionDef) stmt,imports,LHS, RHS);
        System.out.println(s);
        Assertions.assertEquals("def function1(sentence, callbacks):\n" +
                "    ff = {one:1,two:2}\n" +
                "    print(ff)\n" +
                "    z = np.sum(ff.values())\n" +
                "return z",s);
    }


}