package com.inferrules.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.inferrules.core.languageAdapters.ILanguageAdapter;
import io.vavr.collection.Tree;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class Template {

    private final TemplateNode CompleteTemplate;
    private final TemplateNode OptimumTemplateNode;

    public Template(String codeSnippet, ILanguageAdapter languageAdapter, VariableNameGenerator nameGenerator) {
        var root = languageAdapter.parse(codeSnippet);
        List<String> allTokens = languageAdapter.tokenize(codeSnippet);
        CompleteTemplate = new TemplateNode(root, nameGenerator, allTokens);
        var templateVariableTree = getTemplateVariableTree(CompleteTemplate, new TemplateVariable("dummy","abc"));
        List<TemplateVariable> repeatedLeafTvs = CompleteTemplate.getRepeatedTemplateVariables().stream().filter(x -> !templateVariableTree.traverse()
                                            .filter(z -> z.get().equals(x) && z.isLeaf()).isEmpty()).collect(toList());
        OptimumTemplateNode = CompleteTemplate.surfaceTemplateVariables(repeatedLeafTvs, allTokens);
    }

    public TemplateNode getOptimumTemplateNode() {
        return OptimumTemplateNode;
    }
    public TemplateNode getCompleteTemplate() {
        return CompleteTemplate;
    }

    public Tree.Node<TemplateVariable> getTemplateVariableTree(TemplateNode tn, TemplateVariable tv) {
         List<Tree.Node<TemplateVariable>> cs = tn.getTemplateVarsMapping().stream()
                .map(x -> new Tree.Node<>(x._1(), io.vavr.collection.List.ofAll(x._2().getTemplateVarsMapping().stream().map(y -> getTemplateVariableTree(y._2(), y._1()))
                        .collect(toList()))))
                .collect(toList());
         return new Tree.Node<>(tv, io.vavr.collection.List.ofAll(cs));
    }

    public String toJSON(){
        return new GsonBuilder().disableHtmlEscaping().create().toJson(this, Template.class);
    }
}

