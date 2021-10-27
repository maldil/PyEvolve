package org.inferrules;

import com.inferrules.core.RewriteRule;
import com.inferrules.core.Template;
import com.inferrules.core.TemplateNode;
import com.inferrules.core.VariableNameGenerator;
import com.inferrules.core.languageAdapters.JavaAdapter;
import com.inferrules.core.languageAdapters.LanguageSpecificInfo;
import com.inferrules.core.languageAdapters.PythonAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

public class TestRewriteRules {


//    @Test
//    void testJavaTemplates() throws IOException, URISyntaxException {
//        Map<String, String> scenarios = Map.of(
//                "Utils.transform(x);", "java/snippet1.json",
//                "x.map(Utils::transform);", "java/snippet2.json");
//
//        JavaAdapter languageAdapter = new JavaAdapter();
//        for(var scenario : scenarios.entrySet()){
//            TemplateNode t = new Template(scenario.getKey(), languageAdapter, new VariableNameGenerator('l')).getTemplateNode();
//            TemplateNode expectedTemplateNode = readTemplateNodeFromResource(scenario.getValue());
//            Assertions.assertEquals(t.toJson(),expectedTemplateNode.toJson());
//        }
//    }
//
    @Test
    void testPythonTemplates() throws IOException, URISyntaxException {

//        Map<String, String> scenarios = Map.of(
//                """
//                        count = 0
//                        for e in es:
//                                count += e
//                        print(count)
//                        """, "count = sum([1 for y in es])\n");
//
//        for (var sc : scenarios.entrySet()){
//            RewriteRule rw = new RewriteRule(sc.getKey(), sc.getValue(), LanguageSpecificInfo.Language.PYTHON3);
//            System.out.println();
//        }

    }
//        Map<String, String> scenarios = Map.of(
//                "count = 0\n" +
//                "for e in es:\n" +
//                "        count += e\n" +
//                "print(count)\n" , "python/snippet1.json",
//                "count = sum([1 for y in es])\n", "python/snippet3.json",
//                "count = 0\n" +
//                        "for e in es:\n" +
//                        "        y = sq(count)\n" +
//                        "        if not y:\n" +
//                        "                count += e\n" +
//                        "print(count)","python/snippet2.json");
//        PythonAdapter languageAdapter = new PythonAdapter();
//
//        for(var scenario : scenarios.entrySet()){
//            TemplateNode t = new Template(scenario.getKey(), languageAdapter, new VariableNameGenerator(true ? 'l' : 'r')).getTemplateNode();
//            TemplateNode expectedTemplateNode = readTemplateNodeFromResource(scenario.getValue());
//            Assertions.assertEquals(expectedTemplateNode.toJson(),t.toJson());
//        }
//    }


}
