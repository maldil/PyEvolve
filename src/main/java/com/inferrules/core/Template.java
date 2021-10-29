package com.inferrules.core;

import com.inferrules.core.languageAdapters.ILanguageAdapter;
import com.inferrules.core.languageAdapters.LanguageSpecificInfo;
import io.vavr.collection.Traversable;
import io.vavr.collection.Tree;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class Template {
    /**
     * This is the coarsest representation of the input code snippet as a TemplateTree.
     * This tree is obtained by mapping the input code snippet -> parse tree ->  Node -> TemplateNode
     * @see com.inferrules.core.Node
     * Usually at the first level it has template variables capturing the entire statement.
     */
    private final TemplateNode UnflattendTemplateNode;
//    /**
//     * This is a more optimal representation of the Coarsest Template Node.
//     * This is more optimal because surfaces all the template variables that are repeated within the template.
//     * @see com.inferrules.core.TemplateNode #surfaceTemplateVariables
//     */
//    private final TemplateNode OptimumTemplateNode;

    /**
     * This is the most fine grained representation of the CoarsestTemplateNode.
     * All the leaf template nodes in the CoarsestTemplateNode are surfaced.
     * @see com.inferrules.core.TemplateNode #surfaceTemplateVariables
     */
    private final TemplateNode CompletelyFlattenedTemplateNode;
    private final List<String> AllTokens;

    public Template(String codeSnippet, LanguageSpecificInfo.Language language, VariableNameGenerator nameGenerator) {
        ILanguageAdapter languageAdapter = LanguageSpecificInfo.getAdapter(language);
        var root = languageAdapter.parse(codeSnippet);
        AllTokens = languageAdapter.tokenize(codeSnippet);
        UnflattendTemplateNode = new TemplateNode(root, nameGenerator, AllTokens);
        var templateVariableTree = UnflattendTemplateNode.getTemplateVariableTree(TemplateVariable.getDummy());
        List<TemplateVariable> leafTvs = templateVariableTree.traverse()
                .filter(Tree.Node::isLeaf)
                .map(Traversable::get).collect(toList());
        List<TemplateVariable> repeatedLeafTvs = UnflattendTemplateNode.getRepeatedTemplateVariables().stream()
                .filter(leafTvs::contains).collect(toList());
//        OptimumTemplateNode = UnflattendTemplateNode.surfaceTemplateVariables(repeatedLeafTvs, AllTokens);
        CompletelyFlattenedTemplateNode = UnflattendTemplateNode.surfaceTemplateVariables(leafTvs,AllTokens);
    }
    public List<String> getAllTokens() {
        return AllTokens;
    }
//    public TemplateNode getOptimumTemplateNode() {
//        return OptimumTemplateNode;
//    }
    public TemplateNode getUnflattendTemplateNode() {
        return UnflattendTemplateNode;
    }
    public TemplateNode getCompletelyFlattenedTemplateNode() {
        return CompletelyFlattenedTemplateNode;
    }
}

