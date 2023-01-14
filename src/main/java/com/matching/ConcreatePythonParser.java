package com.matching;

import com.matching.fgpdg.nodes.ast.AlphanumericHole;
import com.matching.fgpdg.nodes.ast.LazyHole;
import com.utils.Assertions;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.python.antlr.AnalyzingParser;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.*;
import org.python.antlr.ast.Module;
import org.python.antlr.base.*;
import org.python.core.PyObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConcreatePythonParser  {
    public ConcreatePythonParser() {

    }

    public Module parse(String fileName) {
        URLModule inputStream = new URLModule(fileName);
        CharStream file = null;
        try {
            file = new ANTLRInputStream(inputStream.getInputStream(fileName));
            PythonParser parser =new PythonParser(file, inputStream.getName(), "UTF-8");
            mod mod = parser.parseModule();
            ParentUpdater pUpdater = new ParentUpdater();
            pUpdater.visit(mod);
            return  (Module)mod;
        } catch (Exception | NoClassDefFoundError e) {
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

    public Module parseTemplates(String code) throws Exception {
        code = convertComByTemplateToParsableCode(code);
        ANTLRStringStream antlrSting = new ANTLRStringStream(code);
        AnalyzingParser p = new AnalyzingParser(antlrSting, "", "ascii");
        return prepareHoles(p.parseModule());
    }

    public String convertComByTemplateToParsableCode(String code){
        Pattern alpPat = Pattern.compile("(:\\[\\[l)(\\d+)(\\]\\])");
        Matcher alpMat = alpPat.matcher(code);
        StringBuilder sb = new StringBuilder();
        while (alpMat.find()) {
            String text = alpMat.group(2);
            int degit = Integer.parseInt(text);
            alpMat.appendReplacement(sb, "[\\$"+degit+"]");
//            alpMat.appendReplacement(sb, Matcher.quoteReplacement(text));
        }
        alpMat.appendTail(sb);
//        String matcher = code.replaceAll(String.valueOf(alpPat),"[:\\$\d]");

        Pattern lazyPat = Pattern.compile("(:\\[l)(\\d+)(\\])");
        Matcher lazyMat = lazyPat.matcher(sb.toString());
        StringBuilder sb1 = new StringBuilder();

        while (lazyMat.find()) {
            String text = lazyMat.group(2);
            int degit = Integer.parseInt(text);
            lazyMat.appendReplacement(sb1, "[\\%"+degit+"]");
//            alpMat.appendReplacement(sb, Matcher.quoteReplacement(text));
        }
        lazyMat.appendTail(sb1);
        return sb1.toString();
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
        public Object visitIndex(Index node) throws Exception {
            Hole lhole=null;
            if (node.getInternalValue() instanceof List && ((List)node.getInternalValue()).getInternalElts().size()==1 &&
                    ((List)node.getInternalValue()).getInternalElts().get(0) instanceof AlphHole){
                lhole = new AlphanumericHole();
                updateDataOnHole((List) node.getInternalValue(), lhole);
                node.setValue(lhole);
            }
            else if (node.getInternalValue() instanceof List && ((List)node.getInternalValue()).getInternalElts().size()==1 && ((List)node.getInternalValue()).getInternalElts().get(0) instanceof Hole){
                lhole = new LazyHole();
                updateDataOnHole((List) node.getInternalValue(), lhole);
                node.setValue(lhole);
            }
            return super.visitIndex (node);
        }

        @Override
        public void preVisit(PyObject node) {

        }

        @Override
        public void postVisit(PyObject node) {

        }

        @Override
        public Object visitCall(Call node) throws Exception {
            if (node.getInternalFunc() instanceof Attribute){
                if (((Attribute)node.getInternalFunc()).getInternalHole() !=null){
                    Hole hole = new LazyHole();
                    hole.setCharStartIndex(((Attribute)node.getInternalFunc()).getCharStartIndex());
                    hole.setCharStopIndex(((Attribute)node.getInternalFunc()).getCharStopIndex());
                    hole.setCol_offset(((Attribute)node.getInternalFunc()).getCol_offset());
                    hole.setLineno(((Attribute)node.getInternalFunc()).getLineno());
                    hole.setN( ((Attribute)node.getInternalFunc()).getInternalHole().getN());
                    hole.setParent(((Attribute)node.getInternalFunc()).getParent());
                    ((Attribute)node.getInternalFunc()).setAttr(hole);
                    ((Attribute)node.getInternalFunc()).setInternalHole(hole);
                }
                else if (((Attribute)node.getInternalFunc()).getInternalAlphHole()!=null){
                    Hole hole = new AlphanumericHole();
                    hole.setCharStartIndex(((Attribute)node.getInternalFunc()).getCharStartIndex());
                    hole.setCharStopIndex(((Attribute)node.getInternalFunc()).getCharStopIndex());
                    hole.setCol_offset(((Attribute)node.getInternalFunc()).getCol_offset());
                    hole.setLineno(((Attribute)node.getInternalFunc()).getLineno());
                    hole.setN( ((Attribute)node.getInternalFunc()).getInternalAlphHole().getN());
                    hole.setParent(((Attribute)node.getInternalFunc()).getParent());
                    ((Attribute)node.getInternalFunc()).setAttr(hole);
                    ((Attribute)node.getInternalFunc()).setInternalHole(hole);
                }


            }
            java.util.List<expr> arguments = new ArrayList<>(node.getInternalArgs());

            for (expr elt : node.getInternalArgs()) {
                Hole lhole=null;
                if (elt instanceof List && ((List)elt).getInternalElts().size()==1 && ((List)elt).getInternalElts().get(0) instanceof AlphHole){
                    lhole = new AlphanumericHole();
                    updateDataOnHole((List) elt, lhole);
                }
                else if (elt instanceof List && ((List)elt).getInternalElts().size()==1 && ((List)elt).getInternalElts().get(0) instanceof Hole){
                    lhole = new LazyHole();
                    updateDataOnHole((List) elt, lhole);
                }
                if (lhole!=null){
                    int index = arguments.indexOf(elt);
                    arguments.remove(elt);
                    arguments.add(index,lhole);
                }


            }
            node.setArgs(arguments);


            return super.visitCall(node);
        }


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
        public Object visitSet(Set node) throws Exception {
            java.util.List<expr> childValues = new ArrayList<>(node.getInternalElts());

            for (expr elt : node.getInternalElts()) {
                Hole lhole=null;
                if (elt instanceof List && ((List)elt).getInternalElts().size()==1 && ((List)elt).getInternalElts().get(0) instanceof AlphHole){
                    lhole = new AlphanumericHole();
                    updateDataOnHole((List) elt, lhole);
                }
                else if (elt instanceof List && ((List)elt).getInternalElts().size()==1 && ((List)elt).getInternalElts().get(0) instanceof Hole){
                    lhole = new LazyHole();
                    updateDataOnHole((List) elt, lhole);
                }
                if (lhole!=null){
                    int index = childValues.indexOf(elt);
                    childValues.remove(elt);
                    childValues.add(index,lhole);
                    int childIndex= node.getChildren().indexOf(elt);
                    node.getChildren().remove(elt);
                    node.getChildren().add(childIndex,lhole);
                }

            }
            if (!node.getElts().equals(childValues))
                node.setElts(childValues);

            return super.visitSet(node);
        }

        @Override
        public Object visitDict(Dict node) throws Exception {
            java.util.List<expr> childValues = new ArrayList<>(node.getInternalValues());
            java.util.List<expr> childKeys = new ArrayList<>(node.getInternalKeys());
            for (expr elt : node.getInternalValues()) {
                Hole lhole=null;
                if (elt instanceof List && ((List)elt).getInternalElts().size()==1 && ((List)elt).getInternalElts().get(0) instanceof AlphHole){
                    lhole = new AlphanumericHole();
                    updateDataOnHole((List) elt, lhole);
                }
                else if (elt instanceof List && ((List)elt).getInternalElts().size()==1 && ((List)elt).getInternalElts().get(0) instanceof Hole){
                    lhole = new LazyHole();
                    updateDataOnHole((List) elt, lhole);
                }
                if (lhole!=null){
                    int index = childValues.indexOf(elt);
                    childValues.remove(elt);
                    childValues.add(index,lhole);
//                    int childIndex= node.getChildren().indexOf(elt);
//                    node.getChildren().remove(elt);
//                    node.getChildren().add(childIndex,lhole);
                }

            }

            if (!childValues.equals(node.getInternalValues()))
                node.setValues(childValues);

            for (expr elt : node.getInternalKeys()) {
                Hole lhole=null;
                if (elt instanceof List && ((List)elt).getInternalElts().size()==1 && ((List)elt).getInternalElts().get(0) instanceof AlphHole){
                    lhole = new AlphanumericHole();
                    updateDataOnHole((List) elt, lhole);
                }
                else if (elt instanceof List && ((List)elt).getInternalElts().size()==1 && ((List)elt).getInternalElts().get(0) instanceof Hole){
                    lhole = new LazyHole();
                    updateDataOnHole((List) elt, lhole);
                }
                if (lhole!=null){
                    int index = childKeys.indexOf(elt);
                    childKeys.remove(elt);
                    childKeys.add(index,lhole);
//                    int childIndex= node.getChildren().indexOf(elt);
//                    node.getChildren().remove(elt);
//                    node.getChildren().add(childIndex,lhole);
                }

            }

            if (!childKeys.equals(node.getInternalValues()))
                node.setKeys(childKeys);

            return super.visitDict(node);

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
            else if (node.getInternalElts().size()>0){
                java.util.List<expr> childALT = new ArrayList<>(node.getInternalElts());

                for (expr elt : node.getInternalElts()) {
                    Hole lhole=null;
                    if (elt instanceof List && ((List)elt).getInternalElts().size()==1 && ((List)elt).getInternalElts().get(0) instanceof AlphHole){
                            lhole = new AlphanumericHole();
                            updateDataOnHole((List) elt, lhole);
                    }
                    else if (elt instanceof List && ((List)elt).getInternalElts().size()==1 && ((List)elt).getInternalElts().get(0) instanceof Hole){
                            lhole = new LazyHole();
                            updateDataOnHole((List) elt, lhole);
                    }
                    if (lhole!=null){
                        int index = childALT.indexOf(elt);
                        childALT.remove(elt);
                        childALT.add(index,lhole);
                        int childIndex= node.getChildren().indexOf(elt);
                        node.getChildren().remove(elt);
                        node.getChildren().add(childIndex,lhole);
                    }

                }
                if (childALT.size()>0){
                    node.setElts( childALT);

                }
                //                        Hole hole=null;
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
                    else if (node.getParent() instanceof ListComp){
                        if (((ListComp)node.getParent()).getInternalElt()==node){
                            ((ListComp)node.getParent()).setElt(hole);
                        }
                    }
                    else if (node.getParent() instanceof SetComp){
                        if (((SetComp)node.getParent()).getInternalElt()==node){
                            ((SetComp)node.getParent()).setElt(hole);
                        }
                    }
                    else if (node.getParent() instanceof DictComp){
                        if (((DictComp)node.getParent()).getInternalValue()==node){
                            ((DictComp)node.getParent()).setValue(hole);
                        }
                        else if (((DictComp)node.getParent()).getInternalKey()==node){
                            ((DictComp)node.getParent()).setKey(hole);
                        }

                    }
                    else if (node.getParent() instanceof GeneratorExp){
                        if (((GeneratorExp) node.getParent()).getInternalElt()==node){
                            ((GeneratorExp)node.getParent()).setElt(hole);
                        }
                    }
                    else if (node.getParent() instanceof comprehension){
                        if (((comprehension) node.getParent()).getInternalTarget()==node){
                            ((comprehension)node.getParent()).setTarget(hole);
                        }
                        else if (((comprehension) node.getParent()).getInternalIter()==node){
                            ((comprehension)node.getParent()).setIter(hole);
                        }
                        int index= ((comprehension) node.getParent()).getInternalIfs().indexOf(node);
                        if (index!=-1){
                            ((comprehension) node.getParent()).getInternalIfs().remove(node);
                            ((comprehension) node.getParent()).getInternalIfs().add(index,hole);
                        }
                    }
                    else if (node.getParent() instanceof BinOp){
                        if(((BinOp) node.getParent()).getInternalLeft()==node)
                            ((BinOp) node.getParent()).setLeft(hole);
                        else if (((BinOp) node.getParent()).getInternalRight()==node)
                            ((BinOp) node.getParent()).setRight(hole);
                    }
                    else if (node.getParent() instanceof  Return){
                        if (((Return) node.getParent()).getInternalValue()==node){
                            ((Return) node.getParent()).setValue(hole);
                        }
                    }
                    else if (node.getParent() instanceof Delete){
                        if (((Delete) node.getParent()).getInternalTargets().contains(node)){
                            int index = ((Delete) node.getParent()).getInternalTargets().indexOf(node);
                            ((Delete) node.getParent()).getInternalTargets().remove(node);
                            ((Delete) node.getParent()).getInternalTargets().add(index,hole);
                        }
                    }
                    else if (node.getParent() instanceof AugAssign){
                        if (((AugAssign) node.getParent()).getInternalValue()==node){
                            ((AugAssign) node.getParent()).setValue(hole);
                        }
                        else if (((AugAssign) node.getParent()).getInternalTarget()==node){
                            ((AugAssign) node.getParent()).setTarget(hole);
                        }
                    }
                    else if (node.getParent() instanceof While){
                        if (((While) node.getParent()).getInternalTest()==node){
                            ((While) node.getParent()).setTest(hole);
                        }
                    }
                    else if (node.getParent() instanceof With){

                    }
                    else if (node.getParent() instanceof withitem){
                        if (((withitem) node.getParent()).getInternalContext_expr()==node){
                            ((withitem) node.getParent()).setContext_expr(hole);
                        }
                        else if (((withitem) node.getParent()).getInternalOptional_vars()==node){
                            ((withitem) node.getParent()).setOptional_vars(hole);
                        }
                    }

                    else if (node.getParent() instanceof  Raise){
                        if(((Raise) node.getParent()).getInternalCause()==node){
                            ((Raise) node.getParent()).setCause(hole);
                        }
                        else if (((Raise) node.getParent()).getInternalExc()==node){
                            ((Raise) node.getParent()).setExc(hole);
                        }
                    }

                    else if ((node.getParent() instanceof TryExcept)){

                    }

                    else if ((node.getParent() instanceof ExceptHandler)){
                        if (((ExceptHandler) node.getParent()).getInternalType()==node){
                            ((ExceptHandler) node.getParent()).setExceptType(hole);
                        }

                    }

                    else if (node.getParent() instanceof Assert){
                        if (((Assert) node.getParent()).getInternalTest()==node){
                            ((Assert) node.getParent()).setTest(hole);
                        }
                        else if (((Assert) node.getParent()).getInternalMsg()==node){
                            ((Assert) node.getParent()).setMsg(hole);
                        }
                    }

                    else if (node.getParent() instanceof BoolOp){
                        if (((BoolOp)node.getParent()).getInternalValues().contains(node)){
                            int index = ((BoolOp)node.getParent()).getInternalValues().indexOf(node);
                            ((BoolOp)node.getParent()).getInternalValues().remove(node);
                            ((BoolOp)node.getParent()).getInternalValues().add(index,hole);
                        }
                    }
                    else if (node.getParent() instanceof UnaryOp){
                        if (((UnaryOp)node.getParent()).getInternalOperand()==node){
                            ((UnaryOp)node.getParent()).setOperand(hole);
                        }
                    }
                    else if (node.getParent() instanceof Lambda){
                        if (((Lambda) node.getParent()).getInternalBody()==node){
                            ((Lambda) node.getParent()).setBody(node);
                        }
                    }
                    else if (node.getParent() instanceof IfExp){
                        if (((IfExp)node.getParent()).getInternalOrelse()==node){
                            ((IfExp)node.getParent()).setOrelse(hole);
                        }
                        else if (((IfExp)node.getParent()).getInternalTest()==node){
                            ((IfExp)node.getParent()).setTest(hole);
                        }
                    }
                    else if (node.getParent() instanceof Dict){
                    }

                    else if (node.getParent() instanceof Set){

                    }
                    else if (node.getParent() instanceof List){

                    }
                    else if (node.getParent() instanceof Call){
                        if (((Call)node.getParent()).getInternalFunc()==node){
                            ((Call)node.getParent()).setFunc(hole);
                        }
                        else if (((Call)node.getParent()).getInternalArgs().contains(node)){
                            int index = ((Call)node.getParent()).getInternalArgs().indexOf(node);
                            ((Call)node.getParent()).getInternalArgs().remove(node);
                            ((Call)node.getParent()).getInternalArgs().add(index,hole);
                        }
                        for (keyword internalKeyword : ((Call) node.getParent()).getInternalKeywords()) {
                            if (internalKeyword.getInternalValue()==node){
                                internalKeyword.setValue(hole);
                            }
                        }

                    }
                    else if (node.getParent() instanceof Compare){
                        if (((Compare)node.getParent()).getInternalLeft()==node){
                            ((Compare)node.getParent()).setLeft(hole);
                        }
                        else if (((Compare)node.getParent()).getInternalComparators().contains(node)){
                            int index = ((Compare)node.getParent()).getInternalComparators().indexOf(node);
                            ((Compare)node.getParent()).getInternalComparators().remove(node);
                            ((Compare)node.getParent()).getInternalComparators().add(index,hole);
                        }
                    }
                    else if (node.getParent() instanceof Subscript){
                        if (((Subscript)node.getParent()).getInternalValue()==node){
                            ((Subscript)node.getParent()).setValue(hole);
                        }

                    }
                    else if (node.getParent() instanceof Tuple){
                        java.util.List<expr> tuple = new ArrayList<>(((Tuple)node.getParent()).getInternalElts());
                        if (tuple.contains(node)){
                            int index =tuple.indexOf(node);
                            tuple.remove(node);
                            tuple.add(index,hole);
                        }
                        ((Tuple)node.getParent()).setElts(tuple);

                    }
                    else if (node.getParent() instanceof keyword){
                        if (((keyword) node.getParent()).getInternalValue()==node){
                            ((keyword) node.getParent()).setValue(hole);
                        }
                    }
                    else if (node.getParent() instanceof Index){
                        if (((Index)node.getParent()).getInternalValue()==node){
                            ((Index) node.getParent()).setValue(hole);
                        }
                    }
                    else if (node.getParent() instanceof ExtSlice){
                        if (((ExtSlice)node.getParent()).getInternalDims().contains(node)){

                        }
                    }
                    else {
                        Assertions.UNREACHABLE(node.getParent().getClass().toString());
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
        public Object visitExtSlice(ExtSlice node) throws Exception {
            updateParent(node);

//            node.getInternalValue().setParent(node);
            return super.visitExtSlice(node);
        }

        @Override
        public Object visitExpr(Expr node) throws Exception {
            updateParent(node);

//            node.getInternalValue().setParent(node);
            return super.visitExpr(node);
        }

        @Override
        public void preVisit(PyObject node) {

        }

        @Override
        public void postVisit(PyObject node) {

        }

        private void updateParent(stmt node) {
            for (PythonTree child : node.getChildren()) {
                child.setParent(node);
            }
        }

        private void updateParent(PythonTree node) {
            for (PythonTree child : node.getChildren()) {
                child.setParent(node);
            }
        }
        private void updateParent(expr node) {
            if (node.getChildren()!=null){
                for (PythonTree child : node.getChildren()) {
                    child.setParent(node);
                }
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
            node.getInternalValue().setParent(node);
            if (node.getInternalAlphHole()!=null)
                node.getInternalAlphHole().setParent(node);
            if (node.getInternalHole()!=null)
                node.getInternalHole().setParent(node);
            return super.visitAttribute(node);
        }

        @Override
        public Object visitDictComp(DictComp node) throws Exception {
            updateParent(node);
            for (comprehension generator : node.getInternalGenerators()) {
                for (expr anIf : generator.getInternalIfs()) {
                    anIf.setParent(generator);
                }
                generator.getInternalIter().setParent(generator);
                generator.getInternalTarget().setParent(generator);
            }
            return super.visitDictComp(node);
        }


        @Override
        public Object visitListComp(ListComp node) throws Exception {
            updateParent(node);
            for (comprehension generator : node.getInternalGenerators()) {
                for (expr anIf : generator.getInternalIfs()) {
                    anIf.setParent(generator);
                }
                generator.getInternalIter().setParent(generator);
                if (generator.getInternalTarget()!=null)
                    generator.getInternalTarget().setParent(generator);
            }
            return super.visitListComp(node);
        }

        @Override
        public Object visitSetComp(SetComp node) throws Exception {
            updateParent(node);
            for (comprehension generator : node.getInternalGenerators()) {
                for (expr anIf : generator.getInternalIfs()) {
                    anIf.setParent(generator);
                }
                generator.getInternalIter().setParent(generator);
                generator.getInternalTarget().setParent(generator);
            }
            return super.visitSetComp(node);
        }

        @Override
        public Object visitGeneratorExp(GeneratorExp node) throws Exception {
            updateParent(node);
            for (comprehension generator : node.getInternalGenerators()) {
                for (expr anIf : generator.getInternalIfs()) {
                    anIf.setParent(generator);
                }
                generator.getInternalIter().setParent(generator);
                generator.getInternalTarget().setParent(generator);
            }
            return super.visitGeneratorExp(node);
        }
        @Override
        public Object visitSubscript(Subscript node) throws Exception {
            updateParent(node);
            node.getInternalValue().setParent(node);
            node.getInternalSlice().setParent(node);
            return super.visitSubscript(node);
        }

        @Override
        public Object visitIndex(Index node) throws Exception {
            updateParent(node);



            return super.visitIndex(node);
        }

        @Override
        public Object visitCall(Call node) throws Exception {
            updateParent(node);
            for (expr arg : node.getInternalArgs()) {
                arg.setParent(node);
            }
            node.getInternalFunc().setParent(node);
            return super.visitCall(node);
        }

        @Override
        public Object visitTryExcept(TryExcept node)  throws Exception{
            updateParent(node);
            for (excepthandler handler : node.getInternalHandlers()) {
                handler.setParent(node);
            }
            for (stmt stmt : node.getInternalBody()) {
                stmt.setParent(node);
            }

            return super.visitTryExcept(node);

        }

    }

}
