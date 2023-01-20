package com.matching.fgpdg;

import com.matching.fgpdg.nodes.PDGActionNode;
import com.matching.fgpdg.nodes.TypeInfo.TypeWrapper;
import com.matching.fgpdg.nodes.ast.AlphanumericHole;
import com.matching.fgpdg.nodes.ast.LazyHole;
import com.utils.Assertions;

import org.python.antlr.PythonTree;
import org.python.antlr.ast.*;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;

import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class PDGBuildingContext {
    private String filePath;
    private TypeWrapper typeWrapper;
    private FunctionDef method;
    private HashMap<String, String> fieldTypes = new HashMap<>();
    private Stack<HashMap<String, String>> localVariables = new Stack<>(), localVariableTypes = new Stack<>();
    private Stack<HashSet<PDGActionNode>> stkTrys = new Stack<>();
    private HashMap<String, String> importsMap = new HashMap<>();

    public HashMap<String, String> getImportsMap() {
        return importsMap;
    }

    public String getFilePath() {
        return filePath;
    }

    public PDGBuildingContext(List<stmt> importStmt, String sourceFilePath) throws IOException {
        typeWrapper = new TypeWrapper(Configurations.TYPE_REPOSITORY+
                sourceFilePath.split("/")[0]
                +"/"+
                sourceFilePath.split("/")[1]+"/"+
                Arrays.stream(sourceFilePath.split("/")).skip(2).collect(Collectors.joining("_")).split("\\.")[0]+".json" );
        this.filePath=sourceFilePath;
        updateImportMap(importStmt);
    }

    public PDGBuildingContext(List<stmt> importStmt){
        updateImportMap(importStmt);
    }

    public PDGBuildingContext(List<stmt> importStmt, TypeWrapper typeWrapper){
        this.typeWrapper=typeWrapper;
        updateImportMap(importStmt);
    }



    public TypeWrapper getTypeWrapper() {
        return typeWrapper;
    }

    private void updateImportMap(List<stmt> importStmt) {
        for (Object anImport : importStmt) {
            if (anImport instanceof Import){
                Import im = (Import) anImport;
                List<alias> internalNames = im.getInternalNames();
                for (alias internalName : internalNames) {
                    if (internalName.getInternalAsname()!=null){
                        importsMap.put(internalName.getInternalAsname(),internalName.getInternalName());
                    }
                    else{
                        String[] split = internalName.getInternalName().split("\\.");
                        importsMap.put(split[split.length-1],internalName.getInternalName());
                    }
                }
            }
            else if (anImport instanceof ImportFrom){
                ImportFrom im = (ImportFrom) anImport;
                String from = im.getInternalModule();
                List<alias> internalNames = im.getInternalNames();
                for (alias internalName : internalNames) {
                    if (internalName.getInternalAsname()!=null){
                        importsMap.put(internalName.getInternalAsname(),from+"."+internalName.getInternalName());
                    }
                    else{
                        String[] split = internalName.getInternalName().split("\\.");
                        importsMap.put(split[split.length-1],from+"."+internalName.getInternalName());
                    }
                }
            }
        }

        if (this.typeWrapper.getGuards()!=null){
            for (Map.Entry<String, String> entry : this.typeWrapper.getGuards().getImports().entrySet()) {
                importsMap.put(entry.getKey(),entry.getValue());
            }
        }


    }

    public String getFieldType(String name) {
        String type = this.fieldTypes.get(name);
        if (type == null) {
//			buildSuperFieldTypes();
            type = this.fieldTypes.get(name);
        }
        return type;
    }

    public void addScope() {
        this.localVariables.push(new HashMap<String, String>());
        this.localVariableTypes.push(new HashMap<String, String>());
    }

    public void removeScope() {
        this.localVariables.pop();
        this.localVariableTypes.pop();
    }

    public void setMethod(FunctionDef func){
        method=func;
    }

    public String[] getLocalVariableInfo(String identifier) {
        for (int i = localVariables.size() - 1; i >= 0; i--) {
            HashMap<String, String> variables = this.localVariables.get(i);
            if (variables.containsKey(identifier))
                return new String[]{variables.get(identifier), this.localVariableTypes.get(i).get(identifier)};
        }
        return null;
    }

    public void addLocalVariable(String identifier, String key, String type) {
        this.localVariables.peek().put(identifier, key);
        this.localVariableTypes.peek().put(identifier, type);
    }

    public void updateTypeOfVariable(String identifier,   String type){
        for (int i = localVariables.size() - 1; i >= 0; i--) {
            HashMap<String, String> variables = this.localVariables.get(i);
            if (variables.containsKey(identifier))
                 this.localVariableTypes.get(i).put(identifier,type);
        }
    }

    public void pushTry() {
        stkTrys.push(new HashSet<PDGActionNode>());
    }

    public HashSet<PDGActionNode> popTry() {
        return stkTrys.pop();
    }

    public String getKey(Subscript astNode) {
        String name=null;
        expr value = astNode.getInternalValue();
        if (value instanceof Subscript){
            name = getKey((Subscript) value);
        }
        else if (value instanceof Attribute){
            name = getFullNameOfAttribute((Attribute)value);
        }
        else if (value instanceof Name){
            name = ((Name) value).getInternalId();
            String[] info = getLocalVariableInfo(name);
            if (info != null)
                name = info[0];
            else
                name=getTypeWrapper().getTypeInfo(((Name) value).getLineno() ,((Name) value).getCol_offset(),((Name) value).getInternalId());
        }
        if (astNode.getInternalSlice() instanceof Index && ((Index)astNode.getInternalSlice()).getInternalValue() instanceof Tuple){
            Tuple internalValue = (Tuple)((Index) astNode.getInternalSlice()).getInternalValue();
            if (internalValue.getInternalElts().stream().allMatch(x->x instanceof Num)){
                name = "["+internalValue.getInternalElts().stream().map(x-> ((Num)x).getInternalN().toString()).collect(Collectors.joining(","))+"]";
            }
        }
        else if (astNode.getInternalSlice() instanceof Index && ((Index)astNode.getInternalSlice()).getInternalValue() instanceof Num){
            name= "[Num]";
        }

        return  name;


    }

    private String getFullNameOfAttribute(Attribute atr){
        if (atr.getInternalValue() instanceof Name)
            return ((Name) atr.getInternalValue()).getInternalId()+atr.getInternalAttr();
        else if (atr.getInternalValue() instanceof AlphanumericHole)
            return (atr.getInternalValue()).toString()+atr.getInternalAttr();
        else if (atr.getInternalValue() instanceof LazyHole)
            return (atr.getInternalValue()).toString()+atr.getInternalAttr();
        else if ( atr.getInternalValue() instanceof Subscript){
            return getFullNameOfAttribute((Subscript)atr.getInternalValue()) + "."+atr.getInternalAttr();
        }
        else if ( atr.getInternalValue() instanceof Attribute){
            return getFullNameOfAttribute((Attribute) atr.getInternalValue())+atr.getInternalAttr();
        }
        else if ( atr.getInternalValue() instanceof Call){
            return getFullNameOfAttribute((Call) atr.getInternalValue())+atr.getInternalAttr();
        }
        else{

            return "";
        }
    }


    private String getFullNameOfAttribute(Call node){
        if (node.getInternalFunc() instanceof Attribute){
            return getFullNameOfAttribute((Attribute)node.getInternalFunc())+"()";
        }
        else if (node.getInternalFunc() instanceof Name){
            return ((Name) node.getInternalFunc()).getInternalId()+"()";
        }
        else if (node.getInternalFunc() instanceof Subscript){
            return getFullNameOfAttribute((Subscript)node.getInternalFunc())+"()";
        }
        else if (node.getInternalFunc() instanceof Call){
            return getFullNameOfAttribute((Call)node.getInternalFunc())+"()";
        }
        else{
            return "";
        }


    }


    private String getFullNameOfAttribute(Subscript atr){
        if (atr.getInternalSlice() instanceof ExtSlice){
            return "::";
        }
        else if (atr.getInternalSlice() instanceof Slice){
            return ":";
        }
        else if (atr.getInternalValue() instanceof Name){
            if (((Index)atr.getInternalSlice()).getInternalValue() instanceof Num)
                return ((Name) atr.getInternalValue()).getInternalId()+ "["+((Num)((Index)atr.getInternalSlice()).getInternalValue()).getInternalN() +"]";
            else if (((Index)atr.getInternalSlice()).getInternalValue() instanceof Str)
                return ((Name) atr.getInternalValue()).getInternalId()+ "["+((Str)((Index)atr.getInternalSlice()).getInternalValue()).getInternalS() +"]";
            else if (((Index)atr.getInternalSlice()).getInternalValue() instanceof Name)
                return ((Name) atr.getInternalValue()).getInternalId()+ "["+((Name)((Index)atr.getInternalSlice()).getInternalValue()).getInternalId() +"]";
            else if (((Index)atr.getInternalSlice()).getInternalValue() instanceof BinOp)
                return ((Name) atr.getInternalValue()).getInternalId()+ "["+((BinOp)((Index)atr.getInternalSlice()).getInternalValue()).getInternalRight()
                        +((BinOp)((Index)atr.getInternalSlice()).getInternalValue()).getInternalOp().name() +
                        ((BinOp)((Index)atr.getInternalSlice()).getInternalValue()).getInternalLeft()+"]";
            else if (((Index)atr.getInternalSlice()).getInternalValue() instanceof org.python.antlr.ast.List){
                return ((Name) atr.getInternalValue()).getInternalId() +"[]";
            }
            else if (((Index)atr.getInternalSlice()).getInternalValue() instanceof Subscript){
                return ((Name) atr.getInternalValue()).getInternalId()+ "["+getFullNameOfAttribute((Subscript) ((Index)atr.getInternalSlice()).getInternalValue()) +"]";
            }
            else if (((Index)atr.getInternalSlice()).getInternalValue() instanceof Str){
                return ((Name) atr.getInternalValue()).getInternalId()+ "["+((Str)((Index)atr.getInternalSlice()).getInternalValue()).getInternalS() +"]";
            }
            else
                return ((Name) atr.getInternalValue()).getInternalId()+ "["  +"]";
        }
        else if (atr.getInternalValue() instanceof AlphanumericHole)
            return (atr.getInternalValue()).toString()+ "["+((Num)((Index)atr.getInternalSlice()).getInternalValue()).getInternalN()  +"]";
        else if (atr.getInternalValue() instanceof LazyHole)
            return (atr.getInternalValue()).toString()+ "["+((Num)((Index)atr.getInternalSlice()).getInternalValue()).getInternalN() +"]";
        else if ( atr.getInternalValue() instanceof Subscript){
            if (((Index)atr.getInternalSlice()).getInternalValue() instanceof Num)
                return getFullNameOfAttribute((Subscript)atr.getInternalValue()) + "["+((Num)((Index)atr.getInternalSlice()).getInternalValue()).getInternalN() +"]";
            else if (((Index)atr.getInternalSlice()).getInternalValue() instanceof Num)
                return getFullNameOfAttribute((Subscript)atr.getInternalValue()) + "["+((Name)((Index)atr.getInternalSlice()).getInternalValue()).getInternalId() +"]";
            else if (((Index)atr.getInternalSlice()).getInternalValue() instanceof Name){
                return getFullNameOfAttribute((Subscript)atr.getInternalValue()) + "["+((Name)((Index)atr.getInternalSlice()).getInternalValue()).getInternalId() +"]";
            }
            else if (((Index)atr.getInternalSlice()).getInternalValue() instanceof Attribute){
                return getFullNameOfAttribute((Subscript)atr.getInternalValue()) + "["+getFullNameOfAttribute(((Attribute)((Index)atr.getInternalSlice()).getInternalValue())) +"]";

            }
            else if (((Index)atr.getInternalSlice()).getInternalValue() instanceof Str)
                return getFullNameOfAttribute((Subscript)atr.getInternalValue()) + "["+((Str)((Index)atr.getInternalSlice()).getInternalValue()).getInternalS() +"]";
            else
                return getFullNameOfAttribute((Subscript)atr.getInternalValue()) + "["  +"]";
        }
        else if ( atr.getInternalValue() instanceof Attribute){
            if (((Index)atr.getInternalSlice()).getInternalValue() instanceof Num)
                return getFullNameOfAttribute((Attribute)atr.getInternalValue()) + "["+((Num)((Index)atr.getInternalSlice()).getInternalValue()).getInternalN() +"]";
            else if (((Index)atr.getInternalSlice()).getInternalValue() instanceof Name)
                return getFullNameOfAttribute((Attribute)atr.getInternalValue()) + "["+((Name)((Index)atr.getInternalSlice()).getInternalValue()).getInternalId() +"]";
            else if (((Index)atr.getInternalSlice()).getInternalValue() instanceof Attribute)
                return getFullNameOfAttribute((Attribute)atr.getInternalValue()) + "["+getFullNameOfAttribute((Attribute) ((Index)atr.getInternalSlice()).getInternalValue())+"]";
            else if (((Index)atr.getInternalSlice()).getInternalValue() instanceof Subscript){
                return getFullNameOfAttribute((Attribute)atr.getInternalValue()) + "["+getFullNameOfAttribute((Subscript)((Index)atr.getInternalSlice()).getInternalValue()) +"]";
            }
            else if (((Index)atr.getInternalSlice()).getInternalValue() instanceof Str)
                return getFullNameOfAttribute((Attribute)atr.getInternalValue()) + "["+((Str)((Index)atr.getInternalSlice()).getInternalValue()).getInternalS() +"]";
            else
                return getFullNameOfAttribute((Attribute)atr.getInternalValue()) + "["+  "]";
        }
        else if (atr.getInternalValue() instanceof Call) {
            if (((Call) atr.getInternalValue()).getInternalFunc() instanceof Name) {
                String fuName = ((Name) ((Call) atr.getInternalValue()).getInternalFunc()).getInternalId() + "()";
                if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Num)
                    return fuName + "[" + ((Num) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalN() + "]";
                else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Name)
                    return fuName + "[" + ((Name) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalId() + "]";
                else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Subscript)
                    return fuName + "[ ]";
                else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Str)
                    return fuName + "[" + ((Str) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalS() + "]";
                else
                    return fuName +"[ ]";
            }
            else{
                if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Num)
                    return getFullNameOfAttribute((Attribute) ((Call) atr.getInternalValue()).getInternalFunc())
                            + "[" + ((Num) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalN() + "]";
                else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Name)
                    return getFullNameOfAttribute((Attribute) ((Call) atr.getInternalValue()).getInternalFunc())
                            + "[" + ((Name) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalId() + "]";
                else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Subscript)
                    return getFullNameOfAttribute((Attribute) ((Call) atr.getInternalValue()).getInternalFunc())
                            + "[ ]";
                else if  (((Index) atr.getInternalSlice()).getInternalValue() instanceof Str)
                    return getFullNameOfAttribute((Attribute) ((Call) atr.getInternalValue()).getInternalFunc())
                            + "[" + ((Str) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalS() + "]";
                else{
                    return "";
                }
            }
        }
        else{
            Assertions.UNREACHABLE();
            return null;
        }
    }

    }
