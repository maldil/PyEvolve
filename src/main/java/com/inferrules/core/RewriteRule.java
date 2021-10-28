package com.inferrules.core;

import com.google.common.collect.ImmutableSet;
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

    private ImmutableSet<TemplateVariable> getTemplateVariablesToConcretize(TemplateNode afterTemplateNode, TemplateNode beforeTemplateNode, Set<TemplateVariable> repeatedVars) {
        return Sets.difference(Sets.difference(beforeTemplateNode.getTemplateVariableSet(),
                afterTemplateNode.getTemplateVariableSet()).immutableCopy(), repeatedVars).immutableCopy();
    }

    public TemplateNode getMatch() {
        return Match;
    }

    public TemplateNode getReplace() {
        return Replace;
    }

}
