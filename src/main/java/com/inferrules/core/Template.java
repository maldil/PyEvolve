package com.inferrules.core;

import com.google.common.graph.Traverser;
import com.inferrules.core.languageAdapters.ILanguageAdapter;
import com.inferrules.core.languageAdapters.Language;
import io.vavr.Tuple;
import io.vavr.Tuple2;

import java.util.List;

import static com.inferrules.utils.Utilities.traverseTree;
import static java.util.stream.Collectors.toList;

public class Template {
    /**
     * This is the coarsest representation of the input code snippet as a TemplateTree.
     * This tree is obtained by mapping the input code snippet -> parse tree ->  Node -> TemplateNode
     * @see com.inferrules.core.Node
     * Usually at the first level it has template variables capturing the entire statement.
     */
    private final Tuple2<TemplateVariable, TemplateNode> UnflattendTemplateNode;

    /**
     * This is the most fine grained representation of the CoarsestTemplateNode.
     * All the leaf template nodes in the CoarsestTemplateNode are surfaced.
     * @see com.inferrules.core.TemplateNode #surfaceTemplateVariables
     */
    private final Tuple2<TemplateVariable, TemplateNode> CompletelyFlattenedTemplateNode;
    private final List<String> AllTokens;
    public static final Traverser<Tuple2<TemplateVariable, TemplateNode>> TreeTraverser = Traverser.forTree(t -> t._2().getTemplateVarsMapping());

    public Template(String codeSnippet,  Language language, VariableNameGenerator nameGenerator) {
        ILanguageAdapter languageAdapter = language.getAdapter();
        codeSnippet = languageAdapter.removeComments(codeSnippet);
        var root = languageAdapter.parse(codeSnippet);
        AllTokens = languageAdapter.tokenize(codeSnippet);
        UnflattendTemplateNode = Tuple.of(TemplateVariable.getDummy(), new TemplateNode(root, nameGenerator, AllTokens));
        List<TemplateVariable> leafTvs = traverseTree(UnflattendTemplateNode)
                .filter(x -> x._2().isLeaf()).map(Tuple2::_1).collect(toList());
        CompletelyFlattenedTemplateNode = Tuple.of(TemplateVariable.getDummy(),UnflattendTemplateNode._2().surfaceTemplateVariables(leafTvs,AllTokens));
    }
    public List<String> getAllTokens() {
        return AllTokens;
    }
    public Tuple2<TemplateVariable, TemplateNode> getUnflattendTemplateNode() {
        return UnflattendTemplateNode;
    }
    public Tuple2<TemplateVariable, TemplateNode> getCompletelyFlattenedTemplateNode() {
        return CompletelyFlattenedTemplateNode;
    }
}

