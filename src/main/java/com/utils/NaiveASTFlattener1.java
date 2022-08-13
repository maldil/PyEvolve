package com.utils;
import org.python.antlr.Visitor;
import org.python.antlr.ast.*;
import org.python.antlr.base.excepthandler;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;

import java.lang.Module;
import java.util.List;


public class NaiveASTFlattener1 extends Visitor{
    protected StringBuffer buffer = new StringBuffer();
    private int indent = 0;
    @Override
    public Object visitIfExp(IfExp node) throws Exception {
        printIndent();
        node.getInternalBody().accept(this);
        this.buffer.append(" if ");
        node.getInternalTest().accept(this);

        if (node.getInternalOrelse()!=null){
            this.buffer.append(" else ");
            node.getInternalOrelse().accept(this);
        }
        return null;
    }


    @Override
    public Object visitAssign(Assign node) throws Exception {
        for (expr target : node.getInternalTargets()) {
            target.accept(this);
        }
        this.buffer.append(" = ");
        node.getInternalValue().accept(this);
        return null;
    }

    @Override
    public Object visitCall(Call node)  throws Exception {
        node.getInternalFunc().accept(this);
        this.buffer.append("(");
        for (int i =0; i<node.getInternalArgs().size();i++){
            node.getInternalArgs().get(i).accept(this);
            if (i<node.getInternalArgs().size()-2){
                this.buffer.append(", ");
            }
        }
        return super.visitCall(node);
    }

    @Override
    public Object visitName(Name node)  throws Exception {
        this.buffer.append(node.getInternalId());
        return super.visitName(node);
    }

    @Override
    public Object visitNum(Num node)  throws Exception {
        this.buffer.append(node.getInternalN().toString());
        return super.visitNum(node);
    }

    @Override
    public Object visitAttribute(Attribute node)  throws Exception {
//        node.
        node.getInternalValue().accept(this);
        this.buffer.append(".");
        if (node.getInternalHole()!=null)
            node.getInternalHole().accept(this);
        else if (node.getInternalAlphHole()!=null){
            node.getInternalAlphHole().accept(this);
        }else{
            node.getInternalAttrName().accept(this);
        }

        return super.visitAttribute(node);
    }

    @Override
    public Object visitReturn(Return node)  throws Exception {
        this.buffer.append("return ");
        node.getInternalValue().accept(this);
        return null;
    }

    @Override
    public Object visitBinOp(BinOp node)  throws Exception {
        node.getInternalLeft().accept(this);
        String op = getOperatorType(node.getInternalOp());
        this.buffer.append(op);
        this.buffer.append("return ");
        node.getInternalRight().accept(this);
        return null;
    }

    private String getOperatorType(operatorType node) {
        String op = "";
        if (node.equals(operatorType.Div)){
            op = " / ";
        }
        else  if (node.equals(operatorType.Add)){
            op = " + ";
        }
        else  if (node.equals(operatorType.Sub)){
            op = " - ";
        }
        else  if (node.equals(operatorType.Mult)){
            op = " * ";
        }
        return op;
    }

    @Override
    public Object visitSubscript(Subscript node)  throws Exception {
        node.getInternalValue().accept(this);
        this.buffer.append("[");
        node.getInternalSlice().accept(this);
        this.buffer.append("]");
        return null;
    }

    @Override
    public Object visitIndex(Index node)  throws Exception {
        node.getInternalValue().accept(this);
        return null;
    }

    @Override
    public Object visitExpr(Expr node)  throws Exception {
        printIndent();
        node.getInternalValue().accept(this);
        return null;
    }

    @Override
    public Object visitImport(Import node)  throws Exception {
        this.buffer.append("import ");
        for (alias name : node.getInternalNames()) {
            this.buffer.append(name.getInternalName());
            if (name.getInternalAsname()!=null)
                this.buffer.append(" as ").append(name.getInternalAsname());
        }
        return null;
    }

    @Override
    public Object visitImportFrom(ImportFrom node)  throws Exception {
        this.buffer.append("from ");
        this.buffer.append(node.getInternalModule()).append(" ");
        for (alias name : node.getInternalNames()) {
            this.buffer.append("import ");
            this.buffer.append(name.getInternalName());
            if (name.getInternalAsname()!=null)
                this.buffer.append(" as ").append(name.getInternalAsname());
        }
        return null;
    }

    @Override
    public Object visitIf(If node) throws Exception {
        this.buffer.append("if ");
        node.getInternalTest().accept(this);
        this.buffer.append(":");
        this.buffer.append('\n');
        printBock(node.getInternalBody());
        if (node.getInternalOrelse().size()==0){
            printIndent();
            this.buffer.append("else:");
            this.buffer.append('\n');
            printBock(node.getInternalOrelse());
        }
        return null;
    }

