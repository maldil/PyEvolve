package com.inferrules.core;

import java.util.HashMap;
import java.util.Map;

public class VariableNameGenerator {

    private final char VarNameSeed;
    private int currentIndex = 0;
    private final Map<String, TemplateVariable> CodeToTemplateVars;

    public VariableNameGenerator(char seed) {
        VarNameSeed = seed;
        CodeToTemplateVars = new HashMap<>();
    }


    public TemplateVariable getNameOrElseNew(String childText){
        return  CodeToTemplateVars.computeIfAbsent(childText,
                c -> new TemplateVariable("" + VarNameSeed + (currentIndex++), c));
    }
}
