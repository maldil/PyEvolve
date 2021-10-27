package com.inferrules.core;

import com.google.gson.GsonBuilder;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.antlr.v4.runtime.misc.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public class TemplateNode {
    private final String CodeSnippet;
    private final String Template;
    private final List<Tuple2<TemplateVariable, TemplateNode>> TemplateVarsMapping;
    private final Interval SourceInterval;

    public TemplateNode(Node r, VariableNameGenerator nameGenerator, List<String> allTokens) {
        var n = compress(r.getText(), r);
        CodeSnippet = n.getValue();
        SourceInterval = n.getSourceInterval();
        TemplateVarsMapping = n.getChildren().stream()
                .filter(Node::isNotKwdOrSymb)
                .map(x -> Tuple.of(nameGenerator.getNameOrElseNew(x.getText()),
                                                new TemplateNode(x, nameGenerator, allTokens)))
                .collect(Collectors.toList());

        Template = constructTemplate(TemplateVarsMapping, n.getSourceInterval(), allTokens);
    }

    TemplateNode(String template, List<Tuple2<TemplateVariable, TemplateNode>> templateVariableMapping,
                 String codeSnippet, Interval sourceInterval) {
        this.Template = template;
        this.TemplateVarsMapping = templateVariableMapping;
        this.CodeSnippet = codeSnippet;
        this.SourceInterval = sourceInterval;
    }

    private Node compress(String text, Node n ){
        return n.getChildren().stream().filter(x -> x.getText().equals(text))
                .findFirst().map(node -> compress(text, node)).orElse(n);
    }

    public TemplateNode surfaceTemplateVariables(List<TemplateVariable> templateVariables, List<String> allTokens){
        TemplateNode r = new TemplateNode(this.Template, new ArrayList<>(this.TemplateVarsMapping),
                this.CodeSnippet, this.SourceInterval);
        List<Tuple2<TemplateVariable, TemplateNode>> variableMapping = helper(templateVariables, r);
        var newTemplate = constructTemplate(variableMapping, SourceInterval,allTokens);
        return new TemplateNode(newTemplate, variableMapping, r.CodeSnippet, r.SourceInterval);
    }

    public List<Tuple2<TemplateVariable, TemplateNode>> helper(List<TemplateVariable> tmpltVars, TemplateNode root){
        return root.getTemplateVarsMapping().stream()
                .flatMap(varMapping -> {
                    if (tmpltVars.contains(varMapping._1()) || tmpltVars.stream().noneMatch(x -> varMapping._2().isDescendant(x)))
                        return Stream.of(varMapping);
                    return varMapping._2().getTemplateVarsMapping().isEmpty() ? Stream.empty() : helper(tmpltVars, varMapping._2()).stream();
                })
                .collect(toList());
    }


    public String getCodeSnippet() {
        return CodeSnippet;
    }

    public String toJson() {
        return new GsonBuilder().disableHtmlEscaping().create().toJson(this, TemplateNode.class);
    }


    public List<TemplateVariable> getAllVariables() {
        return TemplateVarsMapping.stream()
                .flatMap(x -> Stream.concat(Stream.of(x._1()), x._2.getAllVariables().stream()))
                .collect(Collectors.toList());
    }


    public List<TemplateVariable> getRepeatedTemplateVariables() {
        return TemplateVarsMapping.stream()
                .flatMap(x -> Stream.concat(Stream.of(x._1()), x._2.getAllVariables().stream()))
                .collect(groupingBy(x->x, counting()))
                .entrySet().stream()
                .filter(x -> x.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toList());
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
}
