package com.inferrules.core;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.inferrules.core.languageAdapters.ILanguageAdapter;
import com.inferrules.core.languageAdapters.LanguageSpecificInfo;

import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class RewriteRule {

    private final Template Before;
    private final Template After;
    private String beforeSnippet;
    private String afterSnippet;

    public RewriteRule(String beforeSnippet, String afterSnippet, LanguageSpecificInfo.Language language) {
        this.beforeSnippet = beforeSnippet;
        this.afterSnippet = afterSnippet;
        ILanguageAdapter adapter = LanguageSpecificInfo.getAdapter(language);
        VariableNameGenerator l = new VariableNameGenerator('l');
        this.Before = new Template(beforeSnippet, adapter, l);
        l.resetButKeepCache('r');
        this.After = new Template(afterSnippet, adapter, l);
        Set<TemplateVariable> intrx = Before.getOptimumTemplateNode().getAllVariables()
                .stream().filter(x -> After.getOptimumTemplateNode().getAllVariables().stream().anyMatch(y -> y.equals(x)))
                .collect(Collectors.toSet());


    }









//    public Map<String, ValueDifference<TemplateVariable>> intersection(){
//        return Maps.difference(Before.getCodeToTemplateVars(), After.getCodeToTemplateVars())
//                .entriesDiffering();
//    }

}
