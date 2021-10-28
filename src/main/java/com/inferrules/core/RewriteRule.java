package com.inferrules.core;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.inferrules.core.languageAdapters.LanguageSpecificInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class RewriteRule {

    private final TemplateNode Match;
    private final TemplateNode Replace;

    public RewriteRule(String beforeSnippet, String afterSnippet, LanguageSpecificInfo.Language language) {

        VariableNameGenerator l = new VariableNameGenerator('l');
        var beforeTemplate = new Template(beforeSnippet, language, l);
        l.resetButKeepCache('r');
        var afterTemplate = new Template(afterSnippet, language, l);

        TemplateNode afterNode = afterTemplate.getOptimumTemplateNode();
        TemplateNode beforeNode = beforeTemplate.getOptimumTemplateNode();

        Set<TemplateVariable> repeatedVars = new HashSet<>(beforeNode.getRepeatedTemplateVariables());

        Collection<TemplateVariable> commonVars = Sets.intersection(beforeNode.getTemplateVariableSet(),
                afterNode.getTemplateVariableSet());
        // var varTree = afterTemplate.getOptimumTemplateNode().getTemplateVariableTree(TemplateVariable.getDummy());
        // remove leaves if parent is present
        afterNode = afterNode.surfaceTemplateVariables(commonVars, afterTemplate.getAllTokens());
        beforeNode = beforeNode.surfaceTemplateVariables(commonVars, beforeTemplate.getAllTokens());

        Collection<TemplateVariable> varsOnlyInBefore = getTemplateVariablesToConcretize(afterNode, beforeNode, repeatedVars);
        Collection<TemplateVariable> varOnlyInAfter = getTemplateVariablesToConcretize(beforeNode, afterNode, repeatedVars);

        this.Match = beforeNode.concretizeTemplateVars(varsOnlyInBefore, beforeTemplate.getAllTokens());
        this.Replace = afterNode.concretizeTemplateVars(varOnlyInAfter, afterTemplate.getAllTokens());
    }

    /**
     * @param t1
     * @param t2
     * @param repeatedVars
     * @return vars(t1) - vars(t2) - repeatedVars
     */
    private ImmutableSet<TemplateVariable> getTemplateVariablesToConcretize(TemplateNode t1, TemplateNode t2, Set<TemplateVariable> repeatedVars) {
        return Sets.difference(Sets.difference(t2.getTemplateVariableSet(), t1.getTemplateVariableSet())
                .immutableCopy(), repeatedVars).immutableCopy();
    }

    public TemplateNode getMatch() {
        return Match;
    }

    public TemplateNode getReplace() {
        return Replace;
    }

}
