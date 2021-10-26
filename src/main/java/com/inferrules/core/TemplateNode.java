package com.inferrules.core;

import com.google.gson.GsonBuilder;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.antlr.v4.runtime.misc.Interval;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

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
        return new GsonBuilder().disableHtmlEscaping()
                .create().toJson(this, TemplateNode.class);
    }

    public TemplateNode(String codeSnippet, String template,
                         List<Tuple2<TemplateVariable, TemplateNode>> templateVarsMapping, Interval sourceInterval) {
        this.codeSnippet = codeSnippet;
        this.Template = template;
        this.TemplateVarsMapping = templateVarsMapping;
        this.sourceInterval = sourceInterval;
    }

    public Set<TemplateVariable> getAllVariables() {
        return TemplateVarsMapping.stream()
                .flatMap(x -> Stream.concat(Stream.of(x._1()), x._2.getAllVariables().stream()))
                .collect(Collectors.toSet());
    }

    private TemplateNode renameTemplateNode(TemplateNode tn, TemplateVariable before, String after) {
        String newTemplate = tn.getTemplate().replace(before.getName(), after);
        var newTemplateVariableMapping = tn.getTemplateVarsMapping().stream()
                .map(x -> Tuple.of(x._1().equals(before) ? x._1().rename(after) : x._1(), renameTemplateNode(x._2(), before, after)))
                .collect(Collectors.toList());
        return new TemplateNode(tn.codeSnippet, newTemplate, newTemplateVariableMapping, sourceInterval);
    }

    public List<Tuple2<TemplateVariable, TemplateNode>> getTemplateVarsMapping() {
        return TemplateVarsMapping;
    }

    public TemplateNode(Node n, VariableNameGenerator nameGenerator, List<String> allTokens) {
        this.codeSnippet = n.getValue();
        this.sourceInterval = n.getSourceInterval();
        this.TemplateVarsMapping = n.getChildren().stream().filter(Node::isNotKwdOrSymb)
                .map(child -> Tuple.of(nameGenerator.getNameOrElseNew(child.getText()), new TemplateNode(child, nameGenerator, allTokens)))
                .collect(Collectors.toList());
        ;
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

    /*
    abc.def(xyz.gw())
        mappings:
            :[1] -> abc , starts : 0, ends : 2
            :[2] -> def , starts : 5 , ends : 7
            :[3] -> xyz.gw() , starts : 8, ends: 10

        Interval: 0, -1
        allTokens : abc, . , def, ( , xyz,, gw, (, ), )

        :[1].:[2](:[3]]) -3->  :[1].:[2](:[4].{5]())

     */

    private String constructTemplate(List<Tuple2<TemplateVariable, TemplateNode>> mappings, Interval interval, List<String> allTokens) {
        // Interval = (From, to)
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
