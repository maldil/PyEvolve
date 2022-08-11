package com.matching.fgpdg.nodes;


import java.util.ArrayList;
import java.util.HashSet;

import org.python.core.PyObject;

public class PDGActionNode extends PDGNode {
    public static final String RECURSIVE = "recur";
    protected String name;
    protected String[] parameterTypes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PDGActionNode(PyObject astNode, int nodeType, String key, String type, String name) {
        super(astNode, nodeType, key);
        this.dataType = type;
        this.name = name;
    }

    public PDGActionNode(PDGNode control, String branch, PyObject astNode, int nodeType, String key, String type, String name) {
        super(astNode, nodeType, key);
        if (control != null) {
            this.control = control;
            new PDGControlEdge(control, this, branch);
        }
        this.dataType = type;
        this.name = name;
    }

    @Override
    public String getLabel() {
        return name + (parameterTypes == null ? "" : buildParameters());
    }

    @Override
    public String getExasLabel() {
        //return name + (parameterTypes == null ? "" : buildParameters());
        int index = name.lastIndexOf('.');
        return name.substring(index + 1);
    }

    private String buildParameters() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        if (parameterTypes.length > 0) {
            sb.append(parameterTypes[0]);
            for (int i = 1; i < parameterTypes.length; i++)
                sb.append("," + parameterTypes[i]);
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean isSame(PDGNode node) {
        if (node instanceof PDGActionNode) {
            return name.equals(((PDGActionNode) node).name);
        }
        return false;
    }

    @Override
    public boolean isDefinition() {
        return false;
    }

    @Override
    public String toString() {
        return "("+getId()+")"+getLabel();
    }

    public boolean hasBackwardDataDependence(PDGActionNode preNode) {
        HashSet<PDGNode> defs = new HashSet<>(), preDefs = new HashSet<>();
        HashSet<String> fields = new HashSet<>(), preFields = new HashSet<>();
        getDefinitions(defs, fields);
        preNode.getDefinitions(preDefs, preFields);
        return (overlap(defs, preDefs) || overlap(fields, preFields));
    }

    private <E> boolean overlap(HashSet<E> s1, HashSet<E> s2) {
        HashSet<E> c = new HashSet<>(s1);
        c.retainAll(s2);
        return !c.isEmpty();
    }

    private void getDefinitions(HashSet<PDGNode> defs, HashSet<String> fields) {
        for (PDGEdge e : inEdges) {
            if (e.source instanceof PDGDataNode) {
                ArrayList<PDGNode> tmpDefs = e.source.getDefinitions();
                if (tmpDefs.isEmpty())
                    fields.add(e.source.key);
                else
                    defs.addAll(tmpDefs);
            }
        }
    }

    @Override
    public  boolean isEqualNodes(PDGNode node){
        if (node instanceof PDGLazyHole)
            return true;
        return node instanceof PDGActionNode && getLabel().equals(node.getLabel());
    }
}