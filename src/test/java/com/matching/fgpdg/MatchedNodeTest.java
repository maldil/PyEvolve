package com.matching.fgpdg;

import com.matching.ConcreatePythonParser;
import org.junit.jupiter.api.Test;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.Module;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class MatchedNodeTest {
    @Test
    void getSubGraphs() {
        Module codeModule = getPythonModule("test1.py");
        Module patternModule = getPythonModule("pattern.py");
        FunctionDef func = (FunctionDef) codeModule.getInternalBody().get(2);
        PDGBuildingContext fcontext = null;
        try {
            fcontext = new PDGBuildingContext(codeModule.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "TEST_REPO/test1");
            PDGGraph fpdg = new PDGGraph(func,fcontext);

            PDGBuildingContext mcontext = new PDGBuildingContext(patternModule.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()),"TEST_REPO/pattern");
            PDGGraph mpdg = new PDGGraph(patternModule,mcontext);

            MatchPDG match = new MatchPDG();
            List<MatchedNode> graphs = match.getSubGraphs(mpdg,fpdg );
            match.drawMatchedGraphs(fpdg,graphs,"matched_graph.dot");
        } catch (IOException e) {
            System.err.println("Type information can not be performed");
            e.printStackTrace();
        }

    }

    private Module getPythonModule(String fileName){
        ConcreatePythonParser parser = new ConcreatePythonParser();
        return parser.parse(fileName);
    }
}