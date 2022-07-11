package com.matching.fgpdg;

import com.google.gson.stream.JsonToken;
import com.matching.fgpdg.nodes.Guards;
import org.antlr.runtime.ANTLRStringStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.python.antlr.AnalyzingParser;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.*;
import org.python.antlr.ast.Module;
import org.python.antlr.base.expr;
import org.python.antlr.base.mod;
import org.python.antlr.base.stmt;
import org.python.core.PyObject;
import org.python.modules._socket._socket;

public class TestTemplateParsing {
    @Test
    void testPDG1() throws Exception {
        String code= """
                [[$1]] = [[$3]]
                for [[$5]] in [[$6]]:
                        [[%9]] += [[$11]]
                        [[$1]] += [[$5]]
                x = 3%4
                [[$15]]([[$1]])
                """;
        mod mod = parsePython(code);
        PyErrorVisitor error = new PyErrorVisitor();
        error.visit(mod);
        Assertions.assertEquals(error.getError(),0);
        Assertions.assertEquals(error.getHoles(),code.split("\\$").length -1);
    }

    @Test
    void testPDG2() throws Exception {
        String code= """
                # type [[$1]] : int
                # value [[$3]] : 0
                # type [[$4]] : str
                # kind [[$3]] : SimpleName
                [[$1]] = [[$3]]
                for [[$5]] in [[$6]]:
                        [[$9]] = [[$12]]([[$1]])
                        if not [[$9]]:
                                [[$1]] += [[$5]]
                [[$20]]([[$1]])""";
        mod mod = parsePython(code);
        Guards guard = new Guards(code,(Module) mod);
        PyErrorVisitor error = new PyErrorVisitor();
        error.visit(mod);
        Assertions.assertEquals(error.getError(),0);
        Assertions.assertEquals(error.getHoles(),code.split("\\$").length -1);
    }

    @Test
    void testPDG3() throws Exception {
        String code= """
                with [$3] as [[$1]]:
                    coo.boo.foo()
                    [[$13]] = [$16].[$19].[$18]([[$1]], [$21])
                    something()
                    """;

        mod mod = parsePython(code);
        PyErrorVisitor error = new PyErrorVisitor();
        error.visit(mod);
        Assertions.assertEquals(error.getError(),0);
        Assertions.assertEquals(error.getHoles(),code.split("\\$").length -1);
    }

    public static mod parsePython(String code) {
        ANTLRStringStream antlrSting = new ANTLRStringStream(code);
        AnalyzingParser p = new AnalyzingParser(antlrSting, "", "ascii");
        mod ast = p.parseModule();
        return ast;
    }




}

class PyErrorVisitor extends Visitor {
    private int error= 0;
    private int holes= 0;
    public int getError() {
        return error;
    }

    public int getHoles() {
        return holes;
    }




