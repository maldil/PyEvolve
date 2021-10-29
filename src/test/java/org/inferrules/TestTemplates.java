package org.inferrules;

import com.inferrules.core.Template;
import com.inferrules.core.VariableNameGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.inferrules.core.languageAdapters.LanguageSpecificInfo.Language.Java;
import static com.inferrules.core.languageAdapters.LanguageSpecificInfo.Language.Python;
import static org.inferrules.Utils.areAlphaEquivalent;

public class TestTemplates {

    @Test
    void testJavaTemplate1() {
        String scenario = "Utils.transform(x);";
        TemplateAsString expectedTemplates = new TemplateAsString(":[l1];", ":[l0];", ":[[l1]].:[[l2]](:[[l3]]);");
        Template t = new Template(scenario, Java, new VariableNameGenerator('l'));
        TemplateAsString actual = new TemplateAsString(t);
        Assertions.assertTrue(areAlphaEquivalent(expectedTemplates.getCoarsest(), actual.getCoarsest()));
        //Assertions.assertTrue(areAlphaEquivalent(expectedTemplates.getOptimal(),actual.getOptimal()));
        Assertions.assertTrue(areAlphaEquivalent(expectedTemplates.getFinest(),actual.getFinest()));
    }

    @Test
    void testJavaTemplate2() {
        String scenario = "x.map(Utils::transform);";
        TemplateAsString expectedTemplates = new TemplateAsString(":[l0];", ":[l0];", ":[[l1]].:[[l2]](:[[l4]]:::[[l5]]);");
        Template t = new Template(scenario, Java, new VariableNameGenerator('l'));
        TemplateAsString actual = new TemplateAsString(t);
        Assertions.assertTrue(areAlphaEquivalent(expectedTemplates.getCoarsest(),actual.getCoarsest()));
        //Assertions.assertTrue(areAlphaEquivalent(expectedTemplates.getOptimal(),actual.getOptimal()));
        Assertions.assertTrue(areAlphaEquivalent(expectedTemplates.getFinest(),actual.getFinest()));
    }

    @Test
    void testPythonTemplate1() {
        String scenario = """
                count = 0
                for e in es:
                        z += y
                        count += e
                print(count)
                """;
        TemplateAsString expectedTemplates = new TemplateAsString("""
                :[l0]
                :[l4]:[l14]
                """, """
                :[[l1]] :[l2]
                for :[[l5]] in :[[l6]]:
                        :[l8]
                        :[[l1]] += :[[l5]]
                :[[l15]](:[[l1]])
                """, """
                :[[l1]] = :[[l3]]
                for :[[l5]] in :[[l6]]:
                        :[[l9]] += :[[l11]]
                        :[[l1]] += :[[l5]]
                :[[l15]](:[[l1]])
                """);
        Template t = new Template(scenario, Python, new VariableNameGenerator('l'));
        TemplateAsString actual = new TemplateAsString(t);
        Assertions.assertTrue(areAlphaEquivalent(expectedTemplates.getCoarsest(),actual.getCoarsest()));
        //Assertions.assertTrue(areAlphaEquivalent(expectedTemplates.getOptimal(),actual.getOptimal()));
        Assertions.assertTrue(areAlphaEquivalent(expectedTemplates.getFinest(),actual.getFinest()));
    }

    @Test
    void testPythonTemplate2() {
        String scenario = """
                count = 0
                for e in es:
                        y = sq(count)
                        if not y:
                                count += e
                print(count)""";
        TemplateAsString expectedTemplates = new TemplateAsString("""
                :[a]
                :[b]:[c]""",
                """
                :[[a]] :[l2]
                for :[[l5]] in :[[l6]]:
                        :[[l9]] = :[[l12]](:[[a]])
                        if not :[[l9]]:
                                :[[a]] += :[[l5]]
                :[[l20]](:[[a]])""", """
                :[[l1]] = :[[l3]]
                for :[[l5]] in :[[l6]]:
                        :[[l9]] = :[[l12]](:[[l1]])
                        if not :[[l9]]:
                                :[[l1]] += :[[l5]]
                :[[l20]](:[[l1]])""");
        Template t = new Template(scenario, Python, new VariableNameGenerator('l'));
        TemplateAsString actual = new TemplateAsString(t);
        Assertions.assertTrue(areAlphaEquivalent(expectedTemplates.getCoarsest(),actual.getCoarsest()));
        //Assertions.assertTrue(areAlphaEquivalent(expectedTemplates.getOptimal(),actual.getOptimal()));
        Assertions.assertTrue(areAlphaEquivalent(expectedTemplates.getFinest(),actual.getFinest()));
    }

