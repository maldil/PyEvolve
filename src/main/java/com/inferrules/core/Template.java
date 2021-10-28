package com.inferrules.core;

import com.google.common.collect.Iterables;
import com.google.gson.GsonBuilder;
import com.inferrules.core.languageAdapters.ILanguageAdapter;
import com.inferrules.core.languageAdapters.LanguageSpecificInfo;
import io.vavr.collection.Traversable;
import io.vavr.collection.Tree;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class Template {
    /**
     * This is the coarsest representation of the input code snippet as a TemplateTree.
     * This tree is obtained by mapping the input code snippet -> parse tree ->  Node -> TemplateNode
     * @see com.inferrules.core.Node
     * Usually at the first level it has template variables capturing the entire statement.
     */
    private final TemplateNode CoarsestTemplateNode;
    /**
     * This is a more optimal representation of the Coarsest Template Node.
     * This is more optimal because surfaces all the template variables that are repeated within the template.
     * @see com.inferrules.core.TemplateNode #surfaceTemplateVariables
     */
    private final TemplateNode OptimumTemplateNode;

    /**
     * This is the most fine grained representation of the CoarsestTemplateNode.
     * All the leaf template nodes in the CoarsestTemplateNode are surfaced.
     * @see com.inferrules.core.TemplateNode #surfaceTemplateVariables
     */
    private final TemplateNode FinestTemplateNode;

    private final List<String> AllTokens;

    public Template(String codeSnippet, LanguageSpecificInfo.Language language, VariableNameGenerator nameGenerator) {
        ILanguageAdapter languageAdapter = LanguageSpecificInfo.getAdapter(language);
        var root = languageAdapter.parse(codeSnippet);
        AllTokens = languageAdapter.tokenize(codeSnippet);
        CoarsestTemplateNode = new TemplateNode(root, nameGenerator, AllTokens);
        var templateVariableTree = CoarsestTemplateNode.getTemplateVariableTree(TemplateVariable.getDummy());
        List<TemplateVariable> leafTvs = templateVariableTree.traverse()
                .filter(Tree.Node::isLeaf)
                .map(Traversable::get).collect(toList());
        List<TemplateVariable> repeatedLeafTvs = CoarsestTemplateNode.getRepeatedTemplateVariables().stream()
                .filter(leafTvs::contains).collect(toList());
        OptimumTemplateNode = CoarsestTemplateNode.surfaceTemplateVariables(repeatedLeafTvs, AllTokens);
        FinestTemplateNode = CoarsestTemplateNode.surfaceTemplateVariables(leafTvs,AllTokens);
    }

    public List<String> getAllTokens() {
        return AllTokens;
    }

    public TemplateNode getOptimumTemplateNode() {
        return OptimumTemplateNode;
    }
    public TemplateNode getCoarsestTemplateNode() {
        return CoarsestTemplateNode;
    }



    public String toJSON(){
        return new GsonBuilder().disableHtmlEscaping().create().toJson(this, Template.class);
    }

    public TemplateNode getFinestTemplateNode() {
        return FinestTemplateNode;
    }
}

