package com.matching.fgpdg;

import com.matching.ConcreatePythonParser;
import org.junit.jupiter.api.Test;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Module;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class MatchPDGTest {

    @Test
    void getSubGraphs() {
        Module codeModule = getPythonModule("test1.py");
        Module patternModule = getPythonModule("pattern.py");
        FunctionDef func = (FunctionDef) codeModule.getInternalBody().get(1);
        PDGBuildingContext fcontext = null;
        try {
            fcontext = new PDGBuildingContext(new ArrayList<>(),"");
            PDGGraph fpdg = new PDGGraph(func,fcontext);

            PDGBuildingContext mcontext = new PDGBuildingContext(new ArrayList<>(),"");
            PDGGraph mpdg = new PDGGraph(patternModule,mcontext);

            MatchPDG match = new MatchPDG();
            List<MatchedNode> graphs = match.getSubGraphs(mpdg,fpdg );
            match.drawMatchedGraphs(fpdg,graphs,"matched_graph.dot");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private Module getPythonModule(String fileName){
        ConcreatePythonParser parser = new ConcreatePythonParser();
        return parser.parse(fileName);
//        FunctionDef func = (FunctionDef) parse.getInternalBody().get(1);
//        PDGBuildingContext context = new PDGBuildingContext(new ArrayList<>(),"");
//        PDGGraph pdg = new PDGGraph(func,context);

    }

}