    @Override
    public Object visitCompare(Compare node) throws Exception {
        node.getInternalLeft().accept(this);
        for (int i=0;i<node.getInternalComparators().size();i++){
            expr expr = node.getInternalComparators().get(i);
            cmpopType type = node.getInternalOps().get(i);
            if (type==cmpopType.In)
                this.buffer.append(" in ");
            else if (type==cmpopType.Gt)
                this.buffer.append(" > ");
            else if (type==cmpopType.GtE)
                this.buffer.append(" >= ");
            else if (type==cmpopType.LtE)
                this.buffer.append(" <= ");
            else if (type==cmpopType.Lt)
                this.buffer.append(" < ");
            else if (type==cmpopType.Eq)
                this.buffer.append(" == ");
            else if (type==cmpopType.Is)
                this.buffer.append(" is ");
            else if (type==cmpopType.IsNot)
                this.buffer.append(" is not ");
            else
                this.buffer.append(" error ");
            expr.accept(this);
        }
        return null;
    }

    @Override
    public Object visitFunctionDef(FunctionDef node)  throws Exception {
        printIndent();
        this.buffer.append("def ").append(node.getInternalName()).append("(");
        for (int i =0; i<node.getInternalArgs().getInternalArgs().size();i++){
            node.getInternalArgs().getInternalArgs().get(i).accept(this);
            arg arg = node.getInternalArgs().getInternalArgs().get(i);
            this.buffer.append(arg.getInternalArg());
            if (i<node.getInternalArgs().getInternalArgs().size()-2){
                this.buffer.append(", ");
            }
        }
        this.buffer.append("):");
        this.buffer.append('\n');
        printBock(node.getInternalBody());
        printIndent();
        return null;
    }

    @Override
    public Object visitFor(For node)  throws Exception {
        printIndent();
        this.buffer.append("for ");
        node.getInternalTarget().accept(this);
        this.buffer.append(" in ");
        node.getInternalIter().accept(this);
        printBock(node.getInternalBody());
        return null;
    }

    @Override
    public Object visitStr(Str node)  throws Exception {
        this.buffer.append(node.getInternalS().toString());
        return super.visitStr(node);
    }


    private void printBock(java.util.List<stmt> nodes) throws Exception {
        this.indent++;
        int i = 0;
        for (org.python.antlr.base.stmt stmt : nodes) {
            stmt.accept(this);
            i++;
            this.buffer.append('\n');
        }
        this.indent--;
        printIndent();
    }

    @Override
    public Object visitList(org.python.antlr.ast.List node)  throws Exception {
        this.buffer.append("[");
        for (int i=0; i<node.getInternalElts().size();i++){
            expr expr = node.getInternalElts().get(i);
            expr.accept(this);
            if (i<node.getInternalElts().size()-2)
                this.buffer.append(", ");
        }
        this.buffer.append("]");
        return null;
    }

    @Override
    public Object visitSet(org.python.antlr.ast.Set node)  throws Exception {
        this.buffer.append("{");
        for (int i=0; i<node.getInternalElts().size();i++){
            expr expr = node.getInternalElts().get(i);
            expr.accept(this);
            if (i<node.getInternalElts().size()-2)
                this.buffer.append(", ");
        }
        this.buffer.append("}");
        return null;
    }

    @Override
    public Object visitDict(org.python.antlr.ast.Dict node)  throws Exception {
        this.buffer.append("{");
        for (int i=0; i<node.getInternalKeys().size();i++){
            node.getInternalKeys().get(i).accept(this);
            this.buffer.append(":");
            node.getInternalValues().get(i).accept(this);
            if (i<node.getInternalKeys().size()-1)
                this.buffer.append(",");
        }
        this.buffer.append("}");
        return null;
    }

    @Override
    public Object visitTuple(Tuple node)  throws Exception {
        for (int i=0; i<node.getInternalElts().size();i++){
            expr expr = node.getInternalElts().get(i);
            expr.accept(this);
            if (i<node.getInternalElts().size()-2)
                this.buffer.append(", ");
        }
        return null;
    }

    @Override
    public Object visitAugAssign(AugAssign node)  throws Exception {
        node.getInternalTarget().accept(this);
        String op = getOperatorType(node.getInternalOp());
        this.buffer.append(op).append("=");
        node.getInternalValue().accept(this);
        return null;
    }

    @Override
    public Object visitBreak(Break node)  throws Exception {
        this.buffer.append("break");
        return null;
    }

