package com.inferrules.core;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a current hack that I use to generate new names for the holes.
 */
public class VariableNameGenerator {

    private char VarNameSeed;
    private int CurrentIndex = 0;

    private final Map<String, TemplateVariable> CodeToTemplateVars; // codeSnippet -> variableName

    public void resetButKeepCache(char seed) {
        VarNameSeed = seed;
        CurrentIndex = 0;
    }

    public VariableNameGenerator(char seed) {
        VarNameSeed = seed;
        CodeToTemplateVars = new HashMap<>();
    }

    /**
     * @param node The node for which a new template variable is required
     * @return If the generator has previously created a template variable for the node, this template variable is returned;
     * Else a new template variable is produced.
     */
    public TemplateVariable getNameOrElseNew(Node node){
        return CodeToTemplateVars.computeIfAbsent(node.getText(),
                c -> new TemplateVariable("" + VarNameSeed + (CurrentIndex++), c));
    }
}
