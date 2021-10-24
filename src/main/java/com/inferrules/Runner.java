package com.inferrules;

import com.inferrules.core.RewriteRule;
import com.inferrules.core.Template;
import com.inferrules.core.languageAdapters.JavaAdapter;
import com.inferrules.core.languageAdapters.PythonAdapter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class Runner {

    public static void main(String[] args) throws IOException {
        String javaClassContent1 = "abc.def(x.y());";
        String javaClassContent2 = "abc.xyz(x.y());";
        String pyClassContent1 = "count=0\n" + "for e in es:\n" +
                "        count += e" ;
        String pyClassContent2 = "count = np.sum(es)\n";
        PythonAdapter adp = new PythonAdapter();
        JavaAdapter jadp = new JavaAdapter();
        Template tpl = new Template(pyClassContent1,adp, true);
        Template tpr = new Template(pyClassContent2,adp, false);

        RewriteRule pyRw = new RewriteRule(tpl, tpr);

        Template jtpl = new Template(javaClassContent1,jadp, true);
        Template jtpr = new Template(javaClassContent2,jadp, false);
        RewriteRule jRw = new RewriteRule(jtpl, jtpr);
        System.out.println();
    }
}