    @Override
    public Object visitTryFinally(TryFinally node)  throws Exception {
        printIndent();
        this.buffer.append("try:\n");
        printBock(node.getInternalBody());
        printIndent();
        this.buffer.append("finally:\n");
        printBock(node.getInternalFinalbody());
        return null;
    }

    @Override
    public Object visitModule(org.python.antlr.ast.Module node)  throws Exception {
        for (stmt stmt : node.getInternalBody()) {
            stmt.accept(this);
            this.buffer.append('\n');
        }
        return null;
    }

    @Override
    public Object visitDelete(Delete node)  throws Exception {
        this.buffer.append("delete ");
        for (int i=0; i<node.getInternalTargets().size();i++){
            expr expr = node.getInternalTargets().get(i);
            expr.accept(this);
            if (i<node.getInternalTargets().size()-2)
                this.buffer.append(", ");
        }
        return null;
    }

    @Override
    public Object visitHole(Hole node)  throws Exception {
        this.buffer.append(":[");
        this.buffer.append(node.getInternalN());
        this.buffer.append("]");
        return null;
    }

    @Override
    public Object visitWith(With node)  throws Exception {
        this.buffer.append("with ");
        for (int i=0; i<node.getInternalItems().size();i++){
            node.getInternalItems().get(i).getInternalContext_expr().accept(this);
            this.buffer.append(" as ");
            node.getInternalItems().get(i).getInternalOptional_vars().accept(this);
            this.buffer.append(":\n");
            if (i<node.getInternalItems().size()-2)
                this.buffer.append(", ");
            printBock(node.getInternalBody());
        }
        return null;
    }

    @Override
    public Object visitListComp(ListComp node)  throws Exception {
        this.buffer.append("[");
        node.getInternalElt().accept(this);
        for (comprehension generator : node.getInternalGenerators()) {
            this.buffer.append(" for ");
            generator.getInternalTarget().accept(this);
            this.buffer.append(" in ");
            generator.getInternalIter().accept(this);
            for (expr anIf : generator.getInternalIfs()) {
                this.buffer.append(" if ");
                anIf.accept(this);
            }
        }
        this.buffer.append("]");
        return null;
    }

    @Override
    public Object visitSetComp(SetComp node)  throws Exception {
        this.buffer.append("{");
        node.getInternalElt().accept(this);
        for (comprehension generator : node.getInternalGenerators()) {
            this.buffer.append(" for ");
            generator.getInternalTarget().accept(this);
            this.buffer.append(" in ");
            generator.getInternalIter().accept(this);
            for (expr anIf : generator.getInternalIfs()) {
                this.buffer.append(" if ");
                anIf.accept(this);
            }
        }
        this.buffer.append("}");
        return null;
    }


    public Object visitDictComp(DictComp node)  throws Exception {
        this.buffer.append("{");
        node.getInternalKey().accept(this);
        this.buffer.append(":");
        node.getInternalValue().accept(this);
        for (comprehension generator : node.getInternalGenerators()) {
            this.buffer.append(" for ");
            generator.getInternalTarget().accept(this);
            this.buffer.append(" in ");
            generator.getInternalIter().accept(this);
            for (expr anIf : generator.getInternalIfs()) {
                this.buffer.append(" if ");
                anIf.accept(this);
            }
        }
        this.buffer.append("}");
        return null;
    }

    public Object visitGeneratorExp(GeneratorExp node)  throws Exception {
        this.buffer.append("(");
        node.getInternalElt().accept(this);
        for (comprehension generator : node.getInternalGenerators()) {
            this.buffer.append(" for ");
            generator.getInternalTarget().accept(this);
            this.buffer.append(" in ");
            generator.getInternalIter().accept(this);
            for (expr anIf : generator.getInternalIfs()) {
                this.buffer.append(" if ");
                anIf.accept(this);
            }
        }
        this.buffer.append(")");
        return null;
    }

    @Override
    public Object visitTryExcept(TryExcept node)  throws Exception {
        printIndent();
        this.buffer.append("try:\n");
        printBock(node.getInternalBody());
        this.buffer.append('\n');
        for (excepthandler handler : node.getInternalHandlers()) {
            if (handler instanceof ExceptHandler){
                this.buffer.append("except");
                ((ExceptHandler)handler).getInternalType().accept(this);
                this.buffer.append(":\n");
                printBock(((ExceptHandler) handler).getInternalBody());
            }
        }
        return null;

    }

    public String getResult() {
        return this.buffer.toString();
    }


    void printIndent() {
        for (int i = 0; i < this.indent; i++)
            this.buffer.append("  "); //$NON-NLS-1$
    }
}
