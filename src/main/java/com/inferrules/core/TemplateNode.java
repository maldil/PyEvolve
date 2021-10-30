package com.inferrules.core;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Tree;
import org.antlr.v4.runtime.misc.Interval;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;


/**
 * This class represents the a node in the tree of of Templates that is constructed from a @link com.inferrules.code.Node.
 * Each non-keyword/non-key-symbol child of Node is replaced by a hole. Then for each of the nodes captured by these holes,
 * we recurssive construct a template node.
 */
public class TemplateNode {
    private final String CodeSnippet;

    private final String Template;
    private final List<Tuple2<TemplateVariable, TemplateNode>> TemplateVarsMapping;
    private final Interval SourceInterval;
    private final boolean isLeaf;

    public TemplateNode(Node r, VariableNameGenerator nameGenerator, List<String> allTokens) {
        var n = compress(r, r);
        CodeSnippet = n.getValue();
        SourceInterval = n.getSourceInterval();
        TemplateVarsMapping = n.getChildren().stream()
                .filter(Node::isNotKwdOrSymb)
                .map(x -> Tuple.of(nameGenerator.getNameOrElseNew(x),
                                                new TemplateNode(x, nameGenerator, allTokens)))
                .collect(toList());

        Template = constructTemplate(TemplateVarsMapping, n.getSourceInterval(), allTokens);
        isLeaf = TemplateVarsMapping.isEmpty();
    }

    TemplateNode(String template, List<Tuple2<TemplateVariable, TemplateNode>> templateVariableMapping,
                 String codeSnippet, Interval sourceInterval) {
        this.Template = template;
        this.TemplateVarsMapping = templateVariableMapping;
        this.CodeSnippet = codeSnippet;
        this.SourceInterval = sourceInterval;
        isLeaf = TemplateVarsMapping.isEmpty();
    }

    public Tree.Node<TemplateVariable> getTemplateVariableTree(TemplateVariable tv) {
        List<Tree.Node<TemplateVariable>> cs = getTemplateVarsMapping().stream()
                .map(x -> new Tree.Node<>(x._1(), io.vavr.collection.List.ofAll(x._2().getTemplateVarsMapping().stream().map(y -> y._2().getTemplateVariableTree(y._1()))
                        .collect(toList()))))
                .collect(toList());
        return new Tree.Node<>(tv, io.vavr.collection.List.ofAll(cs));
    }

    /**
     * @param compareTo This is the input node.
     * @param n This is also the input node
     * @return If the parent and the child nodes are equal, it returns the child node, else the input node itself.
     * This compresses the TemplateNode Tree.
     */
    private Node compress(Node compareTo, Node n ){
        return n.getChildren().stream().filter(x -> x.equals(compareTo))
                .findFirst().map(node -> compress(compareTo, node)).orElse(n);
    }


    /**
     *
     * @param templateVariables The collection of template variables that need to be surfaced to the top
     * @param allTokens All the tokens of the code snippet represented by the template
     * @return returns a new TemplateNode where all the @param templateVariables are its children.
     * Basically it systematically pushes t \in templateVariables to level 1(children of the root node) of the TemplateNode tree.
     */
    public TemplateNode surfaceTemplateVariables(Collection<TemplateVariable> templateVariables, List<String> allTokens){
        TemplateNode r = new TemplateNode(this.Template, new ArrayList<>(this.TemplateVarsMapping),
                this.CodeSnippet, this.SourceInterval);
        List<Tuple2<TemplateVariable, TemplateNode>> variableMapping = surfaceTemplateVariablesHelper(templateVariables, r);
        var newTemplate = constructTemplate(variableMapping, SourceInterval,allTokens);
        return new TemplateNode(newTemplate, variableMapping, r.CodeSnippet, r.SourceInterval);
    }

