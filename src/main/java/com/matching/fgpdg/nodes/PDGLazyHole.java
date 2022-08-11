package com.matching.fgpdg.nodes;

import org.python.core.PyObject;

public class PDGLazyHole extends PDGHoleNode {
    public PDGLazyHole(PyObject astNode, int nodeType, String value, String key, String dataType, String dataName,boolean isDataNode,boolean isActionNode,boolean isContralNode) {
        super(astNode, nodeType, value, key, dataType, dataName, isDataNode, isActionNode, isContralNode);
    }

    @Override
    public boolean isEqualNodes(PDGNode node) {
        return true;
    }
}
