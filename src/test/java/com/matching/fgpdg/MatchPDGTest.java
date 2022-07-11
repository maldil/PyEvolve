package com.matching.fgpdg;

import com.matching.ConcreatePythonParser;
import com.utils.Utils;
import org.junit.jupiter.api.Test;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Module;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class MatchPDGTest {

    @Test
    void getSubGraphs() {
        Module codeModule = getPythonModule("author/project/test1.py");
        Module patternModule = getPythonModule("author/project/pattern.py");
        FunctionDef func = (FunctionDef) codeModule.getInternalBody().get(1);
        PDGBuildingContext fcontext = null;
        try {
            fcontext = new PDGBuildingContext(new ArrayList<>(),"author/project/test1.py");
            PDGGraph fpdg = new PDGGraph(func,fcontext);

            PDGBuildingContext mcontext = new PDGBuildingContext(new ArrayList<>(),"author/project/pattern.py");
            PDGGraph mpdg = new PDGGraph(patternModule,mcontext);

            MatchPDG match = new MatchPDG();
            List<MatchedNode> graphs = match.getSubGraphs(mpdg,fpdg,mcontext,fcontext );

            match.drawMatchedGraphs(fpdg,graphs,"OUTPUT/matches/text1.dot");
            Utils.markNodesInCode("src/test/resources/author/project/test1.py",graphs,"OUTPUT/matches/text1.html","","");
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