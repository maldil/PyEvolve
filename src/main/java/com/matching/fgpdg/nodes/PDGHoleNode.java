package com.matching.fgpdg.nodes;

import org.python.core.PyObject;

public class PDGHoleNode  extends PDGNode {
    PyObject parentClass;

    public PDGHoleNode(PyObject astNode, int nodeType) {
        super(astNode, nodeType);
    }

    public PDGHoleNode(PyObject astNode, int nodeType, String key) {
        super(astNode, nodeType, key);
    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public String getExasLabel() {
        return null;
    }

    @Override
    public boolean isEqualNodes(PDGNode node) {
        return false;
    }
}
