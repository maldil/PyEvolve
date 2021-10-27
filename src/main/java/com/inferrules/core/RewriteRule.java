package com.inferrules.core;

import com.google.common.collect.Sets;
import com.inferrules.core.languageAdapters.ILanguageAdapter;
import com.inferrules.core.languageAdapters.LanguageSpecificInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class RewriteRule {

    private final TemplateNode Match;

    private final TemplateNode Replace;
    public RewriteRule(String beforeSnippet, String afterSnippet, LanguageSpecificInfo.Language language) {
        ILanguageAdapter adapter = LanguageSpecificInfo.getAdapter(language);
        VariableNameGenerator l = new VariableNameGenerator('l');
        var beforeTemplate = new Template(beforeSnippet, adapter, l);
        l.resetButKeepCache('r');
        var afterTemplate = new Template(afterSnippet, adapter, l);


        TemplateNode afterTemplateNode = afterTemplate.getOptimumTemplateNode();
        TemplateNode beforeTemplateNode = beforeTemplate.getOptimumTemplateNode();

        Set<TemplateVariable> repeatedVars = beforeTemplateNode.getRepeatedTemplateVariables().stream().collect(Collectors.toSet());

        Collection<TemplateVariable> commonVars = Sets.intersection(beforeTemplateNode.getTemplateVariableSet(),
                afterTemplateNode.getTemplateVariableSet());
        afterTemplateNode = afterTemplateNode.surfaceTemplateVariables(commonVars, afterTemplate.getAllTokens());
        beforeTemplateNode = beforeTemplateNode.surfaceTemplateVariables(commonVars, beforeTemplate.getAllTokens());

        Collection<TemplateVariable> varsOnlyInBefore = Sets.difference(Sets.difference(beforeTemplateNode.getTemplateVariableSet(),
                afterTemplateNode.getTemplateVariableSet()).immutableCopy(), repeatedVars).immutableCopy();

        Collection<TemplateVariable> varOnlyInAfter = Sets.difference(Sets.difference(afterTemplateNode.getTemplateVariableSet(),
                beforeTemplateNode.getTemplateVariableSet()).immutableCopy(), repeatedVars).immutableCopy();
//        var varTree = afterTemplate.getOptimumTemplateNode().getTemplateVariableTree(TemplateVariable.getDummy());
        // remove leaves if parent is present
        this.Match = beforeTemplateNode.concretizeTemplateVars(varsOnlyInBefore, beforeTemplate.getAllTokens());
        this.Replace = afterTemplateNode.concretizeTemplateVars(varOnlyInAfter, afterTemplate.getAllTokens());
        System.out.println();
    }

    public TemplateNode getMatch() {
        return Match;
    }

    public TemplateNode getReplace() {
        return Replace;
    }









//    public Map<String, ValueDifference<TemplateVariable>> intersection(){
//        return Maps.difference(Before.getCodeToTemplateVars(), After.getCodeToTemplateVars())
//                .entriesDiffering();
//    }

}
