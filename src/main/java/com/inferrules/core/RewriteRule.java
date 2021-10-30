package com.inferrules.core;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.inferrules.core.languageAdapters.Language;
import com.inferrules.utils.Utilities;
//import io.vavr.collection.Tree;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RewriteRule {

    private final TemplateNode Match;
    private final TemplateNode Replace;

    public RewriteRule(String beforeSnippet, String afterSnippet,  Language language) {

        VariableNameGenerator l = new VariableNameGenerator('l');
        var beforeTemplate = new Template(beforeSnippet, language, l);
        l.resetButKeepCache('r');
        var afterTemplate = new Template(afterSnippet, language, l);

        var afterNode = afterTemplate.getUnflattendTemplateNode();
        var beforeNode = beforeTemplate.getUnflattendTemplateNode();

        ImmutableSet<TemplateVariable> intersectingNodes = Sets.intersection(afterNode._2().getTemplateVariableSet(),
                beforeNode._2().getTemplateVariableSet()).immutableCopy();
        Set<TemplateVariable> repeatedTemplateVariables_Before = beforeNode._2().getRepeatedTemplateVariables();

        Set<TemplateVariable> removeIntersectingNodes =  intersectingNodes.stream().flatMap(x -> {
            var before = Utilities.findInTree(beforeNode, x);
            var after = Utilities.findInTree(afterNode, x);;
            if(before._2().isLeaf() || after._2().isLeaf() || before._2().getTemplateVarsMapping().size() != after._2().getTemplateVarsMapping().size())
                return Stream.empty();
            if(Utilities.traverseTree(after).allMatch(y -> Utilities.traverseTree(before).anyMatch(z -> y._1().equals(z._1())))){
                if(Utilities.traverseTree(before).noneMatch(z -> repeatedTemplateVariables_Before.contains(z._1())))
                    return Stream.concat(Utilities.traverseTree(after), Utilities.traverseTree(before))
                            .filter(f -> !f._1().equals(x));
                else return Stream.of(before);
            }
            return Stream.empty();
        }).map(x->x._1()).collect(Collectors.toSet());

        Set<TemplateVariable> commonVars = Sets.difference(intersectingNodes,removeIntersectingNodes);

        TemplateNode match = beforeNode._2().surfaceTemplateVariables(Sets.union(commonVars, repeatedTemplateVariables_Before), beforeTemplate.getAllTokens());
        TemplateNode replace = afterNode._2().surfaceTemplateVariables(commonVars, afterTemplate.getAllTokens());

        Collection<TemplateVariable> varsOnlyInBefore = Sets.difference(Sets.difference(match.getTemplateVariableSet(), replace.getTemplateVariableSet())
                .immutableCopy(), Sets.union(commonVars, repeatedTemplateVariables_Before)).immutableCopy();
        Collection<TemplateVariable> varOnlyInAfter = Sets.difference(Sets.difference(replace.getTemplateVariableSet(), match.getTemplateVariableSet())
                .immutableCopy(), commonVars).immutableCopy();

        this.Match = match.concretizeTemplateVariables(varsOnlyInBefore, beforeTemplate.getAllTokens());
        this.Replace = replace.concretizeTemplateVariables(varOnlyInAfter, afterTemplate.getAllTokens());
    }

    public TemplateNode getMatch() {
        return Match;
    }

    public TemplateNode getReplace() {
        return Replace;
    }

}
