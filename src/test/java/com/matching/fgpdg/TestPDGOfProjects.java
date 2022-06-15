package com.matching.fgpdg;

import com.matching.ConcreatePythonParser;
import com.utils.DotGraph;
import org.inferrules.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.python.antlr.Visitor;
import org.python.antlr.ast.*;
import org.python.antlr.ast.Module;
import org.python.antlr.base.stmt;
import org.python.modules.thread._thread$exit_exposer;

import java.io.File;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.System.exit;
import static org.inferrules.Utils.getAllFunctions;

public class TestPDGOfProjects {
    @Test
    void testKerasPDG() {
        String projectPath = Configurations.PROJECT_REPOSITORY+"keras-team/keras/";
        File dir = new File(projectPath);
        ArrayList<File> files = Utils.getPythonFiles(Objects.requireNonNull(dir.listFiles()));
        for (File file : files) {
            System.out.println(file.getAbsolutePath());
            ConcreatePythonParser parser = new ConcreatePythonParser();
            Module parse = parser.parse(file.getAbsolutePath());
            List<stmt> collect = parse.getInternalBody().stream().filter(x -> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList());
            ArrayList<FunctionDef> functions = getAllFunctions(parse);
            for (FunctionDef function : functions) {
                try {
                    String relative = new File(Configurations.PROJECT_REPOSITORY).toURI().relativize(new File(file.getAbsolutePath()).toURI()).getPath();
                    PDGBuildingContext context =new PDGBuildingContext(collect, relative);
                    System.out.println(function.getInternalName());
                    PDGGraph pdg = new PDGGraph(function,context);
                    DotGraph dg = new DotGraph(pdg);
                } catch (IOException e) {
                    System.out.println("Type File is Not available");
                }
            }
        }
    }

    @Test
    void testTensorFlowPDG() {
        String projectPath = Configurations.PROJECT_REPOSITORY+"nltk/nltk/";
        File dir = new File(projectPath);
        ArrayList<File> files = Utils.getPythonFiles(Objects.requireNonNull(dir.listFiles()));
        for (File file : files) {
            System.out.println(file.getAbsolutePath());
            ConcreatePythonParser parser = new ConcreatePythonParser();
            Module parse = parser.parse(file.getAbsolutePath());
            List<stmt> collect = parse.getInternalBody().stream().filter(x -> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList());
            ArrayList<FunctionDef> functions = getAllFunctions(parse);
            for (FunctionDef function : functions) {
                try {
                    String relative = new File(Configurations.PROJECT_REPOSITORY).toURI().relativize(new File(file.getAbsolutePath()).toURI()).getPath();
                    PDGBuildingContext context =new PDGBuildingContext(collect, relative);
                    System.out.println(function.getInternalName());
                    PDGGraph pdg = new PDGGraph(function,context);
                    DotGraph dg = new DotGraph(pdg);
                } catch (IOException e) {
                    System.out.println("Type File is Not available");
                }
            }
        }
    }

    @Test
    void testPytorchPDG() {
        String projectPath = Configurations.PROJECT_REPOSITORY+"pytorch/pytorch/";
        File dir = new File(projectPath);
        ArrayList<File> files = Utils.getPythonFiles(Objects.requireNonNull(dir.listFiles()));
        for (File file : files) {
            System.out.println(file.getAbsolutePath());
            ConcreatePythonParser parser = new ConcreatePythonParser();
            Module parse = parser.parse(file.getAbsolutePath());
            List<stmt> collect = parse.getInternalBody().stream().filter(x -> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList());
            ArrayList<FunctionDef> functions = getAllFunctions(parse);
            for (FunctionDef function : functions) {
                try {
                    String relative = new File(Configurations.PROJECT_REPOSITORY).toURI().relativize(new File(file.getAbsolutePath()).toURI()).getPath();
                    PDGBuildingContext context =new PDGBuildingContext(collect, relative);
                    System.out.println(function.getInternalName());
                    PDGGraph pdg = new PDGGraph(function,context);
                    DotGraph dg = new DotGraph(pdg);
                } catch (IOException e) {
                    System.out.println("Type File is Not available");
                }
            }
        }
    }


}
