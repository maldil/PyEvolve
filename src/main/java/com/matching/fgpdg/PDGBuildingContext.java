package com.matching.fgpdg;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Import;
import org.python.antlr.base.stmt;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class PDGBuildingContext {
    private String filePath;
    private List<stmt> importStmt;
    private FunctionDef method;
    private Stack<HashMap<String, String>> localVariables = new Stack<>(), localVariableTypes = new Stack<>();

    public PDGBuildingContext(List<stmt> importStmt,String sourceFilePath) {
        this.filePath=sourceFilePath;
        this.importStmt=importStmt;
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
}
