package com.matching.fgpdg;

import com.matching.ConcreatePythonParser;
import com.utils.DotGraph;
import org.junit.jupiter.api.Test;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Module;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PDGGraphTest {
    @Test
    void testPDG1() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("test1.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(1);
        PDGBuildingContext context = new PDGBuildingContext(new ArrayList<>(),"");
        PDGGraph pdg = new PDGGraph(func,context);
        DotGraph dg = new DotGraph(pdg);
        String dirPath = "./OUTPUT/";
        dg.toDotFile(new File(dirPath  +"file___"+".dot"));
    }

    @Test
    void testPDG2() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("pattern.py");
        PDGBuildingContext context = new PDGBuildingContext(new ArrayList<>(),"");
        PDGGraph pdg = new PDGGraph(parse,context);
        DotGraph dg = new DotGraph(pdg);
        String dirPath = "./OUTPUT/";
        dg.toDotFile(new File(dirPath  +"__pattern__file___"+".dot"));
    }
}