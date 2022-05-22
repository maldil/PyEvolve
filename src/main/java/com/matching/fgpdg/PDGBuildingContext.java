package com.matching.fgpdg;

import com.matching.fgpdg.nodes.PDGActionNode;
import com.matching.fgpdg.nodes.TypeInfo.TypeWrapper;
import com.matching.fgpdg.nodes.ast.AlphanumericHole;
import com.utils.Assertions;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.*;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;

import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class PDGBuildingContext {
    private String filePath;
    private TypeWrapper typeWrapper;
    private FunctionDef method;
    private HashMap<String, String> fieldTypes = new HashMap<>();
    private Stack<HashMap<String, String>> localVariables = new Stack<>(), localVariableTypes = new Stack<>();
    private Stack<HashSet<PDGActionNode>> stkTrys = new Stack<>();
    private HashMap<String, String> importsMap = new HashMap<>();

    public HashMap<String, String> getImportsMap() {
        return importsMap;
    }

    public String getFilePath() {
        return filePath;
    }

    public PDGBuildingContext(List<stmt> importStmt, String sourceFilePath) throws IOException {
        typeWrapper = new TypeWrapper(Configurations.TYPE_REPOSITORY+
                sourceFilePath.split("/")[0]
                +"/"+
                sourceFilePath.split("/")[1]+"/"+
                Arrays.stream(sourceFilePath.split("/")).skip(2).collect(Collectors.joining("_")).split("\\.")[0]+".json" );
        this.filePath=sourceFilePath;
        updateImportMap(importStmt);
    }

    public PDGBuildingContext(List<stmt> importStmt){
        updateImportMap(importStmt);
    }

    public PDGBuildingContext(List<stmt> importStmt, TypeWrapper typeWrapper){
        this.typeWrapper=typeWrapper;
        updateImportMap(importStmt);
    }



    public TypeWrapper getTypeWrapper() {
        return typeWrapper;
    }

    private void updateImportMap(List<stmt> importStmt) {
        for (Object anImport : importStmt) {
            if (anImport instanceof Import){
                Import im = (Import) anImport;
                List<alias> internalNames = im.getInternalNames();
                for (alias internalName : internalNames) {
                    if (internalName.getInternalAsname()!=null){
                        importsMap.put(internalName.getInternalAsname(),internalName.getInternalName());
                    }
                    else{
                        String[] split = internalName.getInternalName().split("\\.");
                        importsMap.put(split[split.length-1],internalName.getInternalName());
                    }
                }
            }
            else if (anImport instanceof ImportFrom){
                ImportFrom im = (ImportFrom) anImport;
                String from = im.getInternalModule();
                List<alias> internalNames = im.getInternalNames();
                for (alias internalName : internalNames) {
                    if (internalName.getInternalAsname()!=null){
                        importsMap.put(internalName.getInternalAsname(),from+"."+internalName.getInternalName());
                    }
                    else{
                        String[] split = internalName.getInternalName().split("\\.");
                        importsMap.put(split[split.length-1],from+"."+internalName.getInternalName());
                    }
                }
            }
        }
        if (this.typeWrapper.getGuards()!=null){
            for (Map.Entry<String, String> entry : this.typeWrapper.getGuards().getImports().entrySet()) {
                importsMap.put(entry.getKey(),entry.getValue());
            }
        }


    }

    public String getFieldType(String name) {
        String type = this.fieldTypes.get(name);
        if (type == null) {
//			buildSuperFieldTypes();
            type = this.fieldTypes.get(name);
        }
        return type;
    }

    public void addScope() {
        this.localVariables.push(new HashMap<String, String>());
        this.localVariableTypes.push(new HashMap<String, String>());
    }

    public void removeScope() {
        this.localVariables.pop();
        this.localVariableTypes.pop();
    }

    public void setMethod(FunctionDef func){
        method=func;
    }

    public String[] getLocalVariableInfo(String identifier) {
        for (int i = localVariables.size() - 1; i >= 0; i--) {
            HashMap<String, String> variables = this.localVariables.get(i);
            if (variables.containsKey(identifier))
                return new String[]{variables.get(identifier), this.localVariableTypes.get(i).get(identifier)};
        }
        return null;
    }

    public void addLocalVariable(String identifier, String key, String type) {
        this.localVariables.peek().put(identifier, key);
        this.localVariableTypes.peek().put(identifier, type);
    }

    public void updateTypeOfVariable(String identifier,   String type){
        for (int i = localVariables.size() - 1; i >= 0; i--) {
            HashMap<String, String> variables = this.localVariables.get(i);
            if (variables.containsKey(identifier))
                 this.localVariableTypes.get(i).put(identifier,type);
        }
    }

    public void pushTry() {
        stkTrys.push(new HashSet<PDGActionNode>());
    }

    public HashSet<PDGActionNode> popTry() {
        return stkTrys.pop();
    }

    public String getKey(Subscript astNode) {
        String name=null;
        expr value = astNode.getInternalValue();
        if (value instanceof Subscript){
            name = getKey((Subscript) value);
        }
        else if (value instanceof Attribute){
            name = getFQName((Attribute)value);
        }
        else if (value instanceof Name){
            name = ((Name) value).getInternalId();
            String[] info = getLocalVariableInfo(name);
            if (info != null)
                name = info[0];
            else
                name=getTypeWrapper().getTypeInfo(((Name) value).getLineno() ,((Name) value).getCol_offset(),((Name) value).getInternalId());
        }
        if (astNode.getInternalSlice() instanceof Index && ((Index)astNode.getInternalSlice()).getInternalValue() instanceof Tuple){
            Tuple internalValue = (Tuple)((Index) astNode.getInternalSlice()).getInternalValue();
            if (internalValue.getInternalElts().stream().allMatch(x->x instanceof Num)){
                name = "["+internalValue.getInternalElts().stream().map(x-> ((Num)x).getInternalN().toString()).collect(Collectors.joining(","))+"]";
            }
        }
        else if (astNode.getInternalSlice() instanceof Index && ((Index)astNode.getInternalSlice()).getInternalValue() instanceof Num){
            name= "[Num]";
        }

        return  name;


    }

    private String getFQName(Attribute node) {
        if (node.getInternalValue() instanceof Name){
            if (!node.getInternalAttr().equals("Hole")) {
                    return ((Name) node.getInternalValue()).getInternalId() + "." + node.getInternalAttr();
            } else {
                    return ((Name) node.getInternalValue()).getInternalId() + "." + node.getInternalHole().toString();
            }
        }
        else if(node.getInternalValue() instanceof Attribute){
            if (!node.getInternalAttr().equals("Hole")) {
                return getFQName((Attribute) node.getInternalValue()) + "." + node.getInternalAttr();
            }
            else {
                return getFQName((Attribute) node.getInternalValue()) + "." + node.getInternalHole().toString();
            }
        }
        else if (node.getInternalValue() instanceof Hole){
            if (!node.getInternalAttr().equals("Hole")) {
                return node.getInternalValue().toString() + "." + node.getInternalAttr();
            }
            else {
                return node.getInternalValue().toString() + "." + node.getInternalHole().toString();
            }
        }
        else{
            Assertions.UNREACHABLE();
            return "";
        }
    }
}
