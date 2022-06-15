package com.matching.fgpdg.nodes.TypeInfo;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.Gson;
import com.ibm.wala.util.collections.Pair;
import com.matching.fgpdg.nodes.Guards;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TypeWrapper {
    Table<Integer, Integer, DataTypeClass> typeTable
            = HashBasedTable.create();
    Table<Integer, Integer, DataTypeClass> valueTable
            = HashBasedTable.create();
    Table<Integer, Integer, DataTypeClass> kindsTable
            = HashBasedTable.create();
    Table<Integer, Integer, DataTypeClass> importTable
            = HashBasedTable.create();
    Guards guards;
    public TypeWrapper(String path) throws IOException {
        Gson gson = new Gson();
        String s = Files.readString(Path.of(path));
        if (s ==null) return;
        TypeASTNode[] astArray = gson.fromJson(s, TypeASTNode[].class);
        for(TypeASTNode node : astArray) {
            typeTable.put(node.getLineNumber(), node.getCol_offset(),
                    new DataTypeClass(node.getNodeName(),node.getType()));
        }
    }

    public TypeWrapper(HashMap<String,String> typeInfo){
        Table<Integer, Integer, DataTypeClass> typeTable
                = HashBasedTable.create();
        for (Map.Entry<String, String> typeEntry : typeInfo.entrySet()) {
            typeTable.put(0, 0,
                    new DataTypeClass(typeEntry.getKey(),typeEntry.getValue()));
        }
        this.typeTable = typeTable;
    }

    public TypeWrapper(Guards guard){
        for (Map.Entry<String, Pair<Pair<Integer, Integer>, String>> typeEntry : guard.getTypes().entrySet()) {
            this.typeTable.put(typeEntry.getValue().fst.fst, typeEntry.getValue().fst.snd,
                    new DataTypeClass(typeEntry.getKey(),typeEntry.getValue().snd));
        }
        for (Map.Entry<String, String> typeEntry : guard.getValues().entrySet()) {
            this.valueTable.put(0, 0,
                    new DataTypeClass(typeEntry.getKey(),typeEntry.getValue()));
        }
        for (Map.Entry<String, String> typeEntry : guard.getKinds().entrySet()) {
            this.kindsTable.put(0, 0,
                    new DataTypeClass(typeEntry.getKey(),typeEntry.getValue()));
        }
        this.guards=guard;
    }



    public String getTypeInfo(int lineNumber, int collomnOffset){
        if (typeTable.get(lineNumber,collomnOffset)==null){
            System.err.println("Types in "+lineNumber+" "+collomnOffset+"is not found");

            return null;
        }
        return typeTable.get(lineNumber,collomnOffset).type;
    }

    public String getTypeInfo(int lineNumber, int collomnOffset, String name){
        if (typeTable.get(lineNumber,collomnOffset)==null){
//            System.err.println("Types in "+lineNumber+" "+collomnOffset+"is not found");
            return getTypeInfo(name);

        }
        return typeTable.get(lineNumber,collomnOffset).type;
    }

    public String getTypeInfo(String nodeName){
        for (Table.Cell<Integer, Integer, DataTypeClass> cell : typeTable.cellSet()) {
            if (cell.getValue().nodeName.equals(nodeName)){
                return cell.getValue().type;
            }
        }
        return null;
    }

    class DataTypeClass{
        String nodeName;
        String type;
        public DataTypeClass(String nodeName, String type) {
            this.nodeName = nodeName;
            this.type = type;
        }
    }

    public void setTypeTable(HashMap<String,String> typeInfo) {
        Table<Integer, Integer, DataTypeClass> typeTable
                = HashBasedTable.create();
        for (Map.Entry<String, String> typeEntry : typeInfo.entrySet()) {
            typeTable.put(0, 0,
                    new DataTypeClass(typeEntry.getKey(),typeEntry.getValue()));
        }
        this.typeTable = typeTable;
    }

    public Guards getGuards() {
        return guards;
    }
}
