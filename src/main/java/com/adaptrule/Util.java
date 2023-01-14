package com.adaptrule;

import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.core.PyObject;

public class Util {
    public static boolean isChildNode(PythonTree childNode,PythonTree parentNode){
        CheckChildNode childChecker = new CheckChildNode(childNode);
        try {
            childChecker.visit(parentNode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return childChecker.isChild();
    }

    static class CheckChildNode extends Visitor{
        private boolean isChild = false;
        private PythonTree childTree;
        public CheckChildNode(PythonTree childTree) {
            this.childTree = childTree;
        }
        @Override
        public Object unhandled_node(PythonTree node) throws Exception {
            if (node.getChildren()!=null && node.getChildren().contains(childTree))
                isChild=true;
            return super.unhandled_node(node);
        }

        @Override
        public void preVisit(PyObject node) {

        }

        @Override
        public void postVisit(PyObject node) {

        }
        public boolean isChild() {
            return isChild;
        }
    }
}
