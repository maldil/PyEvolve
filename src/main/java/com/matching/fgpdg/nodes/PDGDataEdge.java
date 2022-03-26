package com.matching.fgpdg.nodes;

public class PDGDataEdge extends PDGEdge {
    public enum Type {RECEIVER, PARAMETER, DEFINITION, RE_DEFINITION, REFERENCE, CONDITION, DEPENDENCE, QUALIFIER, MAP}

    protected Type type;

    public PDGDataEdge(PDGNode source, PDGNode target, Type type) {
        super(source, target);
        this.type = type;
        this.source.addOutEdge(this);
        this.target.addInEdge(this);
    }

    public Type getType() {
        return type;
    }

    @Override
    public String getLabel() {
        switch (type) {
            case RECEIVER: return "recv";
            case PARAMETER: return "para";
            case DEFINITION: return "def";
            case REFERENCE: return "ref";
            case CONDITION: return "cond";
            case DEPENDENCE: return "dep";
            case RE_DEFINITION: return "re_def";
            case QUALIFIER: return "qual";
            case MAP: return "map";
            default: return "";
        }
    }

    @Override
    public String getExasLabel() {
        switch (type) {
            case RECEIVER: return "_recv_";
            case PARAMETER: return "_para_";
            case DEFINITION: return "_def_";
            case REFERENCE: return "_ref_";
            case CONDITION: return "_cond_";
            case DEPENDENCE: return "_dep_";
            case QUALIFIER: return "_qual_";
            case MAP: return "_map_";
            default: return "_data_";
        }
    }

    @Override
    public String toString() {
        return getLabel();
    }

    @Override
    public  boolean isEqualEdge(PDGEdge node){
        return node instanceof PDGDataEdge && getLabel().equals(node.getLabel());
    }
}

