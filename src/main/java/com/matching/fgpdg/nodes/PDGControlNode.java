package com.matching.fgpdg.nodes;


import com.matching.fgpdg.PDGGraph;

import org.python.core.PyObject;

public class PDGControlNode extends PDGNode {

    public PDGControlNode(PDGNode control, String branch, PyObject astNode, int nodeType) {
        super(astNode, nodeType);
        this.control = control;
        new PDGControlEdge(control, this, branch);
    }

    public PDGControlNode(PyObject astNode,int nodeType,PDGNode control){
        super(astNode, nodeType);
        this.control = control;
    }

    @Override
    public String getLabel() {
        return PyObject.nodeClassForASTName(astNodeType);
    }

    @Override
    public String getExasLabel() {
        return PyObject.nodeClassForASTName(astNodeType);
    }

    @Override
    public boolean isDefinition() {
        return false;
    }

    @Override
    public String toString() {
        return "("+getId()+")"+getLabel();
    }

    public PDGGraph getBody() {
        PDGGraph g = new PDGGraph(null);
        g.getNodes().add(this);
        for (PDGEdge e : outEdges) {
            PDGNode node = e.target;
            if (!node.isEmptyNode())
                g.getNodes().add(node);
        }
        for (PDGEdge e : outEdges) {
            PDGNode node = e.target;
            node.addNeighbors(g.getNodes());
        }
        g.getNodes().remove(this);
        return g;
    }

    @Override
    public boolean isSame(PDGNode node) {
        if (node instanceof PDGControlNode)
            return astNodeType == node.astNodeType;
        return false;
    }

    @Override
    public  boolean isEqualNodes(PDGNode node){
        if (node instanceof PDGLazyHole)
            return true;
        return node instanceof PDGControlNode && getLabel().equals(node.getLabel());
    }
}