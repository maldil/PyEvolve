package com.matching.fgpdg.nodes;

import org.python.core.PyObject;

public class PDGEntryNode extends PDGNode {
    private String label;

    public PDGEntryNode(PyObject astNode, int nodeType, String label) {
        super(astNode, nodeType);
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getExasLabel() {
        return label;
    }

    @Override
    public boolean isDefinition() {
        return false;
    }

    @Override
    public String toString() {
        return getLabel();
    }

    @Override
    public  boolean isEqualNodes(PDGNode node){
        return getLabel().equals(node.getLabel());
    }
}
