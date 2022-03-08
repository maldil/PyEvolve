package com.matching.fgpdg.nodes;

public abstract class PDGEdge {
    protected PDGNode source;
    protected PDGNode target;

    public PDGEdge(PDGNode source, PDGNode target) {
        this.source = source;
        this.target = target;
    }

    public abstract String getLabel();

    public PDGNode getSource() {
        return source;
    }

    public void setSource(PDGNode source) {
        this.source = source;
    }

    public void setTarget(PDGNode target) {
        this.target = target;
    }

    public PDGNode getTarget() {
        return target;
    }

    public abstract String getExasLabel();

    public abstract boolean isEqualEdge(PDGEdge edge);
}
