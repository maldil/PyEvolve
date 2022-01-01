package com.matching.fgpdg.nodes.TypeInfo;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TypeWrapper {
    Table<Integer, Integer, String> typeTable
            = HashBasedTable.create();
    public TypeWrapper(String path) throws IOException {
        Gson gson = new Gson();
        String s = Files.readString(Path.of(path));
        if (s ==null) return;
        TypeASTNode[] astArray = gson.fromJson(s, TypeASTNode[].class);
        for(TypeASTNode node : astArray) {
            typeTable.put(node.getLineNumber(), node.getCol_offset(), node.getType());
        }
    }

    public String getTypeInfo(int lineNumber, int collomnOffset){
        if (typeTable.get(lineNumber,collomnOffset)==null)
            System.err.println("Types in "+lineNumber+" "+collomnOffset+"is not found");
        return typeTable.get(lineNumber,collomnOffset);
    }
}
