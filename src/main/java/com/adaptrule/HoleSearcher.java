package com.adaptrule;

import org.python.antlr.Visitor;
import org.python.antlr.ast.AlphHole;
import org.python.antlr.ast.For;
import org.python.antlr.ast.Hole;
import org.python.antlr.base.expr;
import org.python.core.PyObject;

import java.util.ArrayList;
import java.util.List;

public class HoleSearcher extends Visitor {
    boolean holeContained=false;
    private int largestHoleID=1;
    private List<expr> holes= new ArrayList<>();


    @Override
    public void preVisit(PyObject node) {

    }

    @Override
    public void postVisit(PyObject node) {

    }

    @Override
    public Object visitHole(Hole node) throws Exception {
        holeContained=true;
        if (Integer.parseInt(node.getN().toString())>largestHoleID)
            largestHoleID= Integer.parseInt(node.getN().toString());
        holes.add(node);
        return null;

    }
    @Override
    public Object visitAlphHole(AlphHole node) throws Exception {
        holeContained=true;
        holes.add(node);
        if (Integer.parseInt(node.getN().toString())>largestHoleID)
            largestHoleID= Integer.parseInt(node.getN().toString());
        return null;
    }

    public int getLargestHoleID() {
        return largestHoleID;
    }

    public List<expr> getHoles() {
        return holes;
    }
}
