package com.adaptrule;

import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.Name;
import org.python.antlr.base.expr;
import org.python.core.PyObject;

import java.util.*;
import java.util.stream.Collectors;

public class CollectChangedNames extends Visitor {
    private Set<PythonTree> changedNames;

    public CollectChangedNames(Set<PythonTree> changedNames) {
        this.changedNames =  changedNames;
    }

    Map<PythonTree,List<PythonTree>> matchedOtherNodes=new HashMap<>();
    @Override
    public Object visitName(Name node)  throws Exception {
        for (PythonTree name : changedNames) {
            if (name.toString().equals(node.toString())){
                if (matchedOtherNodes.get(name)==null)
                    matchedOtherNodes.put(name,new ArrayList<PythonTree>(Arrays.asList(node)));
                else
                    matchedOtherNodes.get(name).add(node);
            }
        }
        return super.visitName(node);
    }

    public Map<PythonTree, List<PythonTree>> getMatchedOtherNodes() {
        return matchedOtherNodes;
    }

    @Override
    public void preVisit(PyObject node) {

    }

    @Override
    public void postVisit(PyObject node) {

    }
}
