package org.inferrules;

import com.inferrules.core.Template;
import com.inferrules.core.VariableNameGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.inferrules.core.languageAdapters. Language.Java;
import static com.inferrules.core.languageAdapters. Language.Python;
import static com.inferrules.utils.Utilities.stream;
import static java.util.stream.Collectors.toList;
import static org.inferrules.Utils.areAlphaEquivalent;

public class TestTemplates {

    @Test
    void testJavaTemplate1() {
        String scenario = "Utils.transform(x);";
        Template t = new Template(scenario, Java, new VariableNameGenerator('l'));
//        List<String> order = stream(Template.TreeTraverser.depthFirstPostOrder(t.getUnflattendTemplateNode())).map(x -> x._1().toString()).collect(toList());
//        List<String> expectedOrder = List.of(":[[l1]]", ":[[l2]]", ":[[l3]]", ":[l0]", ":[[dummy]]");
        //Assertions.assertTrue(areAlphaEquivalent(order, expectedOrder));
        Assertions.assertTrue(areAlphaEquivalent(t.getUnflattendTemplateNode()._2().getTemplate(), ":[l1];"));
        Assertions.assertTrue(areAlphaEquivalent(t.getCompletelyFlattenedTemplateNode()._2().getTemplate(), ":[[l1]].:[[l2]](:[[l3]]);"));
    }

    @Test
    void testJavaTemplate2() {
        String scenario = "x.map(Utils::transform);";
        Template t = new Template(scenario, Java, new VariableNameGenerator('l'));
//        List<String> order = stream(Template.TreeTraverser.depthFirstPostOrder(t.getUnflattendTemplateNode())).map(x -> x._1().toString()).collect(toList());
//        List<String> expectedOrder = List.of(":[[l1]]", ":[[l2]]", ":[[l4]]", ":[[l5]]", ":[l3]", ":[l0]", ":[[dummy]]");
        //Assertions.assertTrue(areAlphaEquivalent(order, expectedOrder));
        Assertions.assertTrue(areAlphaEquivalent(t.getUnflattendTemplateNode()._2().getTemplate(), ":[l0];"));
        Assertions.assertTrue(areAlphaEquivalent(t.getCompletelyFlattenedTemplateNode()._2().getTemplate(), ":[[l1]].:[[l2]](:[[l4]]:::[[l5]]);"));
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
        Template t = new Template(scenario, Python, new VariableNameGenerator('l'));
//        List<String> order = stream(Template.TreeTraverser.depthFirstPostOrder(t.getUnflattendTemplateNode())).map(x -> x._1().toString()).collect(toList());
//        List<String> expectedOrder = List.of(":[[l1]]", ":[[l3]]", ":[l2]", ":[l0]", ":[[l5]]", ":[[l6]]", ":[[l9]]", ":[[l11]]", ":[l10]", ":[l8]", ":[[l1]]", ":[[l5]]",
//                ":[l13]", ":[l12]", ":[l7]", ":[l4]", ":[[l15]]", ":[[l1]]", ":[l16]", ":[l14]", ":[[dummy]]");
        //Assertions.assertTrue(areAlphaEquivalent(order, expectedOrder));
        Assertions.assertTrue(areAlphaEquivalent(t.getUnflattendTemplateNode()._2().getTemplate(), """
                :[l0]
                :[l4]:[l14]
                """));
        Assertions.assertTrue(areAlphaEquivalent(t.getCompletelyFlattenedTemplateNode()._2().getTemplate(), """
                :[[l1]] = :[[l3]]
                for :[[l5]] in :[[l6]]:
                        :[[l9]] += :[[l11]]
                        :[[l1]] += :[[l5]]
                :[[l15]](:[[l1]])
                """));
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
        Template t = new Template(scenario, Python, new VariableNameGenerator('l'));
//        List<String> order = stream(Template.TreeTraverser.depthFirstPostOrder(t.getUnflattendTemplateNode())).map(x -> x._1().toString()).collect(toList());
//        List<String> expectedOrder = List.of(":[[l1]]", ":[[l3]]", ":[l2]", ":[l0]", ":[[l5]]", ":[[l6]]", ":[[l9]]", ":[[l12]]", ":[[l1]]", ":[l13]", ":[l11]",
//                ":[l10]", ":[l8]", ":[[l9]]",
//                ":[[l15]]", ":[[l1]]", ":[[l5]]", ":[l17]", ":[l16]", ":[l14]", ":[l7]", ":[l4]", ":[[l20]]", ":[[l1]]", ":[l13]", ":[l19]", ":[l18]", ":[[dummy]]");
        //Assertions.assertTrue(areAlphaEquivalent(order, expectedOrder));
        Assertions.assertTrue(areAlphaEquivalent(t.getUnflattendTemplateNode()._2().getTemplate(), """
                :[a]
                :[b]:[c]"""));
        Assertions.assertTrue(areAlphaEquivalent(t.getCompletelyFlattenedTemplateNode()._2().getTemplate(), """
                :[[l1]] = :[[l3]]
                for :[[l5]] in :[[l6]]:
                        :[[l9]] = :[[l12]](:[[l1]])
                        if not :[[l9]]:
                                :[[l1]] += :[[l5]]
                :[[l20]](:[[l1]])"""));
    }

