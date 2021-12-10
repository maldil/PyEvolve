package com.matching.fgpdg;

import com.matching.ConcreatePythonParser;
import org.junit.jupiter.api.Test;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Module;

import java.util.ArrayList;

class MatchPDGTest {

    @Test
    void getSubGraphs() {
        Module codeModule = getPythonModule("test1.py");
        Module patternModule = getPythonModule("pattern.py");
        FunctionDef func = (FunctionDef) codeModule.getInternalBody().get(1);
        PDGBuildingContext fcontext = new PDGBuildingContext(new ArrayList<>(),"");
        PDGGraph fpdg = new PDGGraph(func,fcontext);

        PDGBuildingContext mcontext = new PDGBuildingContext(new ArrayList<>(),"");
        PDGGraph mpdg = new PDGGraph(patternModule,mcontext);

        MatchPDG match = new MatchPDG();
        PDGGraph graphs = match.getSubGraphs(mpdg,fpdg );


    }

    private Module getPythonModule(String fileName){
        ConcreatePythonParser parser = new ConcreatePythonParser();
        return parser.parse(fileName);
//        FunctionDef func = (FunctionDef) parse.getInternalBody().get(1);
//        PDGBuildingContext context = new PDGBuildingContext(new ArrayList<>(),"");
//        PDGGraph pdg = new PDGGraph(func,context);

    }

}