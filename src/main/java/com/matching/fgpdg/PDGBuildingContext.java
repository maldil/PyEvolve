package com.matching.fgpdg;

import com.matching.fgpdg.nodes.PDGActionNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Import;
import org.python.antlr.base.stmt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

public class PDGBuildingContext {
    private String filePath;
    private List<stmt> importStmt;
    private FunctionDef method;
    private Stack<HashMap<String, String>> localVariables = new Stack<>(), localVariableTypes = new Stack<>();
    private Stack<HashSet<PDGActionNode>> stkTrys = new Stack<>();

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
