package com.adaptrule;

import com.matching.fgpdg.MatchedNode;
import org.inferrules.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.python.antlr.ast.Module;

import java.util.List;
import java.util.stream.Collectors;

import static org.inferrules.Utils.getMatchedNodes;
import static org.junit.jupiter.api.Assertions.*;

class AdaptRuleTest {
    @Test
    void testAdaptedRule1() throws Exception {
        String filename="test26";
        String lpatternname = "pattern12";
        String rpatternname = "r_pattern12";
        Module codeModule = Utils.getPythonModule("author/project/"+filename+".py");
        Module lpatternModule = Utils.getPythonModuleForTemplate(Utils.getPathToResources("author/project/"+lpatternname+".py"));
        Module rpatternModule = Utils.getPythonModuleForTemplate(Utils.getPathToResources("author/project/"+rpatternname+".py"));
        List<MatchedNode> matchedNodes = getMatchedNodes(filename, lpatternname,rpatternname, codeModule, lpatternModule,rpatternModule);
        List<MatchedNode> allMatchedGraphs = matchedNodes.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList());

        AdaptRule aRule= new AdaptRule(allMatchedGraphs.get(0),Utils.getAllFunctions(codeModule).get(0) ,rpatternModule);
        Rule rule = aRule.getAdaptedRule();
        assertEquals(
                "def function1(sentence, callbacks):\n" +
                "    :[l4]\n" +
                "    :[[l2]] = 0\n" +
                "    :[l5]\n" +
                "    for :[[l1]] in :[l3].values():\n" +
                "        :[[l2]] = :[[l2]] + :[[l1]]\n" +
                "return :[[l2]]",rule.getLHS());
        assertEquals(
                "def function1(sentence, callbacks):\n" +
                "    :[l4]\n" +
                "    :[l5]\n" +
                "    :[[l2]] = np.sum(:[l3].values())\n" +
                "return :[[l2]]",rule.getRHS());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testAdaptedRule2() throws Exception {
        String filename="test36";
        String lpatternname = "pattern12";
        String rpatternname = "r_pattern12";
        Module codeModule = Utils.getPythonModule("author/project/"+filename+".py");
        Module lpatternModule = Utils.getPythonModuleForTemplate(Utils.getPathToResources("author/project/"+lpatternname+".py"));
        Module rpatternModule = Utils.getPythonModuleForTemplate(Utils.getPathToResources("author/project/"+rpatternname+".py"));

        List<MatchedNode> matchedNodes = getMatchedNodes(filename, lpatternname,rpatternname, codeModule, lpatternModule,rpatternModule);
        List<MatchedNode> allMatchedGraphs = matchedNodes.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList());
        AdaptRule aRule= new AdaptRule(allMatchedGraphs.get(0),Utils.getAllFunctions(codeModule).get(0) ,rpatternModule);
        Rule rule = aRule.getAdaptedRule();
        assertEquals(
                "def function1(sentence, callbacks):\n" +
                "    :[l4]\n" +
                "    :[[l2]] = 0\n" +
                "    :[l5]\n" +
                "    for :[[l1]] in :[l3].values():\n" +
                "        q = :[[l2]] + :[[l1]]\n" +
                "        :[[l2]] = q\n" +
                "return :[[l2]]",rule.getLHS());
        assertEquals(
                "def function1(sentence, callbacks):\n" +
                "    :[l4]\n" +
                "    :[l5]\n" +
                "    :[[l2]] = np.sum(:[l3].values())\n" +
                "return :[[l2]]",rule.getRHS());


//        assertEquals();

    }

    @Test
    void testAdaptedRule3() throws Exception {
        String filename="test25";
        String lpatternname = "lpattern17";
        String rpatternname = "rpattern17";
        Module codeModule = Utils.getPythonModule("author/project/"+filename+".py");
        Module lpatternModule = Utils.getPythonModuleForTemplate(Utils.getPathToResources("author/project/"+lpatternname+".py"));
        Module rpatternModule = Utils.getPythonModuleForTemplate(Utils.getPathToResources("author/project/"+rpatternname+".py"));
        List<MatchedNode> matchedNodes = getMatchedNodes(filename, lpatternname,rpatternname, codeModule, lpatternModule,rpatternModule);
        List<MatchedNode> allMatchedGraphs = matchedNodes.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList());

        AdaptRule aRule= new AdaptRule(allMatchedGraphs.get(0),Utils.getAllFunctions(codeModule).get(0) ,rpatternModule);
        Rule rule = aRule.getAdaptedRule();
        assertEquals(
                "def test_application_pretrained_weights_loading(self):\n" +
                "    :[l5]\n" +
                "    :[l6]\n" +
                "    :[l7]\n" +
                "    for app in apps:\n" +
                "        :[l8]\n" +
                "        :[l9]\n" +
                "        :[l10]\n" +
                "        :[[l4]] = np.dot(np.dot(:[[l1]], :[[l2]]), :[[l3]])\n" +
                "        :[l11]\n" +
                "        :[l12]\n" +
                "        names = [p[1] for :[[l4]] in app_module.decode_predictions(preds)[0]]\n" +
                "        :[l14]",rule.getLHS());
        assertEquals(
                "def test_application_pretrained_weights_loading(self):\n" +
                "    :[l5]\n" +
                "    :[l6]\n" +
                "    :[l7]\n" +
                "    for app in apps:\n" +
                "        :[l8]\n" +
                "        :[l9]\n" +
                "        :[l10]\n" +
                "        :[[l4]] = np.multidot(:[[l1]], :[[l2]], :[[l3]])\n" +
                "        :[l11]\n" +
                "        :[l12]\n" +
                "        names = [p[1] for :[[l4]] in app_module.decode_predictions(preds)[0]]\n" +
                "        :[l14]",rule.getRHS());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testAdaptedRule4() throws Exception {
        String filename="test37";
        String lpatternname = "lpattern17";
        String rpatternname = "rpattern17";
        Module codeModule = Utils.getPythonModule("author/project/"+filename+".py");
        Module lpatternModule = Utils.getPythonModuleForTemplate(Utils.getPathToResources("author/project/"+lpatternname+".py"));
        Module rpatternModule = Utils.getPythonModuleForTemplate(Utils.getPathToResources("author/project/"+rpatternname+".py"));
        List<MatchedNode> matchedNodes = getMatchedNodes(filename, lpatternname,rpatternname, codeModule, lpatternModule,rpatternModule);
        List<MatchedNode> allMatchedGraphs = matchedNodes.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList());

        AdaptRule aRule= new AdaptRule(allMatchedGraphs.get(0), com.utils.Utils.getAllFunctions(codeModule).get(0) ,rpatternModule);
        Rule rule  = aRule.getAdaptedRule();
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testAdaptedRule5() throws Exception {
        String filename="test25";
        String lpatternname = "lpattern18";
        String rpatternname = "rpattern18";
        Module codeModule = Utils.getPythonModule("author/project/"+filename+".py");
        Module lpatternModule = Utils.getPythonModuleForTemplate(Utils.getPathToResources("author/project/"+lpatternname+".py"));
        Module rpatternModule = Utils.getPythonModuleForTemplate(Utils.getPathToResources("author/project/"+rpatternname+".py"));
        List<MatchedNode> matchedNodes = getMatchedNodes(filename, lpatternname,rpatternname, codeModule, lpatternModule,rpatternModule);
        List<MatchedNode> allMatchedGraphs = matchedNodes.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList());

        AdaptRule aRule= new AdaptRule(allMatchedGraphs.get(0),Utils.getAllFunctions(codeModule).get(0) ,rpatternModule);
        Rule rule  = aRule.getAdaptedRule();
        assertEquals(
                "def test_application_pretrained_weights_loading(self):\n" +
                "    :[l4]\n" +
                "    :[l5]\n" +
                "    :[l6]\n" +
                "    for app in apps:\n" +
                "        :[l7]\n" +
                "        :[l8]\n" +
                "        :[l9]\n" +
                "        yy = np.dot(np.dot(:[[l1]], :[[l2]]), :[[l3]])\n" +
                "        :[l10]\n" +
                "        :[l11]\n" +
                "        :[l12]\n" +
                "        :[l13]",rule.getLHS());
        assertEquals(
                "def test_application_pretrained_weights_loading(self):\n" +
                "    :[l4]\n" +
                "    :[l5]\n" +
                "    :[l6]\n" +
                "    for app in apps:\n" +
                "        :[l7]\n" +
                "        :[l8]\n" +
                "        :[l9]\n" +
                "        yy = np.multidot(:[[l1]], :[[l2]], :[[l3]])\n" +
                "        :[l10]\n" +
                "        :[l11]\n" +
                "        :[l12]\n" +
                "        :[l13]",rule.getRHS());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testAdaptedRule6() throws Exception {
        String filename="test37";
        String lpatternname = "lpattern18";
        String rpatternname = "rpattern18";
        Module codeModule = Utils.getPythonModule("author/project/"+filename+".py");
        Module lpatternModule = Utils.getPythonModuleForTemplate(Utils.getPathToResources("author/project/"+lpatternname+".py"));
        Module rpatternModule = Utils.getPythonModuleForTemplate(Utils.getPathToResources("author/project/"+rpatternname+".py"));
        List<MatchedNode> matchedNodes = getMatchedNodes(filename, lpatternname,rpatternname, codeModule, lpatternModule,rpatternModule);
        List<MatchedNode> allMatchedGraphs = matchedNodes.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList());
        AdaptRule aRule= new AdaptRule(allMatchedGraphs.get(0),Utils.getAllFunctions(codeModule).get(0) ,rpatternModule);
        Rule rule  = aRule.getAdaptedRule();
        assertEquals(
                "def test_application_pretrained_weights_loading(self):\n" +
                "    :[l4]\n" +
                "    :[l5]\n" +
                "    :[l6]\n" +
                "    for app in apps:\n" +
                "        :[l7]\n" +
                "        uu = np.dot(:[[l1]], :[[l2]])\n" +
                "        :[l8]\n" +
                "        :[l9]\n" +
                "        yy = np.multidot(:[[l1]], :[[l2]], :[[l3]])\n" +
                "        :[l10]\n" +
                "        :[l11]\n" +
                "        :[l12]\n" +
                "        :[l13]",rule.getRHS());
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

    @Test
    void testAdaptedRule7() throws Exception {
        String filename="test34";
        String lpatternname = "lpattern19";
        String rpatternname = "rpattern19";
        Module codeModule = Utils.getPythonModule("author/project/"+filename+".py");
        Module lpatternModule = Utils.getPythonModuleForTemplate(Utils.getPathToResources("author/project/"+lpatternname+".py"));
        Module rpatternModule = Utils.getPythonModuleForTemplate(Utils.getPathToResources("author/project/"+rpatternname+".py"));
        List<MatchedNode> matchedNodes = getMatchedNodes(filename, lpatternname,rpatternname, codeModule, lpatternModule,rpatternModule);
        List<MatchedNode> allMatchedGraphs = matchedNodes.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList());
        AdaptRule aRule= new AdaptRule(allMatchedGraphs.get(0),Utils.getAllFunctions(codeModule).get(0) ,rpatternModule);
        Rule rule  = aRule.getAdaptedRule();
    }

    @Test
    void testAdaptedRule8() throws Exception {
        String filename = "test38";
        String lpatternname = "pattern17";
        String rpatternname = "r_pattern17";
        Module codeModule = Utils.getPythonModule("author/project/" + filename + ".py");
        Module lpatternModule = Utils.getPythonModuleForTemplate(Utils.getPathToResources("author/project/" + lpatternname + ".py"));
        Module rpatternModule = Utils.getPythonModuleForTemplate(Utils.getPathToResources("author/project/" + rpatternname + ".py"));
        List<MatchedNode> matchedNodes = getMatchedNodes(filename, lpatternname, rpatternname, codeModule, lpatternModule, rpatternModule);
        List<MatchedNode> allMatchedGraphs = matchedNodes.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList());
        AdaptRule aRule = new AdaptRule(allMatchedGraphs.get(0), Utils.getAllFunctions(codeModule).get(0), rpatternModule);
        Rule rule = aRule.getAdaptedRule();
        System.out.println(rule.getLHS());
        System.out.println(rule.getRHS());
    }


}