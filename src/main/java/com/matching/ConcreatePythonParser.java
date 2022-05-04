package com.matching;

import com.matching.fgpdg.nodes.AlphanumericHole;
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
import org.python.core.PyObject;

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
        public Object visitList(List node) throws Exception {
            if (node.getInternalElts().size()==1 && node.getInternalElts().get(0) instanceof List
            && ((List) node.getInternalElts().get(0)).getInternalElts().size()==1 && ((List) node.getInternalElts().get(0)).getInternalElts().get(0) instanceof Hole)
            {
                AlphanumericHole alphanumericHole = new AlphanumericHole();
                updateDataOnHole(node, alphanumericHole);

                if (node.getParent()!=null) {
                    int childIndez = node.getParent().getChildren().indexOf(node);
                    node.getParent().getChildren().remove(node);
                    node.getParent().getChildren().add(childIndez, alphanumericHole);
                    if (node.getParent() instanceof Assign) {
                        if (((Assign) node.getParent()).getInternalValue() == node) {
                            ((Assign) node.getParent()).setValue(alphanumericHole);
                        } else if (((Assign) node.getParent()).getInternalTargets().contains(node)) {
                            int index = ((Assign) node.getParent()).getInternalTargets().indexOf(node);
                            ((Assign) node.getParent()).getInternalTargets().remove(node);
                            ((Assign) node.getParent()).getInternalTargets().add(index, alphanumericHole);
                        } else {
                            Assertions.UNREACHABLE();
                        }

                    }
                    else if (node.getParent() instanceof For) {
                        if (((For) node.getParent()).getInternalTarget() ==  node){
                            ((For) node.getParent()).setTarget(alphanumericHole);
                        }
                        else if (((For) node.getParent()).getInternalIter() == node ){
                            ((For) node.getParent()).setIter(alphanumericHole);
                        }
                        else if (((For) node.getParent()).getInternalBody().contains(node)){
                            int index = ((For) node.getParent()).getInternalBody().indexOf(node);
                            ((For) node.getParent()).getInternalBody().remove(node);
                            Expr expr = new Expr();
                            updateDataOnHole(node, expr);
                            expr.setValue(alphanumericHole);
                            ((For) node.getParent()).getInternalBody().add(index,expr);
                        }
                        else if (((For) node.getParent()).getInternalOrelse().contains(node)){
                            int index = ((For) node.getParent()).getInternalOrelse().indexOf(node);
                            ((For) node.getParent()).getInternalOrelse().remove(node);
                            Expr expr = new Expr();
                            updateDataOnHole(node, expr);
                            expr.setValue(alphanumericHole);
                            ((For) node.getParent()).getInternalOrelse().add(index,expr);
                        }
                        else {
                            Assertions.UNREACHABLE();
                        }

                    }
                    else if (node.getParent() instanceof Expr) {
                        if (((Expr) node.getParent()).getInternalValue()==node)
                            ((Expr) node.getParent()).setValue(alphanumericHole);
                        else
                            Assertions.UNREACHABLE();
                    }
                    else if (node.getParent() instanceof If) {
                        if (((If) node.getParent()).getInternalTest()==node){
                            ((If) node.getParent()).setTest(alphanumericHole);
                        }
                        else if (((If) node.getParent()).getInternalBody().contains(node)){
                            int index = ((If) node.getParent()).getInternalBody().indexOf(node);
                            ((If) node.getParent()).getInternalBody().remove(node);
                            Expr expr = new Expr();
                            updateDataOnHole(node, expr);
                            expr.setValue(alphanumericHole);
                            ((If) node.getParent()).getInternalBody().add(index,expr);
                        }
                        else if (((If) node.getParent()).getInternalOrelse().contains(node)){
                            int index = ((If) node.getParent()).getInternalOrelse().indexOf(node);
                            ((If) node.getParent()).getInternalOrelse().remove(node);
                            Expr expr = new Expr();
                            updateDataOnHole(node, expr);
                            expr.setValue(alphanumericHole);
                            ((If) node.getParent()).getInternalOrelse().add(index,expr);
                        }
                    }
                    else if (node.getParent() instanceof Attribute) {
                        if (((Attribute) node.getParent()).getInternalValue()==node){
                            ((Attribute) node.getParent()).setValue(alphanumericHole);
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

        private void updateDataOnHole(List node, AlphanumericHole alphanumericHole) {
            alphanumericHole.setCharStartIndex(node.getCharStartIndex());
            alphanumericHole.setCharStopIndex(node.getCharStopIndex());
            alphanumericHole.setCol_offset(node.getCol_offset());
            alphanumericHole.setLineno(node.getLineno());
            alphanumericHole.setN(((Hole) ((List) node.getInternalElts().get(0)).getInternalElts().get(0)).getN());
            alphanumericHole.setParent(node.getParent());
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
