package com.matching.fgpdg;

import com.matching.ConcreatePythonParser;
import com.matching.fgpdg.nodes.Guards;
import com.matching.fgpdg.nodes.TypeInfo.TypeWrapper;
import com.utils.DotGraph;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.Module;

import java.io.File;
import java.util.stream.Collectors;

public class PDGGraphTestForTemplates {
    public PDGGraphTestForTemplates() throws Exception {
    }
    @Test
    void testPDG2() throws Exception {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        String code =   "# type :[[l1]] : int\n" +
                "# type :[[l2]] : int\n" +
                "# type :[[l3]] : int[]\n" +
                ":[[l1]] = 0\n" +
                "for :[[l2]] in :[[l3]]:\n"+
                "   :[[l1]]=:[[l1]]+:[[l2]]";
        Module parse = parser.parseTemplates(code);
        Guards guard = new Guards(code,parse);
        TypeWrapper wrapper = new TypeWrapper(guard);
        PDGBuildingContext context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x -> x instanceof Import
                || x instanceof ImportFrom).collect(Collectors.toList()),wrapper);
        PDGGraph pdg = new PDGGraph(parse,context);
        DotGraph dg = new DotGraph(pdg);
        String dirPath = "./OUTPUT/";
        dg.toDotFile(new File(dirPath  +"file___"+".dot"));
        Assertions.assertEquals(pdg.getNodes().size(),13);
        System.out.println(pdg);
    }

    @Test
    void testPDG3() throws Exception {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        String code = """
                # import :[[l4]] : numpy
                import numpy as np
                noise = self._diag_tensor[0, 0]
                :[[l1]] = :[[l4]].:[[l6]][:[[l9]], :[[l9]]]
                :[[l11]] = []
                for :[[l15]] in :[[l4]]._lazy_tensor.lazy_tensors:
                    array.append(array.evaluate())
                    :[[l11]].append.symeig(eigenvectors=True)[:, :[l9]].unsqueeze(-:[[l42]]))
                    :[[l44]] = :[[l11]].matmul(:[[l11]]().t)
                :[[l56]].:[[l58]](:[[l44]] + :[[l1]]).:[[l62]]""";
        Module parse = parser.parseTemplates(code);
        Guards guard = new Guards(code,parse);
        TypeWrapper wrapper = new TypeWrapper(guard);
        PDGBuildingContext context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x -> x instanceof Import
                || x instanceof ImportFrom).collect(Collectors.toList()),wrapper);
        PDGGraph pdg = new PDGGraph(parse,context);
        DotGraph dg = new DotGraph(pdg);
        String dirPath = "./OUTPUT/";
        dg.toDotFile(new File(dirPath  +"file___"+".dot"));
        Assertions.assertEquals(pdg.getNodes().size(),47);
    }

    @Test
    void testPDG4() throws Exception {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        String code = """
                with :[l3] as :[[l1]]:
                    :[[l13]] = :[[l16]].:[[l18]](:[[l1]], :[l21])""";
        Module parse = parser.parseTemplates(code);
        Guards guard = new Guards(code,parse);
        TypeWrapper wrapper = new TypeWrapper(guard);
        PDGBuildingContext context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x -> x instanceof Import
                || x instanceof ImportFrom).collect(Collectors.toList()),wrapper);
        PDGGraph pdg = new PDGGraph(parse,context);
        DotGraph dg = new DotGraph(pdg);
        String dirPath = "./OUTPUT/";
        dg.toDotFile(new File(dirPath  +"file___"+".dot"));
        Assertions.assertEquals(pdg.getNodes().size(),12);
    }

    @Test
    void testPDG5() throws Exception {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        String code = """
                # type :[[l1]] : int[]\n 
                mean = sum(:[[l1]])/len(:[[l1]])""";
        Module parse = parser.parseTemplates(code);
        Guards guard = new Guards(code,parse);
        TypeWrapper wrapper = new TypeWrapper(guard);
        PDGBuildingContext context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x -> x instanceof Import
                || x instanceof ImportFrom).collect(Collectors.toList()),wrapper);
        PDGGraph pdg = new PDGGraph(parse,context);
        DotGraph dg = new DotGraph(pdg);
        String dirPath = "./OUTPUT/";
        dg.toDotFile(new File(dirPath  +"file___"+".dot"));
        Assertions.assertEquals(pdg.getNodes().size(),8);
    }

    @Test
    void testPDG6() throws Exception {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        String code = """ 
                # import :[[l1]] : numpy\n
                :[[l1]].dot(:[[l1]].dot(:[[l2]], :[[l3]]), :[[l4]]))""";
        Module parse = parser.parseTemplates(code);
        Guards guard = new Guards(code,parse);
        TypeWrapper wrapper = new TypeWrapper(guard);
        PDGBuildingContext context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x -> x instanceof Import
                || x instanceof ImportFrom).collect(Collectors.toList()),wrapper);
        PDGGraph pdg = new PDGGraph(parse,context);
        DotGraph dg = new DotGraph(pdg);
        String dirPath = "./OUTPUT/";
        dg.toDotFile(new File(dirPath  +"file___"+".dot"));
        Assertions.assertEquals(pdg.getNodes().size(),7);
    }


    @Test
    void testPDG7() throws Exception {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        String code = """
                # type :[[l1]] : bool
                :[[l1]] = False
                for :[[l2]] in callbacks.values():
                    if :[l3]:
                          :[[l1]] = True
                          break
                """;
        Module parse = parser.parseTemplates(code);
        Guards guard = new Guards(code,parse);
        TypeWrapper wrapper = new TypeWrapper(guard);
        PDGBuildingContext context = new PDGBuildingContext(parse.getInternalBody().stream().filter(x -> x instanceof Import
                || x instanceof ImportFrom).collect(Collectors.toList()),wrapper);
        PDGGraph pdg = new PDGGraph(parse,context);
        DotGraph dg = new DotGraph(pdg);
        String dirPath = "./OUTPUT/";
        dg.toDotFile(new File(dirPath  +"file___"+".dot"));
        Assertions.assertEquals(pdg.getNodes().size(),18);
    }





}
