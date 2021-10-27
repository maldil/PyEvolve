package com.inferrules.core;

import com.google.common.collect.Sets;
import com.inferrules.core.languageAdapters.ILanguageAdapter;
import com.inferrules.core.languageAdapters.LanguageSpecificInfo;

import java.util.Collection;

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
        Collection<TemplateVariable> commonVars = Sets.intersection(beforeTemplateNode.getTemplateVariableSet(),
                afterTemplateNode.getTemplateVariableSet());
        Collection<TemplateVariable> varsOnlyInBefore = Sets.difference(beforeTemplateNode.getTemplateVariableSet(),
                afterTemplateNode.getTemplateVariableSet())
                .immutableCopy();
        Collection<TemplateVariable> varOnlyInAfter = Sets.difference(afterTemplateNode.getTemplateVariableSet(),
                beforeTemplateNode.getTemplateVariableSet())
                .immutableCopy();
//        var varTree = afterTemplate.getOptimumTemplateNode().getTemplateVariableTree(TemplateVariable.getDummy());
        // remove leaves if parent is present
        afterTemplateNode = afterTemplateNode.surfaceTemplateVariables(commonVars, afterTemplate.getAllTokens());
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
