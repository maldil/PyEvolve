package com.inferrules.core;

import com.google.gson.Gson;
import com.inferrules.core.languageAdapters.ILanguageAdapter;
import org.antlr.v4.runtime.misc.Interval;
import java.util.*;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static com.inferrules.utils.Utilities.asEntry;
import static java.util.stream.Collectors.toMap;

public class Template {

    private final String CompleteSnippet;
    private final Node Root;
    private final List<String> Tokens;
    private final TemplateNode TemplateNode;
    private final Map<String, TemplateVariable> CodeToTemplateVars;
    private final char VarNameSeed;
    private int currentIndex = 0;

    public Template(String codeSnippet, ILanguageAdapter languageAdapter, boolean isLeft) {
        this.CodeToTemplateVars = new HashMap<>();
        this.CompleteSnippet = codeSnippet;
        this.Tokens = languageAdapter.tokenize(codeSnippet);
        this.VarNameSeed = isLeft ? 'l' : 'r';
        this.Root = languageAdapter.parse(codeSnippet);
        this.TemplateNode = new TemplateNode(Root);
    }

    public Map<String, TemplateVariable> getCodeToTemplateVars() {
        return CodeToTemplateVars;
    }

    public String getTemplateNodeAsJson(){
        return TemplateNode.toJson();
    }

    public class TemplateNode {

        private final String codeSnippet;
        private final Interval sourceInterval;
        private final String Template;
        private final List<SimpleImmutableEntry<TemplateVariable, TemplateNode>> TemplateVarsMapping;

        private TemplateNode(String template, List<SimpleImmutableEntry<TemplateVariable, TemplateNode>> templateVariableMapping,
                             String codeSnippet, Interval sourceInterval) {
            this.Template = template;
            this.TemplateVarsMapping = templateVariableMapping;
            this.codeSnippet = codeSnippet;
            this.sourceInterval = sourceInterval;
        }

        public String toJson(){
            return new Gson().toJson(this,TemplateNode.class);
        }

        public TemplateNode(Node n) {
            this.codeSnippet = n.getValue();
            this.sourceInterval = n.getSourceInterval();
            Map<Interval, TemplateVariable> childReplacements = new HashMap<>();
            this.TemplateVarsMapping = new ArrayList<>();
            for (Node child : n.getChildren()) {
                if (child.isNotKwdOrSymb()) {
                    var tVar = CodeToTemplateVars.computeIfAbsent(child.getText(),
                            c -> new TemplateVariable("" + VarNameSeed + (currentIndex++), c));
                    childReplacements.put(child.getSourceInterval(), tVar);
                    TemplateVarsMapping.add(asEntry(tVar, new TemplateNode(child)));
                }
            }
            this.Template = constructTemplate(childReplacements, n.getSourceInterval());
        }

        @Override
        public String toString(){
            return  String.join(" -> ", this.codeSnippet, this.getTemplate());
        }

        public TemplateNode renameTemplateVariable(String before, String after) {
            List<SimpleImmutableEntry<TemplateVariable, TemplateNode>> newTemplateVariableMapping = TemplateVarsMapping.stream()
                    .map(x -> asEntry(x.getKey().hasName(before) ? x.getKey().rename(after) : x.getKey(), x.getValue()))
                    .collect(Collectors.toList());
            String newTemplate = Template.replace(before, after);
            return new TemplateNode(newTemplate, newTemplateVariableMapping, codeSnippet, sourceInterval);
        }

        public TemplateNode decomposeTemplateVariable(String templateVariableName) {
            Map<Interval, TemplateVariable> newChildReplacements = new HashMap<>();
            List<SimpleImmutableEntry<TemplateVariable, TemplateNode>> newTemplateVariableMapping = new ArrayList<>();
            for (var entry : TemplateVarsMapping) {
                if (entry.getKey().getName().equals(templateVariableName))
                    for (var decomposedChildTmplVarEntry : entry.getValue().TemplateVarsMapping) {
                        newChildReplacements.put(decomposedChildTmplVarEntry.getValue().sourceInterval, decomposedChildTmplVarEntry.getKey());
                        newTemplateVariableMapping.add(asEntry(decomposedChildTmplVarEntry.getKey(), decomposedChildTmplVarEntry.getValue()));
                    }
                else {
                    newChildReplacements.put(entry.getValue().sourceInterval, entry.getKey());
                    newTemplateVariableMapping.add(asEntry(entry.getKey(), entry.getValue()));
                }
            }
            String newTemplate = constructTemplate(newChildReplacements, sourceInterval);
            return new TemplateNode(newTemplate, newTemplateVariableMapping, codeSnippet, sourceInterval);
        }


        private String constructTemplate(Map<Interval, TemplateVariable> childReplacements, Interval interval) {
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
                            curr += child.b;
                        break;
                    }
                }
                if (!foundChild && curr < Tokens.size())
                    template.append(Tokens.get(curr));
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