package com.inferrules.core;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.inferrules.core.languageAdapters.LanguageSpecificInfo;
import io.vavr.collection.Tree;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RewriteRule {

    private final TemplateNode Match;
    private final TemplateNode Replace;

    public RewriteRule(String beforeSnippet, String afterSnippet, LanguageSpecificInfo.Language language) {

        VariableNameGenerator l = new VariableNameGenerator('l');
        var beforeTemplate = new Template(beforeSnippet, language, l);
        l.resetButKeepCache('r');
        var afterTemplate = new Template(afterSnippet, language, l);

        TemplateNode afterNode = afterTemplate.getUnflattendTemplateNode();
        TemplateNode beforeNode = beforeTemplate.getUnflattendTemplateNode();

        ImmutableSet<TemplateVariable> intersectingNodes = Sets.intersection(afterNode.getTemplateVariableSet(), beforeNode.getTemplateVariableSet()).immutableCopy();
        Tree.Node<TemplateVariable> afterVarsTree = afterTemplate.getUnflattendTemplateNode().getTemplateVariableTree(TemplateVariable.getDummy());
        Tree.Node<TemplateVariable> beforeVarsTree = beforeTemplate.getUnflattendTemplateNode().getTemplateVariableTree(TemplateVariable.getDummy());

        List<TemplateVariable> repeatedTemplateVariables_Before = beforeNode.getRepeatedTemplateVariables();
        Set<TemplateVariable> removeIntersectingNodes = intersectingNodes.stream().flatMap(x -> {
                var b = beforeVarsTree.traverse().find(n -> n.get().equals(x)).get();
                var a = afterVarsTree.traverse().find(n -> n.get().equals(x)).get();
                if (b.getChildren().isEmpty() || a.getChildren().isEmpty() || b.getChildren().size() != a.getChildren().size())
                    return Stream.empty();
                if (a.getChildren().toJavaStream().allMatch(y -> b.getChildren().toJavaStream().anyMatch(z -> y.get().equals(z.get()))))
                    if(b.traverse().toJavaStream().noneMatch(z -> repeatedTemplateVariables_Before.contains(z.get())))
                        return Stream.concat(a.getChildren().toJavaStream().map(z -> z.get()), b.getChildren().toJavaStream().map(z -> z.get()));
                    else return Stream.of(x);
                return Stream.empty();
        }).collect(Collectors.toSet());

        Set<TemplateVariable> commonVars = Sets.difference(intersectingNodes,removeIntersectingNodes);

        afterNode = afterNode.surfaceTemplateVariables(commonVars, afterTemplate.getAllTokens());
        beforeNode = beforeNode.surfaceTemplateVariables(Sets.union(commonVars,new HashSet<>(repeatedTemplateVariables_Before)), beforeTemplate.getAllTokens());

        Collection<TemplateVariable> varsOnlyInBefore = getTemplateVariablesToConcretize(afterNode, beforeNode, Sets.union(commonVars,new HashSet<>(repeatedTemplateVariables_Before)));
        Collection<TemplateVariable> varOnlyInAfter = getTemplateVariablesToConcretize(beforeNode, afterNode, commonVars);

        this.Match = beforeNode.concretizeTemplateVariables(varsOnlyInBefore, beforeTemplate.getAllTokens());
        this.Replace = afterNode.concretizeTemplateVariables(varOnlyInAfter, afterTemplate.getAllTokens());
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
