package com.matching;

import com.utils.Assertions;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CharStream;
import org.python.antlr.AnalyzingParser;
import org.python.antlr.ast.Module;

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
            return (Module)parser.parseModule();
        } catch (IOException e) {
            Assertions.UNREACHABLE();
            return null;
        }
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

}
