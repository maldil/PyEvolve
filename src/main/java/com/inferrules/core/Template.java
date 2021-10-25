package com.inferrules.core;

import com.google.gson.GsonBuilder;
import com.inferrules.core.languageAdapters.ILanguageAdapter;
import org.antlr.v4.runtime.misc.Interval;
import java.util.*;
import java.util.AbstractMap.SimpleImmutableEntry;

import static com.inferrules.utils.Utilities.asEntry;

public class Template {

    private final String CompleteSnippet;
    private final Node Root;
    private final TemplateNode TemplateNode;

    public Template(String codeSnippet, ILanguageAdapter languageAdapter, boolean isLeft) {
        this.CompleteSnippet = codeSnippet;
        this.Root = languageAdapter.parse(codeSnippet);
        this.TemplateNode = new TemplateNode(Root,new VariableNameGenerator(isLeft ? 'l' : 'r'), languageAdapter.tokenize(codeSnippet));
    }

    public String getTemplateNodeAsJson(){
        return TemplateNode.toJson();
    }

    public static class TemplateNode {

        private final String codeSnippet;
        private final String Template;
        private final List<SimpleImmutableEntry<TemplateVariable, TemplateNode>> TemplateVarsMapping;


        public String toJson(){
            return new GsonBuilder().disableHtmlEscaping()
                    .create().toJson(this,TemplateNode.class);
        }

        public TemplateNode(Node n, VariableNameGenerator nameGenerator, List<String> allTokens) {
            this.codeSnippet = n.getValue();
            Map<Interval, TemplateVariable> childReplacements = new HashMap<>();
            this.TemplateVarsMapping = new ArrayList<>();
            for (Node child : n.getChildren()) {
                if (child.isNotKwdOrSymb()) {
                    var tVar = nameGenerator.getNameOrElseNew(child.getText());
                    childReplacements.put(child.getSourceInterval(), tVar);
                    TemplateVarsMapping.add(asEntry(tVar, new TemplateNode(child, nameGenerator,allTokens)));
                }
            }
            this.Template = constructTemplate(childReplacements, n.getSourceInterval(), allTokens);
        }

        @Override
        public String toString(){
            return  String.join(" -> ", this.codeSnippet, this.getTemplate());
        }

        private String constructTemplate(Map<Interval, TemplateVariable> childReplacements, Interval interval, List<String> allTokens) {
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