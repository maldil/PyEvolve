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
        if (node instanceof PDGDataNode){

        }
        else if (node instanceof PDGActionNode){

        }
        else if (node instanceof PDGControlNode){

        }
        return false;

//        return node instanceof PDGActionNode && getLabel().equals(node.getLabel());
    }

}
