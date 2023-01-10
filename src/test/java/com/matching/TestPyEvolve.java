package com.matching;

import com.adaptrule.AdaptRule;
import com.adaptrule.Rule;
import com.inferrules.comby.jsonResponse.CombyRewrite;
import com.inferrules.comby.operations.BasicCombyOperations;
import com.matching.fgpdg.MatchedNode;
import io.vavr.control.Try;
import org.inferrules.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Module;

import java.util.List;
import java.util.stream.Collectors;

import static org.inferrules.Utils.getMatchedNodes;

public class TestPyEvolve {
    @Test
    void testPipeline1() throws Exception {
        String filename="test26";
        String lpatternname = "pattern12";
        String rpatternname = "r_pattern12";
        BasicCombyOperations op = new BasicCombyOperations();

        Module codeModule = Utils.getPythonModule("author/project/"+filename+".py");
        String code = codeModule.getInternalBody().get(1).toString();
        Module lpatternModule = Utils.getPythonModuleForTemplate(Utils.getPathToResources("author/project/"+lpatternname+".py"));
        Module rpatternModule = Utils.getPythonModuleForTemplate(Utils.getPathToResources("author/project/"+rpatternname+".py"));
        List<MatchedNode> matchedNodes = getMatchedNodes(filename, lpatternname,rpatternname, codeModule, lpatternModule,rpatternModule);
        List<MatchedNode> allMatchedGraphs = matchedNodes.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList());
        AdaptRule aRule= new AdaptRule(allMatchedGraphs.get(0), (FunctionDef) codeModule.getInternalBody().get(1),rpatternModule);
        Rule rule = aRule.getAdaptedRule();
        Try<CombyRewrite> changedCode = op.rewrite(rule.getLHS(), rule.getRHS(), code, ".python");
        Assertions.assertEquals("def function1(sentence, callbacks):\n" +
                "    ff = {one:1,two:2}\n" +
                "    print(ff)\n" +
                "    z = np.sum(ff.values())\n" +
                "return z\n",changedCode.get().getRewrittenSource());
    }

    @Test
    void testPipelineForProject() throws Exception {
        String filename="test26";
        String lpatternname = "pattern12";
        String rpatternname = "r_pattern12";
        BasicCombyOperations op = new BasicCombyOperations();

        Module codeModule = Utils.getPythonModule("author/project/"+filename+".py");
        String code = codeModule.getInternalBody().get(1).toString();
        Module lpatternModule = Utils.getPythonModuleForTemplate(Utils.getPathToResources("author/project/"+lpatternname+".py"));
        Module rpatternModule = Utils.getPythonModuleForTemplate(Utils.getPathToResources("author/project/"+rpatternname+".py"));
        List<MatchedNode> matchedNodes = getMatchedNodes(filename, lpatternname,rpatternname, codeModule, lpatternModule,rpatternModule);
        List<MatchedNode> allMatchedGraphs = matchedNodes.stream().filter(MatchedNode::isAllChildsMatched).collect(Collectors.toList());
        AdaptRule aRule= new AdaptRule(allMatchedGraphs.get(0), (FunctionDef) codeModule.getInternalBody().get(1),rpatternModule);
        Rule rule = aRule.getAdaptedRule();
        Try<CombyRewrite> changedCode = op.rewrite(rule.getLHS(), rule.getRHS(), code, ".python");
        Assertions.assertEquals("def function1(sentence, callbacks):\n" +
                "    ff = {one:1,two:2}\n" +
                "    print(ff)\n" +
                "    z = np.sum(ff.values())\n" +
                "return z\n",changedCode.get().getRewrittenSource());
    }
}