    @Override
    public Object visitExpr(Expr node) throws Exception {
        if (node.getValue() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitExpr(node);
    }

    @Override
    public Object unhandled_node(PythonTree node) throws Exception {

        return super.unhandled_node(node);
    }

    @Override
    public Object visitAssign(Assign node) throws Exception {
        if (node.getValue() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitAssign(node);
    }

    @Override
    public Object visitModule(Module node) throws Exception {

        return super.visitModule(node);
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
    public Object visitFunctionDef(FunctionDef node) throws Exception {

        return super.visitFunctionDef (node);
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
    public Object visitReturn(Return node) throws Exception {
        if (node.getValue() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitReturn (node);
    }

    @Override
    public Object visitDelete(Delete node) throws Exception {
        for (expr nd : node.getInternalTargets()) {
            if (nd instanceof ErrorExpr){
                error+=1;
            }
        }


        return super.visitDelete (node);
    }

    @Override
    public Object visitAugAssign(AugAssign node) throws Exception {
        if (node.getInternalTarget() instanceof ErrorExpr){
            error+=1;
        }
        if (node.getValue() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitAugAssign (node);
    }

    @Override
    public Object visitFor(For node) throws Exception {
        if (node.getInternalTarget() instanceof ErrorExpr){
            error+=1;
        }
        if (node.getInternalIter() instanceof ErrorExpr){
            error+=1;
        }

        for (stmt stmt : node.getInternalBody()) {
            if (stmt instanceof ErrorStmt){
                error+=1;
            }
        }

        return super.visitFor (node);
    }

    @Override
    public Object visitAsyncFor(AsyncFor node) throws Exception {
        if (node.getInternalTarget() instanceof ErrorExpr){
            error+=1;
        }
        if (node.getInternalIter() instanceof ErrorExpr){
            error+=1;
        }

        for (stmt stmt : node.getInternalBody()) {
            if (stmt instanceof ErrorStmt){
                error+=1;
            }
        }
        return super.visitAsyncFor (node);
    }

    @Override
    public Object visitWhile(While node) throws Exception {
        if (node.getInternalTest() instanceof ErrorExpr){
            error+=1;
        }

        for (stmt stmt : node.getInternalBody()) {
            if (stmt instanceof ErrorStmt){
                error+=1;
            }
        }

        return super.visitWhile (node);
    }

    @Override
    public Object visitIf(If node) throws Exception {
        if (node.getInternalTest() instanceof ErrorExpr){
            error+=1;
        }
        for (stmt stmt : node.getInternalBody()) {
            if (stmt instanceof ErrorStmt){
                error+=1;
            }
        }
        return super.visitIf (node);
    }

    @Override
    public Object visitWith(With node) throws Exception {
        for (stmt stmt : node.getInternalBody()) {
            if (stmt instanceof ErrorStmt){
                error+=1;
            }
        }
        return super.visitWith (node);
    }

    @Override
    public Object visitAsyncWith(AsyncWith node) throws Exception {
        for (stmt stmt : node.getInternalBody()) {
            if (stmt instanceof ErrorStmt){
                error+=1;
            }
        }
        return super.visitAsyncWith (node);
    }

    @Override
    public Object visitRaise(Raise node) throws Exception {
        if (node.getInternalCause() instanceof ErrorExpr){
            error+=1;
        }
        if (node.getInternalExc() instanceof ErrorExpr){
            error+=1;
        }

        return super.visitRaise (node);
    }

    @Override
    public Object visitTryExcept(TryExcept node) throws Exception {

        for (stmt stmt : node.getInternalBody()) {
            if (stmt instanceof ErrorStmt){
                error+=1;
            }
        }

        for (stmt stmt : node.getInternalOrelse()) {
            if (stmt instanceof ErrorStmt){
                error+=1;
            }
        }
        return super.visitTryExcept (node);
    }

    @Override
    public Object visitTryFinally(TryFinally node) throws Exception {
        for (stmt stmt : node.getInternalBody()) {
            if (stmt instanceof ErrorStmt){
                error+=1;
            }
        }
        for (stmt stmt : node.getInternalFinalbody()) {
            if (stmt instanceof ErrorStmt){
                error+=1;
            }
        }
        return super.visitTryFinally (node);
    }

    @Override
    public Object visitAssert(Assert node) throws Exception {
        if (node.getInternalTest() instanceof ErrorExpr){
            error+=1;
        }
        if (node.getInternalMsg() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitAssert (node);
    }

    @Override
    public Object visitImport(Import node) throws Exception {
        if (node.getInternalNames() instanceof ErrorExpr){
            error+=1;
        }

        return super.visitImport (node);
    }

    @Override
    public Object visitImportFrom(ImportFrom node) throws Exception {


        return super.visitImportFrom (node);
    }

    @Override
    public Object visitGlobal(Global node) throws Exception {
        if (node.getInternalNameNodes() instanceof ErrorExpr){
            error+=1;
        }

        return super. visitGlobal(node);
    }

    @Override
    public Object visitNonlocal(Nonlocal node) throws Exception {
        if (node.getInternalNameNodes() instanceof ErrorExpr){
            error+=1;
        }

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


        for (expr stmt : node.getInternalValues()) {
            if (stmt instanceof ErrorExpr){
                error+=1;
            }
        }
        return super.visitBoolOp (node);
    }

    @Override
    public Object visitBinOp(BinOp node) throws Exception {
        if (node.getInternalLeft() instanceof ErrorExpr){
            error+=1;
        }
        if (node.getInternalRight() instanceof ErrorExpr){
            error+=1;
        }

        return super.visitBinOp (node);
    }

    @Override
    public Object visitUnaryOp(UnaryOp node) throws Exception {
        if (node.getInternalOperand() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitUnaryOp (node);
    }

    @Override
    public Object visitLambda(Lambda node) throws Exception {
        if (node.getInternalBody() instanceof ErrorExpr){
            error+=1;
        }

        return super.visitLambda (node);
    }

    @Override
    public Object visitIfExp(IfExp node) throws Exception {
        if (node.getInternalBody() instanceof ErrorExpr){
            error+=1;
        }
        if (node.getInternalTest() instanceof ErrorExpr){
            error+=1;
        }
        if (node.getInternalOrelse() instanceof ErrorExpr){
            error+=1;
        }
        return super. visitIfExp(node);
    }

    @Override
    public Object visitDict(Dict node) throws Exception {
        for (expr stmt : node.getInternalValues()) {
            if (stmt instanceof ErrorExpr){
                error+=1;
            }
        }
        for (expr stmt : node.getInternalKeys()) {
            if (stmt instanceof ErrorExpr){
                error+=1;
            }
        }
        return super.visitDict (node);
    }

    @Override
    public Object visitSet(Set node) throws Exception {
        for (expr stmt : node.getInternalElts()) {
            if (stmt instanceof ErrorExpr){
                error+=1;
            }
        }
        return super.visitSet (node);
    }

    @Override
    public Object visitListComp(ListComp node) throws Exception {
        if (node.getInternalElt() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitListComp (node);
    }

    @Override
    public Object visitSetComp(SetComp node) throws Exception {
        if (node.getInternalElt() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitSetComp (node);
    }

    @Override
    public Object visitDictComp(DictComp node) throws Exception {
        if (node.getInternalKey() instanceof ErrorExpr){
            error+=1;
        }
        if (node.getInternalValue() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitDictComp (node);
    }


    @Override
    public Object visitGeneratorExp(GeneratorExp node) throws Exception {
        if (node.getInternalElt() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitGeneratorExp (node);
    }

    @Override
    public Object visitAwait(Await node) throws Exception {
        if (node.getInternalValue() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitAwait (node);
    }

    @Override
    public Object visitYield(Yield node) throws Exception {
        if (node.getInternalValue() instanceof ErrorExpr){
            error+=1;
        }

        return super.visitYield (node);
    }

    @Override
    public Object visitYieldFrom(YieldFrom node) throws Exception {
        if (node.getInternalValue() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitYieldFrom (node);
    }

    @Override
    public Object visitCompare(Compare node) throws Exception {
        if (node.getInternalLeft() instanceof ErrorExpr){
            error+=1;
        }
        for (expr nod : node.getInternalComparators()) {
            if (nod  instanceof ErrorExpr){
                error+=1;
            }
        }

        return super.visitCompare (node);
    }

    @Override
    public Object visitCall(Call node) throws Exception {
        if (node.getInternalFunc() instanceof ErrorExpr){
            error+=1;
        }
        for (expr arg : node.getInternalArgs()) {
            if (arg instanceof ErrorExpr){
                error+=1;
            }
        }
        return super.visitCall (node);
    }

    @Override
    public Object visitNum(Num node) throws Exception {
        if (node.getInternalN() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitNum (node);
    }

    @Override
    public Object visitHole(Hole node) throws Exception {
        holes+=1;
        if (node.getInternalN() instanceof ErrorExpr){
            error+=1;
        }
//        """with [$3] as [[$1]]:
//                    coo.boo.foo()
//                    [[$13]] = [$16].[$19].[$18]([[$1]], [$21])
//                    something()
//                    "";""";
        return super.visitHole (node);
    }

    @Override
    public Object visitStr(Str node) throws Exception {
        if (node.getInternalS() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitStr (node);
    }

    @Override
    public Object visitFormattedValue(FormattedValue node) throws Exception {
        if (node.getInternalFormat_spec() instanceof ErrorExpr){
            error+=1;
        }
        if (node.getInternalValue() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitFormattedValue (node);
    }

    @Override
    public Object visitJoinedStr(JoinedStr node) throws Exception {
        for (expr value : node.getInternalValues()) {
            if (value instanceof ErrorExpr){
                error+=1;
            }
        }
        return super.visitJoinedStr (node);
    }

    @Override
    public Object visitBytes(Bytes node) throws Exception {
        if (node.getDict() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitBytes (node);
    }

    @Override
    public Object visitNameConstant(NameConstant node) throws Exception {
        if (node.getValue() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitNameConstant (node);
    }

    @Override
    public Object visitEllipsis(Ellipsis node) throws Exception {
        if (node.getDict() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitEllipsis (node);
    }

    @Override
    public Object visitConstant(Constant node) throws Exception {
        if (node.getValue() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitConstant (node);
    }

    @Override
    public Object visitAttribute(Attribute node) throws Exception {
        if (node.getValue() instanceof ErrorExpr){
            error+=1;
        }
        if (node.getAttr() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitAttribute (node);
    }

    @Override
    public Object visitSubscript(Subscript node) throws Exception {
        if (node.getValue() instanceof ErrorExpr){
            error+=1;
        }
        if (node.getSlice() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitSubscript (node);
    }

    @Override
    public Object visitStarred(Starred node) throws Exception {
        if (node.getValue() instanceof ErrorExpr){
            error+=1;
        }
        if (node.getCtx() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitStarred (node);
    }

    @Override
    public Object visitName(Name node) throws Exception {
        if (node.getId() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitName (node);
    }

    @Override
    public Object visitList(List node) throws Exception {
        for (expr elt : node.getInternalElts()) {
            if (elt instanceof ErrorExpr){
                error+=1;
            }
        }

        if (node.getCtx() instanceof ErrorExpr){
            error+=1;
        }

        return super.visitList (node);
    }

    @Override
    public Object visitTuple(Tuple node) throws Exception {
        for (expr expr : node.getInternalElts()) {
            if (expr instanceof ErrorExpr){
                error+=1;
            }
        }

        if (node.getCtx() instanceof ErrorExpr){
            error+=1;
        }

        return super.visitTuple (node);
    }

    @Override
    public Object visitSlice(Slice node) throws Exception {
        if (node.getLower() instanceof ErrorExpr){
            error+=1;
        }
        if (node.getStep() instanceof ErrorExpr){
            error+=1;
        }
        if (node.getUpper() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitSlice (node);
    }

    @Override
    public Object visitExtSlice(ExtSlice node) throws Exception {
        if (node.getDims() instanceof ErrorExpr){
            error+=1;
        }
        if (node.getDict() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitExtSlice (node);
    }

    @Override
    public Object visitIndex(Index node) throws Exception {
        if (node.getValue() instanceof ErrorExpr){
            error+=1;
        }
        if (node.getDict() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitIndex (node);
    }

    @Override
    public Object visitExceptHandler(ExceptHandler node) throws Exception {
        if (node.getBody() instanceof ErrorExpr){
            error+=1;
        }
        if (node.getName() instanceof ErrorExpr){
            error+=1;
        }

        if (node.getDict() instanceof ErrorExpr){
            error+=1;
        }
        return super.visitExceptHandler (node);
    }

}




