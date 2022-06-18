package com.matching.fgpdg.nodes;

import com.matching.fgpdg.nodes.PDGHoleNode;
import org.python.core.PyObject;

public class PDGAlphHole extends PDGHoleNode {


    public PDGAlphHole(PyObject astNode, int nodeType, String value, String key, String dataType, String dataName,boolean isDataNode,boolean isActionNode,boolean isContralNode) {
        super(astNode, nodeType, value, key, dataType, dataName, isDataNode, isActionNode, isContralNode);
    }
//
//    @Override
//    public boolean isEqualNodes(PDGNode node) {
//        return false;
//    }

    @Override
    public  boolean isEqualNodes(PDGNode node){
        if (node instanceof PDGDataNode && this.isDataNode()  ){
            return true;
        }
        else if (node instanceof PDGActionNode && (this.isDataNode() && this.isActionNode()) ){
            return true;
        }
        else if (node instanceof PDGControlNode && this.isControlNode()){
            return true;
        }
        return false;

//        return node instanceof PDGActionNode && getLabel().equals(node.getLabel());
    }




}
