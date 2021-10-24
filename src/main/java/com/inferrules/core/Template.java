package com.inferrules.core;

import com.inferrules.core.languageAdapters.Code2Node;
import org.antlr.v4.runtime.misc.Interval;
import java.util.*;
import java.util.Map.Entry;
import static java.util.stream.Collectors.toMap;

public class Template {

    private final String CompleteSnippet;
    private final Node root;
    private final List<String> tokens;
    private final TemplateNode templateNode;
    private final Map<String, TemplateVariable> codeToTemplateVars;
    private final char varNameSeed;
    private int currentIndex = 0;

    public Template(String codeSnippet, Code2Node cn, boolean isLeft) {
        this.codeToTemplateVars = new HashMap<>();
        this.CompleteSnippet = codeSnippet;
        this.tokens = cn.tokenize(codeSnippet);
        this.varNameSeed = isLeft ? 'l' : 'r';
        this.root = cn.parse(codeSnippet);
        this.templateNode = new TemplateNode(root);
    }

    public Map<String, TemplateVariable> getCodeToTemplateVars() {
        return codeToTemplateVars;
    }
    public class TemplateNode {

        private final String codeSnippet;
        private final Interval sourceInterval;
        private final String Template;
        private final Map<TemplateVariable, TemplateNode> TemplateVarsMapping;

        private TemplateNode(String template, Map<TemplateVariable, TemplateNode> templateVariableMapping,
                             String codeSnippet, Interval sourceInterval) {
            this.Template = template;
            this.TemplateVarsMapping = templateVariableMapping;
            this.codeSnippet = codeSnippet;
            this.sourceInterval = sourceInterval;
        }

        public TemplateNode(Node n) {
            this.codeSnippet = n.getValue();
            this.sourceInterval = n.getSourceInterval();
            Map<Interval, TemplateVariable> childReplacements = new HashMap<>();
            this.TemplateVarsMapping = new HashMap<>();
            n.getChildren().stream().filter(Node::isNotKwdOrSymb)
                    .forEach(child -> {
                        var tVar =  codeToTemplateVars.computeIfAbsent(child.getText(),
                                c -> new TemplateVariable("" + varNameSeed + (currentIndex++), c));
                        childReplacements.put(child.getSourceInterval(), tVar);
                        TemplateVarsMapping.put(tVar, new TemplateNode(child));
                    });
            this.Template = constructTemplate(childReplacements, n.getSourceInterval());
        }

        @Override
        public String toString(){
            return  String.join(" -> ", this.codeSnippet, this.getTemplate());
        }

        public TemplateNode renameTemplateVariable(String before, String after) {
            Map<TemplateVariable, TemplateNode> newTemplateVariableMapping = TemplateVarsMapping.entrySet().stream()
                    .collect(toMap(x -> x.getKey().hasName(before) ? x.getKey().rename(after) : x.getKey(), Entry::getValue));
            String newTemplate = Template.replace(before, after);
            return new TemplateNode(newTemplate, newTemplateVariableMapping, codeSnippet, sourceInterval);
        }

        public TemplateNode decomposeTemplateVariable(String templateVariableName) {
            Map<Interval, TemplateVariable> newChildReplacements = new HashMap<>();
            Map<TemplateVariable, TemplateNode> newTemplateVariableMapping = new HashMap<>();
            for (var entry : TemplateVarsMapping.entrySet()) {
                if (entry.getKey().getName().equals(templateVariableName))
                    for (var decomposedChildTmplVarEntry : entry.getValue().TemplateVarsMapping.entrySet()) {
                        newChildReplacements.put(decomposedChildTmplVarEntry.getValue().sourceInterval, decomposedChildTmplVarEntry.getKey());
                        newTemplateVariableMapping.put(decomposedChildTmplVarEntry.getKey(), decomposedChildTmplVarEntry.getValue());
                    }
                else {
                    newChildReplacements.put(entry.getValue().sourceInterval, entry.getKey());
                    newTemplateVariableMapping.put(entry.getKey(), entry.getValue());
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
                if (!foundChild && curr < tokens.size())
                    template.append(tokens.get(curr));
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
        return root;
    }

    public TemplateNode getTemplateNode() {
        return templateNode;
    }
}