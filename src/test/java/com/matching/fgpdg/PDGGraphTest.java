package com.matching.fgpdg;

import com.matching.ConcreatePythonParser;
import com.utils.DotGraph;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.Module;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

class PDGGraphTest {
    @Test
    void testPattern() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/pattern.py");
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(new ArrayList<>(),"author/project/pattern.py");
            PDGGraph pdg = new PDGGraph(parse,context);

            MatchPDG mpdg = new    MatchPDG();
            PDGGraph _pattern= mpdg.pruneAndCleanPatternPDG(pdg);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"__pattern__file___"+".dot"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG1() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test1.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(1);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test1.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,2);
            Assertions.assertEquals (pdg.getNodes().size(),48);
            Assertions.assertEquals (pdg.statementNodes.size() ,19);
            Assertions.assertEquals (pdg.dataSources.size() ,10);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG2() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test2.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test2.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,2);
            Assertions.assertEquals (pdg.getNodes().size(),31);
            Assertions.assertEquals (pdg.statementNodes.size() ,12);
            Assertions.assertEquals (pdg.dataSources.size() ,9);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG3() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test3.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test3.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,2);
            Assertions.assertEquals (pdg.getNodes().size(),34);
            Assertions.assertEquals (pdg.statementNodes.size() ,13);
            Assertions.assertEquals (pdg.dataSources.size() ,10);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG4() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test4.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test4.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,2);
            Assertions.assertEquals (pdg.getNodes().size(),44);
            Assertions.assertEquals (pdg.statementNodes.size() ,18);
            Assertions.assertEquals (pdg.dataSources.size() ,14);
            //TODO Tuples do not engage with other elements-FIX IT
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG5() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test5.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test5.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,2);
            Assertions.assertEquals (pdg.getNodes().size(),18);
            Assertions.assertEquals (pdg.statementNodes.size() ,7);
            Assertions.assertEquals (pdg.dataSources.size() ,5);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG6() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test6.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test6.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,2);
            Assertions.assertEquals (pdg.getNodes().size(),37);
            Assertions.assertEquals (pdg.statementNodes.size() ,19);
            Assertions.assertEquals (pdg.dataSources.size() ,10);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG7() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test7.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test7.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,2);
            Assertions.assertEquals (pdg.getNodes().size(),25);
            Assertions.assertEquals (pdg.statementNodes.size() ,10);
            Assertions.assertEquals (pdg.dataSources.size() ,6);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG8() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test8.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test8.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,2);
            Assertions.assertEquals (pdg.getNodes().size(),26);
            Assertions.assertEquals (pdg.statementNodes.size() ,10);
            Assertions.assertEquals (pdg.dataSources.size() ,9);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG9() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test9.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test9.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,2);
            Assertions.assertEquals (pdg.getNodes().size(),31);
            Assertions.assertEquals (pdg.statementNodes.size() ,12);
            Assertions.assertEquals (pdg.dataSources.size() ,7);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG10() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test10.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test10.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,2);
            Assertions.assertEquals (pdg.getNodes().size(),37);
            Assertions.assertEquals (pdg.statementNodes.size() ,12);
            Assertions.assertEquals (pdg.dataSources.size() ,10);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG11() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test11.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test11.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,2);
            Assertions.assertEquals (pdg.getNodes().size(),47);
            Assertions.assertEquals (pdg.statementNodes.size() ,17);
            Assertions.assertEquals (pdg.dataSources.size() ,12);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG12() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test12.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test12.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,2);
            Assertions.assertEquals (pdg.getNodes().size(),34);
            Assertions.assertEquals (pdg.statementNodes.size() ,13);
            Assertions.assertEquals (pdg.dataSources.size() ,10);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG13() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test13.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test13.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,2);
            Assertions.assertEquals (pdg.getNodes().size(),34);
            Assertions.assertEquals (pdg.statementNodes.size() ,13);
            Assertions.assertEquals (pdg.dataSources.size() ,10);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG14() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test14.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test14.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,2);
            Assertions.assertEquals (pdg.getNodes().size(),34);
            Assertions.assertEquals (pdg.statementNodes.size() ,13);
            Assertions.assertEquals (pdg.dataSources.size() ,10);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG15() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test15.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test15.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,2);
            Assertions.assertEquals (pdg.getNodes().size(),33);
            Assertions.assertEquals (pdg.statementNodes.size() ,11);
            Assertions.assertEquals (pdg.dataSources.size() ,13);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG16() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test16.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test16.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,2);
            Assertions.assertEquals (pdg.getNodes().size(),27);
            Assertions.assertEquals (pdg.statementNodes.size() ,10);
            Assertions.assertEquals (pdg.dataSources.size() ,6);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG17() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test17.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test17.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,2);
            Assertions.assertEquals (pdg.getNodes().size(),29);
            Assertions.assertEquals (pdg.statementNodes.size() ,10);
            Assertions.assertEquals (pdg.dataSources.size() ,11);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG18() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test18.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test18.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,0);
            Assertions.assertEquals (pdg.getNodes().size(),16);
            Assertions.assertEquals (pdg.statementNodes.size() ,10);
            Assertions.assertEquals (pdg.dataSources.size() ,1);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG19() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test19.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test19.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,0);
            Assertions.assertEquals (pdg.getNodes().size(),17);
            Assertions.assertEquals (pdg.statementNodes.size() ,8);
            Assertions.assertEquals (pdg.dataSources.size() ,3);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG20() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test20.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test20.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,0);
            Assertions.assertEquals (pdg.getNodes().size(),10);
            Assertions.assertEquals (pdg.statementNodes.size() ,2);
            Assertions.assertEquals (pdg.dataSources.size() ,5);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG21() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test21.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(2);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test21.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,0);
            Assertions.assertEquals (pdg.getNodes().size(),9);
            Assertions.assertEquals (pdg.statementNodes.size() ,3);
            Assertions.assertEquals (pdg.dataSources.size() ,2);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDGm2() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/testm2.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(1);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/testm2.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,2);
            Assertions.assertEquals (pdg.getNodes().size(),52);
            Assertions.assertEquals (pdg.statementNodes.size() ,21);
            Assertions.assertEquals (pdg.dataSources.size() ,55);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG23() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test22.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(1);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test22.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,2);
            Assertions.assertEquals (pdg.getNodes().size(),28);
            Assertions.assertEquals (pdg.statementNodes.size() ,10);
            Assertions.assertEquals (pdg.dataSources.size() ,6);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG24() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test23.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(1);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test23.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,2);
            Assertions.assertEquals (pdg.getNodes().size(),28);
            Assertions.assertEquals (pdg.statementNodes.size() ,10);
            Assertions.assertEquals (pdg.dataSources.size() ,38);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG25() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test26.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(1);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test26.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,2);
            Assertions.assertEquals (pdg.getNodes().size(),35);
            Assertions.assertEquals (pdg.statementNodes.size() ,11);
            Assertions.assertEquals (pdg.dataSources.size() ,2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG26() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test27.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(1);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test27.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,1);
            Assertions.assertEquals (pdg.getNodes().size(),40);
            Assertions.assertEquals (pdg.statementNodes.size() ,11);
            Assertions.assertEquals (pdg.dataSources.size() ,2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG27() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test28.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(1);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test28.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,1);
            Assertions.assertEquals (pdg.getNodes().size(),35);
            Assertions.assertEquals (pdg.statementNodes.size() ,11);
            Assertions.assertEquals (pdg.dataSources.size() ,2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG28() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test29.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(1);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test29.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,0);
            Assertions.assertEquals (pdg.getNodes().size(),24);
            Assertions.assertEquals (pdg.statementNodes.size() ,8);
            Assertions.assertEquals (pdg.dataSources.size() ,2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG29() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test30.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(1);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test30.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,0);
            Assertions.assertEquals (pdg.getNodes().size(),33);
            Assertions.assertEquals (pdg.statementNodes.size() ,15);
            Assertions.assertEquals (pdg.dataSources.size() ,9);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPDG30() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test31.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(1);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test30.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,0);
            Assertions.assertEquals (pdg.getNodes().size(),27);
            Assertions.assertEquals (pdg.statementNodes.size() ,10);
            Assertions.assertEquals (pdg.dataSources.size() ,10);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    void testPDG31() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test32.py");
        FunctionDef func = (FunctionDef) parse.getInternalBody().get(1);
        PDGBuildingContext context = null;
        try {
            context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x-> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList()), "author/project/test32.py");
            PDGGraph pdg = new PDGGraph(func,context);
            DotGraph dg = new DotGraph(pdg);
            String dirPath = "./OUTPUT/";
            dg.toDotFile(new File(dirPath  +"file___"+".dot"));
            Assertions.assertEquals (pdg.parameters.length,0);
            Assertions.assertEquals (pdg.getNodes().size(),27);
            Assertions.assertEquals (pdg.statementNodes.size() ,10);
            Assertions.assertEquals (pdg.dataSources.size() ,10);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testParse() {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module parse = parser.parse("author/project/test24.py");
        System.out.println(parse);
    }


}