package com.matching;

import com.matching.fgpdg.nodes.ast.AlphanumericHole;
import com.matching.fgpdg.nodes.ast.LazyHole;
import com.utils.Assertions;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CharStream;
import org.python.antlr.AnalyzingParser;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.*;
import org.python.antlr.ast.Module;
import org.python.antlr.base.expr;
import org.python.antlr.base.mod;
import org.python.antlr.base.stmt;

import java.io.IOException;
import java.io.InputStream;

public class ConcreatePythonParser  {
    public ConcreatePythonParser() {

    }

    public Module parse(String fileName) {
        URLModule inputStream = new URLModule(fileName);
        CharStream file = null;
        try {
            file = new ANTLRInputStream(inputStream.getInputStream(fileName));
            PythonParser parser =new PythonParser(file, inputStream.getName(), "UTF-8");
            return  prepareHoles(parser.parseModule());
        } catch (IOException e) {
            Assertions.UNREACHABLE();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.UNREACHABLE();
            return null;
        }
    }

    private Module prepareHoles( mod module) throws Exception {
         ParentUpdater pUpdater = new ParentUpdater();
         pUpdater.visit(module);

        PyHoleVisitorAndRepair holeVisitor = new PyHoleVisitorAndRepair();
        holeVisitor.visit(module );
        return (Module)module;
    }



    public Module parse(InputStream code)  {
        CharStream file = null;
        try {
            file = new ANTLRInputStream(code);
        } catch (IOException e) {
            Assertions.UNREACHABLE();
        }
        PythonParser parser =new PythonParser(file, "", "UTF-8");
        return (Module)parser.parseModule();
    }

    class PythonParser extends AnalyzingParser {
        public PythonParser(CharStream stream, String filename, String encoding) {
            super(stream, filename, encoding);
        }
    }

    class PyHoleVisitorAndRepair extends Visitor {
        @Override
        public Object visitAttribute(Attribute node) throws Exception {
                if (node.getInternalHole() !=null){
                    Hole hole = new LazyHole();
                    hole.setCharStartIndex(node.getCharStartIndex());
                    hole.setCharStopIndex(node.getCharStopIndex());
                    hole.setCol_offset(node.getCol_offset());
                    hole.setLineno(node.getLineno());
                    hole.setN( node.getInternalHole().getN());
                    hole.setParent(node.getParent());
                    node.setAttr(hole);
                    node.setInternalHole(hole);
                }
                else if (node.getInternalAlphHole() !=null){
                    Hole hole = new AlphanumericHole();
                    hole.setCharStartIndex(node.getCharStartIndex());
                    hole.setCharStopIndex(node.getCharStopIndex());
                    hole.setCol_offset(node.getCol_offset());
                    hole.setLineno(node.getLineno());
                    hole.setN( node.getInternalAlphHole().getN());
                    hole.setParent(node.getParent());
                    node.setAttr(hole);
                    node.setInternalHole(hole);
                }




            return super.visitAttribute (node);
        }

