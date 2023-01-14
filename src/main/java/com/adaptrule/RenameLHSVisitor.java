package com.adaptrule;

import com.matching.fgpdg.nodes.ast.LazyHole;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.*;
import org.python.antlr.ast.Module;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;
import org.python.core.PyObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RenameLHSVisitor extends Visitor {
    Map<PythonTree,List<PythonTree>> codeAndPara;
    Map<PythonTree,PythonTree> holeAndCode = new HashMap<>();
    Map<PythonTree, Hole> nameToHole = new HashMap<>();
    int largestHoleID=0;

    @Override
    public void preVisit(PyObject node) {

    }

    @Override
    public void postVisit(PyObject node) {

    }
    @Override
    public Object visitAssign(Assign node)  throws Exception {
        for (Map.Entry<PythonTree, List<PythonTree>> entry : codeAndPara.entrySet()) {
            PythonTree child = entry.getKey();
            List<PythonTree> replacement = entry.getValue();
            if (node.getInternalValue().equals(child)){
                node.setValue(replacement.get(0));
                replacement.get(0).setMyLineNumber(child.getLine());
            }else if (node.getInternalTargets().contains(child)){
                int childIndex= node.getInternalTargets().indexOf(child);
                node.getInternalTargets().remove(child);
                node.getInternalTargets().add(childIndex, (expr) replacement.get(0));
                replacement.get(0).setMyLineNumber(child.getLine());
            }
            if(node.getChildren().contains(child)){

                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                int childIndex= node.getChildren().indexOf(child);
                node.getChildren().remove(child);
                node.getChildren().add(childIndex,replacement.get(0));
            }
        }
        return super.visitAssign(node);
    }

    @Override
    public Object visitCall(Call node)  throws Exception {
        for (Map.Entry<PythonTree, List<PythonTree>> entry : codeAndPara.entrySet()) {
            PythonTree child = entry.getKey();
            List<PythonTree> replacement = entry.getValue();
            if (node.getInternalArgs().contains(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                int childIndex= node.getInternalArgs().indexOf(child);
                node.getInternalArgs().remove(child);
                node.getInternalArgs().add(childIndex,(expr)replacement.get(0));

            }else if (node.getInternalFunc().equals(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                node.setFunc(replacement.get(0));
            }
            if(node.getChildren().contains(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                int childIndex= node.getChildren().indexOf(child);
                node.getChildren().remove(child);
                node.getChildren().add(childIndex,replacement.get(0));
            }
        }
        return super.visitCall(node);
    }

    @Override
    public Object visitListComp(ListComp node)  throws Exception {
        for (Map.Entry<PythonTree, List<PythonTree>> entry : codeAndPara.entrySet()) {
            PythonTree child = entry.getKey();
            List<PythonTree> replacement = entry.getValue();
            if (node.getInternalElt().equals(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                node.setElt(replacement.get(0));
            }
            for (comprehension generator : node.getInternalGenerators()) {
                if (generator.getInternalIter().equals(child)){
                    replacement.get(0).setMyLineNumber(child.getLine());
                    generator.setIter(replacement.get(0));
                }
                else if (generator.getInternalTarget().equals(child)){
                    replacement.get(0).setMyLineNumber(child.getLine());
                    generator.setTarget(replacement.get(0));
                }
                if(generator.getChildren().contains(child)){
                    replacement.get(0).setMyLineNumber(child.getLine());
                    if (child instanceof Name && replacement.get(0) instanceof Hole){
                        nameToHole.put((Name) child,(Hole)replacement.get(0));
                    }
                    else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                        nameToHole.put((Name) child,(Hole)replacement.get(0));
                    }
                    int childIndex= generator.getChildren().indexOf(child);
                    generator.getChildren().remove(child);
                    generator.getChildren().add(childIndex,replacement.get(0));
                }

            }

            if(node.getChildren().contains(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                int childIndex= node.getChildren().indexOf(child);
                node.getChildren().remove(child);
                node.getChildren().add(childIndex,replacement.get(0));
            }
        }

        return super.visitListComp(node);
    }




    @Override
    public Object visitFor(For node)  throws Exception {
        for (Map.Entry<PythonTree, List<PythonTree>> entry : codeAndPara.entrySet()) {
            PythonTree child = entry.getKey();
            List<PythonTree> replacement = entry.getValue();
            if (node.getInternalIter().equals(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                node.setIter(replacement.get(0));
            }else if (node.getInternalTarget().equals(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                node.setTarget(replacement.get(0));
            }
            else if (node.getInternalBody().contains(child)){
                int childIndex= node.getInternalBody().indexOf(child);
                node.getInternalBody().remove(child);
                Expr expr = new Expr();
                expr.setValue(replacement.get(0));
                node.getInternalBody().add(childIndex,expr);
            }
            if(node.getChildren().contains(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                int childIndex= node.getChildren().indexOf(child);
                node.getChildren().remove(child);
                node.getChildren().add(childIndex,replacement.get(0));
            }
        }
        return super.visitFor(node);
    }

    @Override
    public Object visitReturn(Return node)  throws Exception {
        for (Map.Entry<PythonTree, List<PythonTree>> entry : codeAndPara.entrySet()) {
            PythonTree child = entry.getKey();
            List<PythonTree> replacement = entry.getValue();
            if (node.getInternalValue().equals(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                node.setValue(replacement.get(0));
            }
            if(node.getChildren().contains(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                int childIndex= node.getChildren().indexOf(child);
                node.getChildren().remove(child);
                node.getChildren().add(childIndex,replacement.get(0));
            }
        }
        return super.visitReturn(node);

    }

    @Override
    public Object visitModule(Module node)  throws Exception {
        for (Map.Entry<PythonTree, List<PythonTree>> entry : codeAndPara.entrySet()) {
            PythonTree child = entry.getKey();
            List<PythonTree> replacement = entry.getValue();
            if(node.getInternalBody().contains(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                int childIndex= node.getInternalBody().indexOf(child);
                node.getInternalBody().remove(child);
                Expr e = new Expr();
                e.setValue(replacement.get(0));
                node.getInternalBody().add(childIndex, e);
            }
            if(node.getChildren().contains(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));

                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                int childIndex= node.getChildren().indexOf(child);
                node.getChildren().remove(child);
                node.getChildren().add(childIndex,replacement.get(0));
            }
        }
        return super.visitModule(node);
    }

    @Override
    public Object visitFunctionDef(FunctionDef node)  throws Exception {
        for (Map.Entry<PythonTree, List<PythonTree>> entry : codeAndPara.entrySet()) {
            PythonTree child = entry.getKey();
            List<PythonTree> replacement = entry.getValue();
            if(node.getInternalBody().contains(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                int childIndex= node.getInternalBody().indexOf(child);
                node.getInternalBody().remove(child);
                if (replacement.get(0) instanceof stmt){
                    node.getInternalBody().add(childIndex, (stmt) replacement.get(0));
                }
                else{
                    Expr e = new Expr();
                    e.setValue(replacement.get(0));
                    e.setParent(node);
                    node.getInternalBody().add(childIndex, e);
                }
            }
            if(node.getChildren().contains(child)){
                replacement.get(0).setCharStartIndex(child.getCharStartIndex());
                replacement.get(0).setCharStopIndex(child.getCharStopIndex());
                replacement.get(0).setChildIndex(child.getChildIndex());
                replacement.get(0).setTokenStartIndex(child.getTokenStartIndex());
                replacement.get(0).setTokenStopIndex(child.getTokenStopIndex());
                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                HoleSearcher searcher = new HoleSearcher();
                searcher.visit(replacement.get(0));
                for (expr hole : searcher.getHoles()) {
                    if (holeAndCode.containsKey(hole)){
                        nameToHole.put(holeAndCode.get(hole), (Hole) hole);
                    }
                }
                int childIndex= node.getChildren().indexOf(child);
                node.getChildren().remove(child);
                node.getChildren().add(childIndex,replacement.get(0));
            }
        }
        return super.visitFunctionDef(node);
    }

    @Override
    public Object visitAttribute(Attribute node) throws Exception {
        for (Map.Entry<PythonTree, List<PythonTree>> entry : codeAndPara.entrySet()) {
            PythonTree child = entry.getKey();
            List<PythonTree> replacement = entry.getValue();
            if(node.getInternalValue()==child){
                replacement.get(0).setMyLineNumber(child.getLine());
                if (child instanceof Name && replacement.get(0) instanceof Hole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                else if (child instanceof Name && replacement.get(0) instanceof LazyHole){
                    nameToHole.put((Name) child,(Hole)replacement.get(0));
                }
                node.setValue(replacement.get(0));
            }
            if(node.getChildren().contains(child)){
                replacement.get(0).setMyLineNumber(child.getLine());
                int childIndex= node.getChildren().indexOf(child);
                node.getChildren().remove(child);
                node.getChildren().add(childIndex,replacement.get(0));
            }
        }
        return super.visitAttribute(node);
    }

    @Override
    public Object visitExpr(Expr node) throws Exception {


        return super.visitExpr (node);
    }

    @Override
    public Object visitInteractive(Interactive node) throws Exception {


        return super.visitInteractive(node);
    }

    @Override
    public Object visitExpression(Expression node) throws Exception {

        return super.visitExpression (node);
    }

    @Override
    public Object visitSuite(Suite node) throws Exception {

        return super.visitSuite (node);
    }


    @Override
    public Object visitAsyncFunctionDef(AsyncFunctionDef node) throws Exception {

        return super.visitAsyncFunctionDef (node);
    }

    @Override
    public Object visitClassDef(ClassDef node) throws Exception {

        return super.visitClassDef (node);
    }

    @Override
    public Object visitDelete(Delete node) throws Exception {

        return super.visitDelete (node);
    }

    @Override
    public Object visitAugAssign(AugAssign node) throws Exception {

        return super.visitAugAssign (node);
    }


    @Override
    public Object visitAsyncFor(AsyncFor node) throws Exception {

        return super.visitAsyncFor (node);
    }

    @Override
    public Object visitWhile(While node) throws Exception {

        return super.visitWhile (node);
    }

    @Override
    public Object visitIf(If node) throws Exception {

        return super.visitIf (node);
    }

    @Override
    public Object visitWith(With node) throws Exception {

        return super.visitWith (node);
    }

    @Override
    public Object visitAsyncWith(AsyncWith node) throws Exception {

        return super.visitAsyncWith (node);
    }

    @Override
    public Object visitRaise(Raise node) throws Exception {


        return super.visitRaise (node);
    }

    @Override
    public Object visitTryExcept(TryExcept node) throws Exception {


        return super.visitTryExcept (node);
    }

    @Override
    public Object visitTryFinally(TryFinally node) throws Exception {

        return super.visitTryFinally (node);
    }

    @Override
    public Object visitAssert(Assert node) throws Exception {

        return super.visitAssert (node);
    }

    @Override
    public Object visitImport(Import node) throws Exception {

        return super.visitImport (node);
    }

    @Override
    public Object visitImportFrom(ImportFrom node) throws Exception {


        return super.visitImportFrom (node);
    }

    @Override
    public Object visitGlobal(Global node) throws Exception {


        return super. visitGlobal(node);
    }

    @Override
    public Object visitNonlocal(Nonlocal node) throws Exception {


        return super.visitNonlocal (node);
    }


    @Override
    public Object visitPass(Pass node) throws Exception {

        return super.visitPass (node);
    }

    @Override
    public Object visitBreak(Break node) throws Exception {

        return super.visitBreak (node);
    }

    @Override
    public Object visitContinue(Continue node) throws Exception {

        return super.visitContinue (node);
    }

    @Override
    public Object visitBoolOp(BoolOp node) throws Exception {



        return super.visitBoolOp (node);
    }

    @Override
    public Object visitBinOp(BinOp node) throws Exception {


        return super.visitBinOp (node);
    }

    @Override
    public Object visitUnaryOp(UnaryOp node) throws Exception {

        return super.visitUnaryOp (node);
    }

    @Override
    public Object visitLambda(Lambda node) throws Exception {


        return super.visitLambda (node);
    }

    @Override
    public Object visitIfExp(IfExp node) throws Exception {

        return super. visitIfExp(node);
    }

    @Override
    public Object visitDict(Dict node) throws Exception {

        return super.visitDict (node);
    }

    @Override
    public Object visitSet(Set node) throws Exception {

        return super.visitSet (node);
    }


    @Override
    public Object visitSetComp(SetComp node) throws Exception {

        return super.visitSetComp (node);
    }

    @Override
    public Object visitDictComp(DictComp node) throws Exception {

        return super.visitDictComp (node);
    }


    @Override
    public Object visitGeneratorExp(GeneratorExp node) throws Exception {

        return super.visitGeneratorExp (node);
    }

    @Override
    public Object visitAwait(Await node) throws Exception {

        return super.visitAwait (node);
    }

    @Override
    public Object visitYield(Yield node) throws Exception {


        return super.visitYield (node);
    }

    @Override
    public Object visitYieldFrom(YieldFrom node) throws Exception {

        return super.visitYieldFrom (node);
    }

    @Override
    public Object visitCompare(Compare node) throws Exception {

        return super.visitCompare (node);
    }



    @Override
    public Object visitNum(Num node) throws Exception {

        return super.visitNum (node);
    }

    @Override
    public Object visitHole(Hole node) throws Exception {

        return super.visitHole (node);
    }

    @Override
    public Object visitStr(Str node) throws Exception {

        return super.visitStr (node);
    }

    @Override
    public Object visitFormattedValue(FormattedValue node) throws Exception {

        return super.visitFormattedValue (node);
    }

    @Override
    public Object visitJoinedStr(JoinedStr node) throws Exception {

        return super.visitJoinedStr (node);
    }

    @Override
    public Object visitBytes(Bytes node) throws Exception {

        return super.visitBytes (node);
    }

    @Override
    public Object visitNameConstant(NameConstant node) throws Exception {

        return super.visitNameConstant (node);
    }

    @Override
    public Object visitEllipsis(Ellipsis node) throws Exception {

        return super.visitEllipsis (node);
    }

    @Override
    public Object visitConstant(Constant node) throws Exception {

        return super.visitConstant (node);
    }



    @Override
    public Object visitSubscript(Subscript node) throws Exception {

        return super.visitSubscript (node);
    }

    @Override
    public Object visitStarred(Starred node) throws Exception {


        return super.visitStarred (node);
    }

    @Override
    public Object visitName(Name node) throws Exception {


        return super.visitName (node);
    }

    @Override
    public Object visitTuple(Tuple node) throws Exception {




        return super.visitTuple (node);
    }

    @Override
    public Object visitSlice(Slice node) throws Exception {

        return super.visitSlice (node);
    }

    @Override
    public Object visitExtSlice(ExtSlice node) throws Exception {

        return super.visitExtSlice (node);
    }

    @Override
    public Object visitIndex(Index node) throws Exception {

        return super.visitIndex (node);
    }

    @Override
    public Object visitExceptHandler(ExceptHandler node) throws Exception {

        return super.visitExceptHandler (node);
    }



    public Map<PythonTree, Hole> getNameToHole() {
        return nameToHole;
    }

    public Map<PythonTree,List<PythonTree>> getRenameChild() {
        return codeAndPara;
    }

    public void setRenameChild(Map<PythonTree,List<PythonTree>> renameChild) {
        this.codeAndPara = renameChild;
        for (Map.Entry<PythonTree, List<PythonTree>> entry : renameChild.entrySet()) {
            if(!codeAndPara.containsKey(entry.getValue().get(0))){
                holeAndCode.put(entry.getValue().get(0),entry.getKey());
            }
        }
    }

    public int getLargestHoleID() {
        return largestHoleID;
    }
}
