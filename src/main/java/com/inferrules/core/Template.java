package com.inferrules.core;

import com.inferrules.core.languageAdapters.ILanguageAdapter;

import java.util.Set;

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

    public String getCompleteSnippet() {
        return CompleteSnippet;
    }

    public Node getRoot() {
        return Root;
    }

    public Set<TemplateVariable> getAllVariables() { return TemplateNode.getAllVariables(); }

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