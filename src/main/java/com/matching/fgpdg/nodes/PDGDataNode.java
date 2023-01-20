package com.matching.fgpdg.nodes;

import org.python.antlr.ast.Name;
import org.python.antlr.ast.arg;
import org.python.core.PyObject;


public class PDGDataNode extends PDGNode {
    protected boolean isField = false, isDeclaration = false;

    protected String dataName;

    public PDGDataNode(PyObject astNode, int nodeType, String key, String dataType, String dataName) {
        super(astNode, nodeType, key);
        this.dataType = dataType;
        this.dataName = dataName;
    }

    public PDGDataNode(PyObject astNode, int nodeType, String key, String dataType, String dataName, boolean isField, boolean isDeclaration) {
        this(astNode, nodeType, key, dataType, dataName);
        this.isField = isField;
        this.isDeclaration = isDeclaration;
    }

    public PDGDataNode(PDGDataNode node) {
        this(node.astNode, node.astNodeType, node.key, node.dataType, node.dataName, node.isField, node.isDeclaration);
    }

    @Override
    public String getDataName() {
        return dataName;
    }

    @Override
    public String getLabel() {
        if (astNodeType == PyObject.STR){
            if (dataName.length()==1){
                return dataType + "(lit(" + dataName + "))";
            }
            return dataType + "(lit(" + dataName+ "))";
        }
        return dataType + "(" + dataName + ")";
    }

    @Override
    public String getExasLabel() {
//        if (astNodeType == ASTNode.NULL_LITERAL) //TODO differentiate None, this comes as Name
//            return "null";
        if (astNodeType == PyObject.NAME && ((Name) astNode).getInternalId().equals("True"))
                return "boolean";
//        if (astNodeType == ASTNode.CHARACTER_LITERAL)   //TODO differentiate String and Char
//            return "char";
        if (astNodeType == PyObject.NUM)
            return "Number";
        if (astNodeType == PyObject.STR)
            return "String";
		/*if (astNodeType == ASTNode.CHARACTER_LITERAL || astNodeType == ASTNode.STRING_LITERAL)
			return dataType + "(lit(" + dataName.substring(1, dataName.length()-1) + "))";
		return dataType;*/
        if (dataType == null)
            return "UNKNOWN";
        return dataType;
    }

    @Override
    public boolean isDefinition() {
        for (PDGEdge e : inEdges)
            if (((PDGDataEdge) e).type == PDGDataEdge.Type.DEFINITION)
                return true;
        return false;
    }

    @Override
    public boolean isSame(PDGNode node) {
        if (node instanceof PDGDataNode)
            return dataName.equals(((PDGDataNode) node).dataName) && dataType.equals(((PDGDataNode) node).dataType);
        return false;
    }

    public boolean isDummy() {
        return key.startsWith(PREFIX_DUMMY);
    }

    public PDGNode getQualifier() {
        for (PDGEdge e : inEdges)
            if (e instanceof PDGDataEdge && ((PDGDataEdge) e).type == PDGDataEdge.Type.QUALIFIER)
                return e.source;
        return null;
    }

    public void copyData(PDGDataNode node) {
        this.astNode = node.astNode;
        this.astNodeType = node.astNodeType;
        this.dataName = node.dataName;
        this.dataType = node.dataType;
        this.key = node.key;
        this.isField = node.isField;
        this.isDeclaration = node.isDeclaration;
    }

    @Override
    public String toString() {
        return "("+getId()+")"+getLabel();
    }

    public boolean isField() {
        return isField;
    }

    @Override
    public  boolean isEqualNodes(PDGNode node){
        if (node instanceof PDGDataNode){
            if ((this.astNodeType == node.astNodeType) &&  (( this.astNodeType== PyObject.NAME)
                    || (this.astNodeType == PyObject.ARG))){
                if ((getDataType()!=null && node.getDataType()!=null) && node.getDataType().equals(getDataType()))
                    return true;
                else if ((getDataType()!=null && node.getDataType()!=null) && (node.getDataType().equals("Any")||getDataType().equals("Any")))
                    return true;
                else
                    return false;
            }
            else if (this.astNodeType==PyObject.NUM &&(this.astNodeType == node.astNodeType)){
                if (((PDGDataNode) node).dataName.equals(getDataName())){
                    return true;
                }
                else{
                    return false;
                }

            }
            else if (this.astNodeType==PyObject.STR && this.astNodeType == node.astNodeType){
                return true;
            }
        }
        else if (node instanceof PDGAlphHole){
            return true;
        }
        else if (node instanceof PDGLazyHole){
            return true;
        }
        else{
            return false;
        }
        return getLabel().equals(node.getLabel());
    }
}
