package com.inferrules;

import com.inferrules.core.RewriteRule;
import com.inferrules.core.Template;
import com.inferrules.core.VariableNameGenerator;
import com.inferrules.core.languageAdapters.JavaAdapter;
import com.inferrules.core.languageAdapters.LanguageSpecificInfo;
import com.inferrules.core.languageAdapters.PythonAdapter;

import java.io.IOException;

public class Runner {

    public static void main(String[] args) throws IOException {
        String javaClassContent1 = "Utils.transform(x);";
        String javaClassContent2 = "x.map(Utils::transform);";
        String pyc = "count = 0\n" +
                "for e in es:\n" +
                "        count += e\n" +
                "print(count)\n";

        String pyClassContent1 = "count = 0\n" +
                "for e in es:\n" +
                "        y = sq(count)\n" +
                "        if not y:\n" +
                "                count += e\n" +
                "print(count)";
        String pyClassContent2 = "count = sum([1 for y in es])\n";

        PythonAdapter adp = new PythonAdapter();
        JavaAdapter jadp = new JavaAdapter();
        var tx = new Template(pyc, adp, new VariableNameGenerator('l'));
        Template tpl = new Template(pyClassContent1, adp, new VariableNameGenerator('l'));
        Template tpr = new Template(pyClassContent2, adp, new VariableNameGenerator('l'));
        Template jtpl = new Template(javaClassContent1, jadp, new VariableNameGenerator('l'));
        Template jtpr = new Template(javaClassContent2, jadp, new VariableNameGenerator('l'));

        RewriteRule jRw = new RewriteRule(javaClassContent1,javaClassContent2, LanguageSpecificInfo.Language.JAVA);
        RewriteRule pRw = new RewriteRule(pyc,pyClassContent2, LanguageSpecificInfo.Language.PYTHON3);
        System.out.println();
    }
}