    private List<Tuple2<TemplateVariable, TemplateNode>> surfaceTemplateVariablesHelper(Collection<TemplateVariable> tmpltVars, TemplateNode root){
        return root.getTemplateVarsMapping().stream()
                .flatMap(varMapping -> tmpltVars.contains(varMapping._1()) || tmpltVars.stream().noneMatch(x -> varMapping._2().isDescendant(x))
                        ? Stream.of(varMapping)
                        : varMapping._2().getTemplateVarsMapping().isEmpty()
                            ? Stream.empty()
                            : surfaceTemplateVariablesHelper(tmpltVars, varMapping._2()).stream())
                .collect(toList());
    }

    public String getCodeSnippet() {
        return CodeSnippet;
    }

    /**
     *
     * @param templateVariables
     * @param allTokens All the tokens of the code snippet represented by the template
     * @return returns a new template where the given template variables are substituted with the associated values,
     * if `t \in templateVariables` and `t \in this.Children`
     */
    public TemplateNode concretizeTemplateVariables(Collection<TemplateVariable> templateVariables, List<String> allTokens){
        var ls = this.TemplateVarsMapping.stream().filter(x-> !templateVariables.contains(x._1())).collect(toList());
        String newTemplate = constructTemplate(ls, SourceInterval, allTokens);
        return new TemplateNode(newTemplate, ls, CodeSnippet, SourceInterval);
    }

    public List<TemplateVariable> getAllVariables() {
        return TemplateVarsMapping.stream()
                .flatMap(x -> Stream.concat(Stream.of(x._1()), x._2.getAllVariables().stream()))
                .collect(toList());
    }


    public Set<TemplateVariable> getTemplateVariableSet() {
        return TemplateVarsMapping.stream()
                .flatMap(x -> Stream.concat(Stream.of(x._1()), x._2.getAllVariables().stream()))
                .collect(toSet());
    }


    public Set<TemplateVariable> getRepeatedTemplateVariables() {
        return TemplateVarsMapping.stream()
                .flatMap(x -> Stream.concat(Stream.of(x._1()), x._2.getAllVariables().stream()))
                .collect(groupingBy(x->x, counting()))
                .entrySet().stream()
                .filter(x -> x.getValue() > 1).map(Map.Entry::getKey).collect(toSet());
    }

    public List<Tuple2<TemplateVariable, TemplateNode>> getTemplateVarsMapping() {
        return TemplateVarsMapping;
    }

    @Override
    public String toString() {
        return String.join(" -> ", this.CodeSnippet, this.getTemplate());
    }

    public boolean isChild(TemplateVariable candidate){
        return TemplateVarsMapping.stream().anyMatch(x->x._1().equals(candidate));
    }

    public boolean isDescendant(TemplateVariable candidate){
        return isChild(candidate) || (!TemplateVarsMapping.isEmpty() && TemplateVarsMapping.stream().anyMatch(x->x._2().isDescendant(candidate)));
    }

    /**
     *
     * @param mappings TemplateVariables to be used in template
     * @param interval Interval in the original code snippet, for which a template needs to be constructed
     * @param allTokens
     * @return
     */
    private String constructTemplate(List<Tuple2<TemplateVariable, TemplateNode>> mappings, Interval interval, List<String> allTokens) {
        Map<Interval, TemplateVariable> childReplacements = mappings.stream().collect(toMap(x -> x._2().SourceInterval, Tuple2::_1, (a, b)->a));
        StringBuilder template = new StringBuilder();
        int curr = interval.a;
        while (curr <= interval.b) {
            boolean foundChild = false;
            for (var cr : childReplacements.entrySet()) {
                Interval child = cr.getKey();
                if (child.a == curr) {
                    template.append(cr.getValue().asText());
                    foundChild = true;
                    if (child.b > child.a)
                        curr = child.b;
                    break;
                }
            }
            if (!foundChild && curr < allTokens.size() && !allTokens.get(curr).equals("<EOF>"))
                template.append(allTokens.get(curr));
            curr += 1;
        }
        return template.toString();
    }

    public String getTemplate() {
        return Template;
    }

    public boolean isLeaf() {
        return isLeaf;
    }
}
