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

        AdaptRule aRule= new AdaptRule(allMatchedGraphs.get(0),codeModule,rpatternModule);
        Module renamedRule = aRule.getAdaptedRule();
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

        AdaptRule aRule= new AdaptRule(allMatchedGraphs.get(0),codeModule,rpatternModule);
        Module renamedRule = aRule.getAdaptedRule();
//        Assertions.assertEquals(23,graphs.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList()).get(0).getCodePDGNodes().size());
    }

}