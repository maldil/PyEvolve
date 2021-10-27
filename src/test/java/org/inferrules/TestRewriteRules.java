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


    @Test
    void testJavaRewriteRule1() throws IOException, URISyntaxException {
        String before = "Utils.transform(x);";
        String after = "x.map(Utils::transform);";
        String expectedMatch = ":[[l1]].:[[l2]](:[[l3]]);";
        String expectedReplace = ":[[l3]].map(:[[l1]]:::[[l2]]);";
        JavaAdapter languageAdapter = new JavaAdapter();
        RewriteRule rw = new RewriteRule(before, after, LanguageSpecificInfo.Language.JAVA);
        Assertions.assertEquals(expectedMatch, rw.getMatch().getTemplate());
        Assertions.assertEquals(expectedReplace, rw.getReplace().getTemplate());
    }

    @Test
    void testPythonRewriteRule1() throws IOException, URISyntaxException {
        String before = """
                count = 0
                for e in es:
                    count += e
                print(count)""";
        String after = "count = np.sum(es)";
        String expectedMatch = """
                :[[l1]] = 0
                for :[[l3]] in :[[l5]]:
                    :[[l1]] += :[[l3]]
                print(:[[l1]])""";
        String expectedReplace = ":[[l1]] = np.sum(:[[l5]])";
        PythonAdapter languageAdapter = new PythonAdapter();
        RewriteRule rw = new RewriteRule(before, after, LanguageSpecificInfo.Language.JAVA);
        Assertions.assertEquals(expectedMatch, rw.getMatch().getTemplate());
        Assertions.assertEquals(expectedReplace, rw.getReplace().getTemplate());
    }


}
