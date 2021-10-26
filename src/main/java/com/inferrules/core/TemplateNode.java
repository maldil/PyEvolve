package com.inferrules.core;

import com.google.gson.GsonBuilder;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.antlr.v4.runtime.misc.Interval;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public class TemplateNode {
    private final String codeSnippet;
    private final String Template;
    private final List<Tuple2<TemplateVariable, TemplateNode>> TemplateVarsMapping;
    private final Interval sourceInterval;

    public String getCodeSnippet() {
        return codeSnippet;
    }

    public Interval getSourceInterval() {
        return sourceInterval;
    }

    public String toJson() {
        return new GsonBuilder().disableHtmlEscaping().create().toJson(this, TemplateNode.class);
    }

    public List<TemplateVariable> getAllVariables() {
        return TemplateVarsMapping.stream()
                .flatMap(x -> Stream.concat(Stream.of(x._1()), x._2.getAllVariables().stream()))
                .collect(Collectors.toList());
    }

    public Map<TemplateVariable, Long> getAllVariablesCount() {
        return TemplateVarsMapping.stream()
                .flatMap(x -> Stream.concat(Stream.of(x._1()), x._2.getAllVariables().stream()))
                .collect(groupingBy(x->x, counting()));
    }

    public List<Tuple2<TemplateVariable, TemplateNode>> getTemplateVarsMapping() {
        return TemplateVarsMapping;
    }

    public TemplateNode(Node n, VariableNameGenerator nameGenerator, List<String> allTokens) {
        this.codeSnippet = n.getValue();
        this.sourceInterval = n.getSourceInterval();
        this.TemplateVarsMapping = n.getChildren().stream().filter(Node::isNotKwdOrSymb)
                // If the parent and child node are same by text (i.e. no white space n stuff), then skip n and decompose child
                .flatMap(child -> child.getText().equals(n.getText()) ?
                        child.getChildren().stream().map(x -> Tuple.of(nameGenerator.getNameOrElseNew(x.getText()),
                                new TemplateNode(x, nameGenerator, allTokens)))
                        : Stream.of(Tuple.of(nameGenerator.getNameOrElseNew(child.getText()), new TemplateNode(child, nameGenerator, allTokens))))
                .collect(Collectors.toList());
        this.Template = constructTemplate(TemplateVarsMapping, n.getSourceInterval(), allTokens);
    }

    @Override
    public String toString() {
        return String.join(" -> ", this.codeSnippet, this.getTemplate());
    }

    public boolean isChild(TemplateVariable candidate){
        return TemplateVarsMapping.stream().anyMatch(x->x._1().equals(candidate));
    }

    public boolean isDescendant(TemplateVariable candidate){
        return isChild(candidate) || (!TemplateVarsMapping.isEmpty() && TemplateVarsMapping.stream().anyMatch(x->x._2().isDescendant(candidate)));
    }

    private String constructTemplate(List<Tuple2<TemplateVariable, TemplateNode>> mappings, Interval interval, List<String> allTokens) {
        Map<Interval, TemplateVariable> childReplacements = mappings.stream().collect(toMap(x -> x._2().sourceInterval, Tuple2::_1));
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
            if (!foundChild && curr < allTokens.size())
                template.append(allTokens.get(curr));
            curr += 1;
        }
        return template.toString().replace("<EOF>", "");
    }

    public String getTemplate() {
        return Template;
    }


    private TemplateNode renameTemplateNode(TemplateNode tn, TemplateVariable before, String after) {
//        String newTemplate = tn.getTemplate().replace(before.getName(), after);
//        var newTemplateVariableMapping = tn.getTemplateVarsMapping().stream()
//                .map(x -> Tuple.of(x._1().equals(before) ? x._1().rename(after) : x._1(), renameTemplateNode(x._2(), before, after)))
//                .collect(Collectors.toList());
//        return new TemplateNode(tn.codeSnippet, newTemplate, newTemplateVariableMapping, sourceInterval);
//    }
        return null;
    }
//        public TemplateNode(String codeSnippet, String template,
//                         List<Tuple2<TemplateVariable, TemplateNode>> templateVarsMapping, Interval sourceInterval) {
//        this.codeSnippet = codeSnippet;
//        this.Template = template;
//        this.TemplateVarsMapping = templateVarsMapping;
//        this.sourceInterval = sourceInterval;
//    }



//        public TemplateNode concretizeNode(Set<String> except) {
//            Map<Interval, TemplateVariable> newChildReplacements = new HashMap<>();
//            List<SimpleImmutableEntry<TemplateVariable, TemplateNode>> newTemplateVariableMapping = new ArrayList<>();
//
//            for (var entry : TemplateVarsMapping) {
//                if(!except.contains(entry._1())){
//
//
//                    for (var decomposedChildTmplVarEntry : entry._2().TemplateVarsMapping) {
//
//
////                        newChildReplacements.put(decomposedChildTmplVarEntry._2().sourceInterval, decomposedChildTmplVarEntry._1());
////                        newTemplateVariableMapping.add(asEntry(decomposedChildTmplVarEntry._1(), decomposedChildTmplVarEntry._2()));
//                    }
//                }
//
//
//                if (entry._1().getName().equals(templateVariableName))
//                    for (var decomposedChildTmplVarEntry : entry._2().TemplateVarsMapping) {
//                        newChildReplacements.put(decomposedChildTmplVarEntry._2().sourceInterval, decomposedChildTmplVarEntry._1());
//                        newTemplateVariableMapping.add(asEntry(decomposedChildTmplVarEntry._1(), decomposedChildTmplVarEntry._2()));
//                    }
//                else {
//                    newChildReplacements.put(entry._2().sourceInterval, entry._1());
//                    newTemplateVariableMapping.add(asEntry(entry._1(), entry._2()));
//                }
//            }
//            String newTemplate = constructTemplate(newChildReplacements, sourceInterval);
//            return new TemplateNode(newTemplate, newTemplateVariableMapping, codeSnippet, sourceInterval);
//        }

}