        @Override
        public Object visitList(List node) throws Exception {
            Hole hole=null;
            if (node.getInternalElts().size()==1 && node.getInternalElts().get(0) instanceof AlphHole){
                hole = new AlphanumericHole();
                updateDataOnHole(node, hole);
            }
            else if (node.getInternalElts().size()==1 && node.getInternalElts().get(0) instanceof Hole){
                hole = new LazyHole();
                updateDataOnHole(node, hole);
            }
            if (hole!=null)
            {
                if (node.getParent()!=null) {
                    int childIndez = node.getParent().getChildren().indexOf(node);
                    node.getParent().getChildren().remove(node);
                    node.getParent().getChildren().add(childIndez, hole);
                    if (node.getParent() instanceof Assign) {
                        if (((Assign) node.getParent()).getInternalValue() == node) {
                            ((Assign) node.getParent()).setValue(hole);
                        } else if (((Assign) node.getParent()).getInternalTargets().contains(node)) {
                            int index = ((Assign) node.getParent()).getInternalTargets().indexOf(node);
                            ((Assign) node.getParent()).getInternalTargets().remove(node);
                            ((Assign) node.getParent()).getInternalTargets().add(index, hole);
                        } else {
                            Assertions.UNREACHABLE();
                        }

                    }
                    else if (node.getParent() instanceof For) {
                        if (((For) node.getParent()).getInternalTarget() ==  node){
                            ((For) node.getParent()).setTarget(hole);
                        }
                        else if (((For) node.getParent()).getInternalIter() == node ){
                            ((For) node.getParent()).setIter(hole);
                        }
                        else if (((For) node.getParent()).getInternalBody().contains(node)){
                            int index = ((For) node.getParent()).getInternalBody().indexOf(node);
                            ((For) node.getParent()).getInternalBody().remove(node);
                            Expr expr = new Expr();
                            updateDataOnHole(node, expr);
                            expr.setValue(hole);
                            ((For) node.getParent()).getInternalBody().add(index,expr);
                        }
                        else if (((For) node.getParent()).getInternalOrelse().contains(node)){
                            int index = ((For) node.getParent()).getInternalOrelse().indexOf(node);
                            ((For) node.getParent()).getInternalOrelse().remove(node);
                            Expr expr = new Expr();
                            updateDataOnHole(node, expr);
                            expr.setValue(hole);
                            ((For) node.getParent()).getInternalOrelse().add(index,expr);
                        }
                        else {
                            Assertions.UNREACHABLE();
                        }

                    }
                    else if (node.getParent() instanceof Expr) {
                        if (((Expr) node.getParent()).getInternalValue()==node)
                            ((Expr) node.getParent()).setValue(hole);
                        else
                            Assertions.UNREACHABLE();
                    }
                    else if (node.getParent() instanceof If) {
                        if (((If) node.getParent()).getInternalTest()==node){
                            ((If) node.getParent()).setTest(hole);
                        }
                        else if (((If) node.getParent()).getInternalBody().contains(node)){
                            int index = ((If) node.getParent()).getInternalBody().indexOf(node);
                            ((If) node.getParent()).getInternalBody().remove(node);
                            Expr expr = new Expr();
                            updateDataOnHole(node, expr);
                            expr.setValue(hole);
                            ((If) node.getParent()).getInternalBody().add(index,expr);
                        }
                        else if (((If) node.getParent()).getInternalOrelse().contains(node)){
                            int index = ((If) node.getParent()).getInternalOrelse().indexOf(node);
                            ((If) node.getParent()).getInternalOrelse().remove(node);
                            Expr expr = new Expr();
                            updateDataOnHole(node, expr);
                            expr.setValue(hole);
                            ((If) node.getParent()).getInternalOrelse().add(index,expr);
                        }
                    }
                    else if (node.getParent() instanceof Attribute) {
                        if (((Attribute) node.getParent()).getInternalValue()==node){
                            ((Attribute) node.getParent()).setValue(hole);
                        }
//                        else if (((Attribute) node.getParent()).getInternalHole() instanceof Hole)

                    }


                    else {
//                        Assertions.UNREACHABLE(node.getParent().getClass().toString());
                    }
                }
            }

            return super.visitList (node);

        }

        private void updateDataOnHole(List node, Expr expr) {
            expr.setCharStartIndex(node.getCharStartIndex());
            expr.setCharStopIndex(node.getCharStopIndex());
            expr.setCol_offset(node.getCol_offset());
            expr.setLineno(node.getLineno());
            expr.setParent(node.getParent());
        }

        private void updateDataOnHole(List node, Hole hole) {
            hole.setCharStartIndex(node.getCharStartIndex());
            hole.setCharStopIndex(node.getCharStopIndex());
            hole.setCol_offset(node.getCol_offset());
            hole.setLineno(node.getLineno());
            if (node.getInternalElts().get(0) instanceof Hole)
                hole.setN( ((Hole)(node.getInternalElts().get(0))).getN());
            else if (node.getInternalElts().get(0) instanceof AlphHole)
                hole.setN( ((AlphHole)(node.getInternalElts().get(0))).getN());
            hole.setParent(node.getParent());
        }



    }

    class ParentUpdater extends Visitor {
        @Override
        public Object visitExpr(Expr node) throws Exception {
            updateParent(node);

//            node.getInternalValue().setParent(node);
            return super.visitExpr(node);
        }

        private void updateParent(stmt node) {
            for (PythonTree child : node.getChildren()) {
                child.setParent(node);
            }
        }

        private void updateParent(expr node) {
            for (PythonTree child : node.getChildren()) {
                child.setParent(node);
            }
        }

        @Override
        public Object visitList(List node) throws Exception {
            updateParent(node);
            return super.visitList(node);
        }

        @Override
        public Object visitAttribute(Attribute node) throws Exception {
            updateParent(node);
            return super.visitAttribute(node);
        }
    }

}
