package com.matching.fgpdg;

import com.matching.fgpdg.nodes.PDGActionNode;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.alias;
import org.python.antlr.base.stmt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

public class PDGBuildingContext {
    private String filePath;
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

    public PDGBuildingContext(List<stmt> importStmt, String sourceFilePath) {
        this.filePath=sourceFilePath;
        updateImportMap(importStmt);
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

    public void pushTry() {
        stkTrys.push(new HashSet<PDGActionNode>());
    }

}
