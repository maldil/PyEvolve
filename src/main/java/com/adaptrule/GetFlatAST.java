package com.adaptrule;

import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;

import java.util.ArrayList;
import java.util.List;

public class GetFlatAST extends Visitor {
    List<PythonTree> flatTree = new ArrayList<>();
    @Override
    public Object unhandled_node(PythonTree node) throws Exception {
        flatTree.add(node);
        return super.unhandled_node(node);
    }
}
