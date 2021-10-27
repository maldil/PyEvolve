package com.inferrules.core;

import java.util.HashMap;
import java.util.Map;

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

    public TemplateVariable getNameOrElseNew(String childText){
        return CodeToTemplateVars.computeIfAbsent(childText,
                c -> new TemplateVariable("" + VarNameSeed + (CurrentIndex++), c));
    }

    public Map<String, TemplateVariable> getCodeToTemplateVars() {
        return CodeToTemplateVars;
    }
}
