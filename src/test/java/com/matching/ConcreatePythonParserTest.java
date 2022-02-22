package com.matching;


import org.junit.jupiter.api.Test;
import org.python.antlr.ast.Module;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConcreatePythonParserTest {
    @Test
    void parse() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test1.py");
        System.out.println(parse.toStringTree());

        assertEquals(3,parse.getChildCount());
    }

    @Test
    void parseCode() {
        String code = "import numpy as np \nx=True";
        InputStream codeStream = new ByteArrayInputStream(code.getBytes());
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse(codeStream);
        assertEquals(2,parse.getChildCount());
    }
}