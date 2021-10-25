package com.inferrules.core;

import com.google.gson.GsonBuilder;
import com.inferrules.core.languageAdapters.ILanguageAdapter;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.antlr.v4.runtime.misc.Interval;
import java.util.*;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.inferrules.utils.Utilities.asEntry;
import static java.util.stream.Collectors.toMap;

public class Template {

    private final String CompleteSnippet;
    private final Node Root;
    private final TemplateNode TemplateNode;

    public Template(String codeSnippet, ILanguageAdapter languageAdapter, VariableNameGenerator nameGenerator) {
        this.CompleteSnippet = codeSnippet;
        this.Root = languageAdapter.parse(codeSnippet);
        this.TemplateNode = new TemplateNode(Root, nameGenerator, languageAdapter.tokenize(codeSnippet));
    }

    public String getTemplateNodeAsJson(){
        return TemplateNode.toJson();
    }



    public static class TemplateNode {
        private final String codeSnippet;
        private final String Template;
        private final List<Tuple2<TemplateVariable, TemplateNode>> TemplateVarsMapping;
        private final Interval sourceInterval;


        public String toJson(){
            return new GsonBuilder().disableHtmlEscaping()
                    .create().toJson(this,TemplateNode.class);
        }

        private TemplateNode(String codeSnippet, String template,
                             List<Tuple2<TemplateVariable, TemplateNode>> templateVarsMapping, Interval sourceInterval){
            this.codeSnippet = codeSnippet;
            this.Template = template;
            this.TemplateVarsMapping = templateVarsMapping;
            this.sourceInterval = sourceInterval;
        }

        private Set<TemplateVariable> getAllVariables(){
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
                            .map(child -> Tuple.of(nameGenerator.getNameOrElseNew(child.getText()), new TemplateNode(child, nameGenerator,allTokens)))
                            .collect(Collectors.toList());;
            this.Template = constructTemplate(TemplateVarsMapping, n.getSourceInterval(), allTokens);
        }

        @Override
        public String toString(){
            return  String.join(" -> ", this.codeSnippet, this.getTemplate());
        }

        private String constructTemplate(List<Tuple2<TemplateVariable, TemplateNode>> mappings, Interval interval, List<String> allTokens) {
            Map<Interval, TemplateVariable> childReplacements = mappings.stream().collect(toMap(x->x._2().sourceInterval, x->x._1()));
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

    public String getCompleteSnippet() {
        return CompleteSnippet;
    }

    public Node getRoot() {
        return Root;
    }

    public TemplateNode getTemplateNode() {
        return TemplateNode;
    }
}


//    private TemplateNode renameTemplateNode(TemplateNode tn, TemplateVariable before, String after){
//        String newTemplate = tn.Template.replace(before.getName(), after);
//        var newTemplateVariableMapping = tn.TemplateVarsMapping.stream().map(x -> asEntry(x.getKey().equals(before) ? x.getKey().rename(after) : x.getKey(), x.getValue()))
//                .collect(Collectors.toList());
//        return new TemplateNode(newTemplate, newTemplateVariableMapping, tn.codeSnippet, tn.sourceInterval);
//    }

//    public static TemplateNode renameTemplateVariable(TemplateNode tn, String before, String after) {
//        tn.TemplateVarsMapping.stream().filter(x->x.getKey().hasName(before));
//
//
//        List<SimpleImmutableEntry<TemplateVariable, TemplateNode>> newTemplateVariableMapping = TemplateVarsMapping.stream()
//                .map(x -> asEntry(x.getKey().hasName(before) ? x.getKey().rename(after) : x.getKey(), x.getValue()))
//                .collect(Collectors.toList());
//        String newTemplate = Template.replace(before, after);
//        return new TemplateNode(newTemplate, newTemplateVariableMapping, codeSnippet, sourceInterval);
//    }


//    public TemplateNode decomposeTemplateVariable(String templateVariableName) {
//        Map<Interval, TemplateVariable> newChildReplacements = new HashMap<>();
//        List<SimpleImmutableEntry<TemplateVariable, TemplateNode>> newTemplateVariableMapping = new ArrayList<>();
//        for (var entry : TemplateVarsMapping) {
//            if (entry.getKey().getName().equals(templateVariableName))
//                for (var decomposedChildTmplVarEntry : entry.getValue().TemplateVarsMapping) {
//                    newChildReplacements.put(decomposedChildTmplVarEntry.getValue().sourceInterval, decomposedChildTmplVarEntry.getKey());
//                    newTemplateVariableMapping.add(asEntry(decomposedChildTmplVarEntry.getKey(), decomposedChildTmplVarEntry.getValue()));
//                }
//            else {
//                newChildReplacements.put(entry.getValue().sourceInterval, entry.getKey());
//                newTemplateVariableMapping.add(asEntry(entry.getKey(), entry.getValue()));
//            }
//        }
//        String newTemplate = constructTemplate(newChildReplacements, sourceInterval);
//        return new TemplateNode(newTemplate, newTemplateVariableMapping, codeSnippet, sourceInterval);
//    }


//        public TemplateNode(String template, List<SimpleImmutableEntry<TemplateVariable, TemplateNode>> templateVariableMapping,
//                            String codeSnippet, Interval sourceInterval, VariableNameGenerator nameGenerator) {
//            this.Template = template;
//            this.TemplateVarsMapping = templateVariableMapping;
//            this.codeSnippet = codeSnippet;
//            this.sourceInterval = sourceInterval;
//            this.nameGenerator = nameGenerator;
//        }