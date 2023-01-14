package com.matching.fgpdg.nodes;

import com.ibm.wala.util.collections.Pair;
import org.antlr.runtime.ANTLRStringStream;
import org.python.antlr.AnalyzingParser;
import org.python.antlr.Visitor;
import org.python.antlr.ast.*;
import org.python.antlr.ast.Module;
import org.python.antlr.base.mod;
import org.python.core.PyObject;
import org.python.modules._hashlib;
import org.python.modules._imp$new_module_exposer;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Guards {

    private HashMap<String, Pair<Pair<Integer,Integer>, String>> types=new HashMap<>();
    private HashMap<String,String> kinds = new HashMap<>();
    private HashMap<String,String> values = new HashMap<>();
    private HashMap<String,String> imports = new HashMap<>();

    public Guards(String code, Module mod) {
//        Module mod = (Module)parsePython(code);
        PyNameVisitor lineNumber= new PyNameVisitor();
        try {
            lineNumber.visit(mod);
            for (String type : Arrays.stream(code.split("\n")).filter(x -> x.contains("#") && x.contains("type")).collect(Collectors.toList())) {
                if (lineNumber.mapRowCol.get(type.split(" ")[2])!=null){
                    for (Pair<Integer, Integer> typePair : lineNumber.mapRowCol.get(type.split(" ")[2])) {
                        types.put(type.split(" ")[2],Pair.make(Pair.make(typePair.fst,typePair.snd),type.split(" ")[4]));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        values = (HashMap<String,String>)Arrays.stream(code.split("\n")).filter(x -> x.contains("#") && x.contains("value")).collect(Collectors.toMap(y -> y.split(" ")[2], y -> y.split(" ")[4]));
                kinds = (HashMap<String,String>)Arrays.stream(code.split("\n")).filter(x -> x.contains("#") && x.contains("kind")).collect(Collectors.toMap(y -> y.split(" ")[2], y -> y.split(" ")[4]));
        imports = (HashMap<String,String>)Arrays.stream(code.split("\n")).filter(x -> x.contains("#") && x.contains("import")).collect(Collectors.toMap(y -> y.split(" ")[2], y -> y.split(" ")[4]));
    }


    public static mod parsePython(String code) {
        ANTLRStringStream antlrSting = new ANTLRStringStream(code);
        AnalyzingParser p = new AnalyzingParser(antlrSting, "", "ascii");
        return p.parseModule();
    }

    static class PyNameVisitor extends Visitor {
        private HashMap<String, List<Pair<Integer, Integer>>> mapRowCol = new HashMap<>();

        @Override
        public void preVisit(PyObject node) {

        }

        @Override
        public void postVisit(PyObject node) {

        }

        @Override
        public Object visitName(Name node) throws Exception {
            if (mapRowCol.get(node.getInternalId())==null){
                ArrayList<Pair<Integer,Integer>> arrayRowCol = new ArrayList<>();
                arrayRowCol.add(Pair.make(node.getLineno(),node.getCol_offset()));
                mapRowCol.put(node.getInternalId(),arrayRowCol);
            }
            else{
                mapRowCol.get(node.getInternalId()).add(Pair.make(node.getLineno(),node.getCol_offset()));
            }
            return super.visitName(node);
        }

        @Override
        public Object visitHole(Hole node) throws Exception {
            if (mapRowCol.get(node.toString())==null){
                ArrayList<Pair<Integer,Integer>> arrayRowCol = new ArrayList<>();
                arrayRowCol.add(Pair.make(node.getLineno(),node.getCol_offset()));
                mapRowCol.put(node.toString(),arrayRowCol);
            }
            else{
                mapRowCol.get(node.toString()).add(Pair.make(node.getLineno(),node.getCol_offset()));
            }

            return super.visitHole(node);
        }
//        @Override
//        public Object visitAttribute(Attribute node) throws Exception {
//            if (node.getInternalValue() instanceof Name){
//                if (mapRowCol.get(((Name)node.getInternalValue()).getInternalId())==null){
//                    ArrayList<Pair<Integer,Integer>> arrayRowCol = new ArrayList<>();
//                    arrayRowCol.add(Pair.make(((Name)node.getInternalValue()).getLineno(),
//                            ((Name)node.getInternalValue()).getCol_offset()));
//                    mapRowCol.put(((Name)node.getInternalValue()).getInternalId(),arrayRowCol);
//                }
//                else{
//                    mapRowCol.get(((Name)node.getInternalValue()).getInternalId()).
//                            add(Pair.make(((Name)node.getInternalValue()).getLineno(),((Name)node.getInternalValue()).getCol_offset()));
//                }
//            }
//
//
//
//
//            return super.visitAttribute(node);
//        }
    }

    public String getTypeOfTemplateVariable(String templateVariable){
        if (types.get(templateVariable)!=null)
            return types.get(templateVariable).snd;
        else
            return "Any";
//        return types.getOrDefault(templateVariable, "AnyType");
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

    public HashMap<String, Pair<Pair<Integer,Integer>, String>> getTypes() {
        return types;
    }

    public HashMap<String, String> getKinds() {
        return kinds;
    }

    public HashMap<String, String> getValues() {
        return values;
    }

    public HashMap<String, String> getImports() {
        return imports;
    }
}
