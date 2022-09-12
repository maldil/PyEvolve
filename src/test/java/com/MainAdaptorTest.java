package com;

import com.inferrules.core.RewriteRule;
import com.inferrules.core.languageAdapters.Language;
import com.matching.ConcreatePythonParser;
import com.matching.fgpdg.nodes.Guards;
import org.inferrules.Utils;
import org.junit.jupiter.api.Test;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.Module;
import org.python.antlr.base.stmt;

import static org.inferrules.Utils.getPythonModule;
import static org.inferrules.Utils.getPythonModuleForTemplate;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class MainAdaptorTest {

    @Test
    void adaptFunction() throws Exception {
        MainAdaptor adaptor = new MainAdaptor();
        Module codeModule = getPythonModule("");
        Module lpatternModule = getPythonModuleForTemplate("");
        Module rpatternModule = getPythonModuleForTemplate("");
        List<stmt> imports = lpatternModule.getInternalBody().stream().filter(x -> x instanceof Import
                || x instanceof ImportFrom).collect(Collectors.toList());
        Guards guards = new Guards("",lpatternModule);
        String adaptFunction = adaptor.adaptFunction(imports, guards, lpatternModule, rpatternModule, codeModule);
    }
}