    @Test
    void testPythonTemplate3() {
        String scenario = "count = sum([1 for y in es])\n";
        TemplateAsString expectedTemplates = new TemplateAsString(":[[l0]] :[l1]", ":[[l0]] :[l1]", ":[[l0]] = :[[l3]]([:[[l7]] for :[[l9]] in :[[l10]]])");
        Template t = new Template(scenario, Python, new VariableNameGenerator('l'));
        TemplateAsString actual = new TemplateAsString(t);
        Assertions.assertTrue(areAlphaEquivalent(expectedTemplates.getCoarsest(),actual.getCoarsest()));
        //Assertions.assertTrue(areAlphaEquivalent(expectedTemplates.getOptimal(),actual.getOptimal()));
        Assertions.assertTrue(areAlphaEquivalent(expectedTemplates.getFinest(),actual.getFinest()));
    }

    @Test
    void testPythonTemplate4() {
        String scenario = """
                        mse = 0.0
                        for i in range(border_size, image1.shape[0] - border_size):
                         for j in range(border_size, image1.shape[1] - border_size):
                           for k in range(image1.shape[2]):
                        \terror = image1[i, j, k] - image2[i, j, k]
                        \tmse += error * error
                        \treturn mse / ((image1.shape[0] - 2 * border_size) * (image1.shape[1] - 2 * border_size) * image1.shape[2])""";
        TemplateAsString expectedTemplates = new TemplateAsString("""
                        :[l0]
                        :[l4]""",
                """
                        :[[l1]] :[l2]
                        for :[[l5]] in :[[l7]](:[[l10]], :[[l13]].:[[l15]][:[[l17]]] - :[[l10]]):
                         for :[[l19]] in :[[l7]](:[[l10]], :[[l13]].:[[l15]][:[[l27]]] - :[[l10]]):
                           for :[[l29]] in :[[l7]](:[[l13]].:[[l15]][:[[l35]]]):
                        \t:[[l38]] = :[[l13]][:[[l5]], :[[l19]], :[[l29]]] - :[[l45]][:[[l5]], :[[l19]], :[[l29]]]
                        \t:[[l1]] += :[[l38]] * :[[l38]]
                        \treturn :[[l1]] / ((:[[l13]].:[[l15]][:[[l17]]] - :[[l35]] * :[[l10]]) * (:[[l13]].:[[l15]][:[[l27]]] - :[[l35]] * :[[l10]]) * :[[l13]].:[[l15]][:[[l35]]])""",
                """
                        :[[l1]] = :[l3]
                        for :[[l5]] in :[[l7]](:[[l10]], :[[l13]].:[[l15]][:[[l17]]] - :[[l10]]):
                         for :[[l19]] in :[[l7]](:[[l10]], :[[l13]].:[[l15]][:[[l27]]] - :[[l10]]):
                           for :[[l29]] in :[[l7]](:[[l13]].:[[l15]][:[[l35]]]):
                        \t:[[l38]] = :[[l13]][:[[l5]], :[[l19]], :[[l29]]] - :[[l45]][:[[l5]], :[[l19]], :[[l29]]]
                        \t:[[l1]] += :[[l38]] * :[[l38]]
                        \treturn :[[l1]] / ((:[[l13]].:[[l15]][:[[l17]]] - :[[l35]] * :[[l10]]) * (:[[l13]].:[[l15]][:[[l27]]] - :[[l35]] * :[[l10]]) * :[[l13]].:[[l15]][:[[l35]]])""");
        Template t = new Template(scenario, Python, new VariableNameGenerator('l'));
        TemplateAsString actual = new TemplateAsString(t);
        Assertions.assertTrue(areAlphaEquivalent(expectedTemplates.getCoarsest(),actual.getCoarsest()));
        //Assertions.assertTrue(areAlphaEquivalent(expectedTemplates.getOptimal(),actual.getOptimal()));
        Assertions.assertTrue(areAlphaEquivalent(expectedTemplates.getFinest(),actual.getFinest()));
    }
}
