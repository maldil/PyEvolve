package com.matching.fgpdg.nodes;

import org.python.modules._hashlib;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Guards {

    private HashMap<String,String> types = new HashMap<>();
    private HashMap<String,String> kinds = new HashMap<>();
    private HashMap<String,String> values = new HashMap<>();
    private HashMap<String,String> imports = new HashMap<>();
    public Guards(String code) {
        Map<String, String> values = Arrays.stream(code.split("\n")).filter(x -> x.contains("#") && x.contains("value")).collect(Collectors.toMap(y -> y.split(" ")[2], y -> y.split(" ")[3]));
        Map<String, String> types = Arrays.stream(code.split("\n")).filter(x -> x.contains("#") && x.contains("type")).collect(Collectors.toMap(y -> y.split(" ")[2], y -> y.split(" ")[3]));
        Map<String, String> kinds = Arrays.stream(code.split("\n")).filter(x -> x.contains("#") && x.contains("kind")).collect(Collectors.toMap(y -> y.split(" ")[2], y -> y.split(" ")[3]));
        Map<String, String> imports = Arrays.stream(code.split("\n")).filter(x -> x.contains("#") && x.contains("import")).collect(Collectors.toMap(y -> y.split(" ")[2], y -> y.split(" ")[3]));


    }

    public String getTypeOfTemplateVariable(String templateVariable){
        return types.getOrDefault(templateVariable, "AnyType");
    }

    public String getValueOfTemplateVariable(String templateVariable){
        return values.getOrDefault(templateVariable, "AnyValue");
    }

    public String getKindOfTemplateVariable(String templateVariable){
        return kinds.getOrDefault(templateVariable, "AnyKind");
    }

    public String getImportOfTemplateVariable(String templateVariable){
        return imports.getOrDefault(templateVariable, "AnyImport");
    }

    public HashMap<String, String> getTypes() {
        return types;
    }

    public HashMap<String, String> getKinds() {
        return kinds;
    }

    public HashMap<String, String> getValues() {
        return values;
    }
}