    @Test
    void testPythonTemplate3() {
        String scenario = "count = sum([1 for y in es])\n";
        Template t = new Template(scenario, Python, new VariableNameGenerator('l'));
//        List<String> order = stream(Template.TreeTraverser.depthFirstPostOrder(t.getUnflattendTemplateNode())).map(x -> x._1().toString()).collect(toList());
//        List<String> expectedOrder = List.of(":[[l0]]",":[[l3]]",":[[l7]]",":[[l9]]",":[[l10]]",":[[l8]]",":[[l6]]",":[l5]",":[l4]",":[l2]",":[l1]",":[[dummy]]");
        //Assertions.assertTrue(areAlphaEquivalent(order, expectedOrder));
        Assertions.assertTrue(areAlphaEquivalent(t.getUnflattendTemplateNode()._2().getTemplate(), ":[[l0]] :[l1]"));
        Assertions.assertTrue(areAlphaEquivalent(t.getCompletelyFlattenedTemplateNode()._2().getTemplate(), ":[[l0]] = :[[l3]]([:[[l7]] for :[[l9]] in :[[l10]]])"));
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
        Template t = new Template(scenario, Python, new VariableNameGenerator('l'));
//        List<String> order = stream(Template.TreeTraverser.depthFirstPostOrder(t.getUnflattendTemplateNode())).map(x -> x._1().toString()).collect(toList());
//        List<String> expectedOrder = List.of(":[[l1]]",":[l3]",":[l2]",":[l0]",":[[l5]]",":[[l7]]",":[[l10]]",":[[l13]]",":[[l15]]",":[[l17]]",":[l16]",":[l14]",
//                ":[l12]",":[[l10]]",":[l11]",":[l9]",":[l8]",":[l6]",":[[l19]]",":[[l7]]",":[[l10]]",":[[l13]]",":[[l15]]",":[[l27]]",":[l26]",":[l25]",":[l24]",
//                ":[[l10]]",":[l23]",":[l22]",":[l21]",":[l20]",":[[l29]]",":[[l7]]",":[[l13]]",":[[l15]]",":[[l35]]",":[l34]",":[l33]",":[l32]",":[l31]",":[l30]",
//                ":[[l38]]",":[[l13]]",":[[l5]]",":[[l19]]",":[[l29]]",":[l43]",":[l42]",":[l41]",":[[l45]]",":[[l5]]",":[[l19]]",":[[l29]]",":[l43]",":[l42]",":[l44]",
//                ":[l40]",":[l39]",":[l37]",":[[l1]]",":[[l38]]",":[[l38]]",":[l48]",":[l47]",":[l46]",":[[l1]]",":[[l13]]",":[[l15]]",":[[l17]]",":[l16]",":[l14]",":[l12]",
//                ":[[l35]]",":[[l10]]",":[l56]",":[l55]",":[l54]",":[[l13]]",":[[l15]]",":[[l27]]",":[l26]",":[l25]",":[l24]",":[[l35]]",":[[l10]]",":[l56]",":[l58]",":[l57]",
//                ":[l53]",":[[l13]]",":[[l15]]",":[[l35]]",":[l34]",":[l33]",":[l32]",":[l52]",":[l51]",":[l50]",":[l49]",":[l36]",":[l28]",":[l18]",":[l4]",":[[dummy]]");
        //Assertions.assertTrue(areAlphaEquivalent(order, expectedOrder));
        Assertions.assertTrue(areAlphaEquivalent(t.getUnflattendTemplateNode()._2().getTemplate(), """
                :[l0]
                :[l4]"""));
        Assertions.assertTrue(areAlphaEquivalent(t.getCompletelyFlattenedTemplateNode()._2().getTemplate(), """
                :[[l1]] = :[l3]
                for :[[l5]] in :[[l7]](:[[l10]], :[[l13]].:[[l15]][:[[l17]]] - :[[l10]]):
                 for :[[l19]] in :[[l7]](:[[l10]], :[[l13]].:[[l15]][:[[l27]]] - :[[l10]]):
                   for :[[l29]] in :[[l7]](:[[l13]].:[[l15]][:[[l35]]]):
                \t:[[l38]] = :[[l13]][:[[l5]], :[[l19]], :[[l29]]] - :[[l45]][:[[l5]], :[[l19]], :[[l29]]]
                \t:[[l1]] += :[[l38]] * :[[l38]]
                \treturn :[[l1]] / ((:[[l13]].:[[l15]][:[[l17]]] - :[[l35]] * :[[l10]]) * (:[[l13]].:[[l15]][:[[l27]]] - :[[l35]] * :[[l10]]) * :[[l13]].:[[l15]][:[[l35]]])"""));
    }
}
