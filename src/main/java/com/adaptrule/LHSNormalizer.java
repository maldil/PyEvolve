package com.adaptrule;

import com.matching.fgpdg.nodes.ast.AlphanumericHole;
import com.matching.fgpdg.nodes.ast.LazyHole;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.*;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;
import org.python.core.PyLong;
import org.python.core.PyObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LHSNormalizer extends Visitor {
    int start ;
    Map<PythonTree, List<PythonTree>> codeAndHole= new HashMap<>();
    public LHSNormalizer(int holeStarter) {
        this.start=holeStarter;
    }


//    @Override
//    public Object unhandled_node(PythonTree node) throws Exception {
//        if(normalize(node))
//            return null;
//        return super.unhandled_node(node);
//    }

    @Override
    public void preVisit(PyObject node) {

    }

    @Override
    public void postVisit(PyObject node) {

    }

    @Override
    public Object visitFor(For node)  throws Exception {
        if(normalize(node))
            return null;
        return super.visitFor(node);
    }

    @Override
    public Object visitExpr(Expr node)  throws Exception {
        if(normalize(node))
            return null;
        return super.visitExpr(node);
    }

    @Override
    public Object visitAttribute(Attribute node)  throws Exception {

        return super.visitAttribute(node);
    }

    @Override
    public Object visitName(Name node)  throws Exception {
        return super.visitName(node);
    }

    private boolean normalize(PythonTree node) {
        HoleSearcher searcher = new HoleSearcher();
        try {
            searcher.visit(node);
            if (!searcher.holeContained){
                Hole hole;
                if (node instanceof Name)
                     hole = new AlphanumericHole();
                else
                     hole = new LazyHole();
                PyLong pyLong = new PyLong(start);
                hole.setParent(node.getParent());
                hole.setN(pyLong);
                start++;
                codeAndHole.put(node, List.of(hole));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Object visitAssign(Assign node)  throws Exception {
        if(normalize(node))
            return null;
        return super.visitAssign(node);
    }

    @Override
    public Object visitTryExcept(TryExcept node)  throws Exception {
        if(normalize(node))
            return null;
        return super.visitTryExcept(node);
    }

    @Override
    public Object visitCall(Call node)  throws Exception {
        if(normalize(node))
            return null;
        return super.visitCall(node);
    }

    @Override
    public Object visitDict(Dict node)  throws Exception {
        if(normalize(node))
            return null;
        return super.visitDict(node);
    }
}
