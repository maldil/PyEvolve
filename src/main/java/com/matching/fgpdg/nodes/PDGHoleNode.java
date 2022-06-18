package com.matching.fgpdg.nodes;

import org.python.core.PyObject;

public class PDGHoleNode  extends PDGNode {
    PyObject parentClass;
    protected String dataName;
    protected String value;
    private boolean actionNode=false;
    private boolean dataNode=false;
    private boolean controlNode=false;

    public PDGHoleNode(PyObject astNode, int nodeType) {
        super(astNode, nodeType);
    }

    public PDGHoleNode(PyObject astNode, int nodeType,String value, String key, String dataType1, String dataName,boolean isDataNode,boolean isActionNode,boolean isContralNode) {
        super(astNode, nodeType, key);
        dataType = dataType1;
        this.dataName = dataName;
        this.value = value;
        this.dataNode=isDataNode;
        this.actionNode=isActionNode;
        this.controlNode=isContralNode;
    }
    public PDGHoleNode(PyObject astNode, int nodeType, String key) {
        super(astNode, nodeType, key);
    }

    @Override
    public String getLabel() {
        return dataType+"("+dataName+")";
    }

    @Override
    public String getExasLabel() {
        return null;
    }

    @Override
    public boolean isEqualNodes(PDGNode node) {
        return false;
    }

    @Override
    public boolean isDefinition() {
        for (PDGEdge e : inEdges)
            if (((PDGDataEdge) e).type == PDGDataEdge.Type.DEFINITION)
                return true;
        return false;
    }

    @Override
    public String getDataName() {
        return dataName;
    }

    public boolean isActionNode() {
        return actionNode;
    }

    public boolean isDataNode() {
        return dataNode;
    }

    public boolean isControlNode() {
        return controlNode;
    }

    public void copyData(PDGHoleNode node) {
            this.astNode = node.astNode;
            this.astNodeType = node.astNodeType;
            this.dataName = node.dataName;
            this.dataType = node.dataType;
            this.key = node.key;
            this.dataNode = node.dataNode;
            this.controlNode = node.controlNode;
            this.actionNode = node.actionNode;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "("+getId()+")"+getLabel();
    }
}
