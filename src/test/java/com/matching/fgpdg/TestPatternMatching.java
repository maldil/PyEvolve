package com.matching.fgpdg;

import com.matching.ConcreatePythonParser;
import com.matching.fgpdg.nodes.Guards;
import com.matching.fgpdg.nodes.TypeInfo.TypeWrapper;
import com.utils.DotGraph;
import com.utils.FileIO;
import org.inferrules.Utils;
import org.junit.jupiter.api.Test;
import org.python.antlr.ast.*;
import org.python.antlr.ast.Module;
import org.python.antlr.base.stmt;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.inferrules.Utils.getAllFunctions;

public class TestPatternMatching {
    @Test
    public void getPattern1() throws IOException {
        String content = FileIO.readStringFromFile("repos.csv");
        Scanner sc = new Scanner(content);
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String projectPath = Configurations.PROJECT_REPOSITORY+line;
            analyseProject(projectPath);
        }
    }

    private void analyseProject(String projectPath) throws IOException {
        File dir = new File(projectPath);

        ArrayList<File> files = Utils.getPythonFiles(Objects.requireNonNull(dir.listFiles()));
        Module patternModule = getPythonModule("author/project/pattern.py");

        Guards guards = new Guards(com.utils.Utils.getFileContent(getPathToResources("author/project/pattern.py")));
        TypeWrapper wrapper = new TypeWrapper(guards);
        PDGBuildingContext patternContext = new PDGBuildingContext(patternModule.getInternalBody().stream().filter(x -> x instanceof Import
                || x instanceof ImportFrom).collect(Collectors.toList()),wrapper);
        PDGGraph ppdg = new PDGGraph(patternModule,patternContext);

        DotGraph pdg1 = new DotGraph(ppdg);
        pdg1.toDotFile(new File("./OUTPUT/"  +"____pattern_code__file___"+".dot"));

        for (File file : files) {
            System.out.println("Processing "+file.getAbsolutePath());
            Module parse = getPythonModule (file.getAbsolutePath());
            List<stmt> codeImports = parse.getInternalBody().stream().filter(x -> x instanceof Import
                    || x instanceof ImportFrom).collect(Collectors.toList());
            ArrayList<FunctionDef> functions = getAllFunctions(parse);
//            if (file.getAbsolutePath().equals("/Users/malinda/Documents/Research3/PROJECT_REPO/keras-team/keras/keras/datasets/mnist.py")) {
                System.out.println();
                for (FunctionDef function : functions) {
                    try {
                        int num =0;
                        String relative = new File(Configurations.PROJECT_REPOSITORY).toURI().relativize(new File(file.getAbsolutePath()).toURI()).getPath();
                        PDGBuildingContext cContext = new PDGBuildingContext(codeImports, relative);
                        System.out.println(function.getInternalName());
                        PDGGraph pdg = new PDGGraph(function, cContext);
                        DotGraph dg = new DotGraph(pdg);
                        dg.toDotFile(new File("./OUTPUT/"  +"____code__file___"+".dot"));
                        MatchPDG match = new MatchPDG();
                        List<MatchedNode> graphs = match.getSubGraphs(ppdg, pdg, patternContext, cContext);
                        if (graphs!=null) {
                            graphs.forEach(x -> x.updateAllMatchedNodes(x,ppdg));
                            if (graphs.stream().anyMatch(MatchedNode::isAllChildsMatched)){
                                String paterntName = "";
                                if (function.getParent()!=null && function.getParent() instanceof FunctionDef)
                                    paterntName = ((FunctionDef)function.getParent()).getInternalName();
                                else if (function.getParent()!=null && function.getParent() instanceof ClassDef)
                                    paterntName = ((ClassDef)function.getParent()).getInternalName();
                                String fileName =  paterntName +"____"+ function.getInternalName();
                                com.utils.Utils.markNodesInCode(file.getAbsolutePath(), graphs,
                                        "OUTPUT/matches/" + relative.substring(0,relative.length()-3)+"____"+fileName+  ".html",
                                        Arrays.stream(relative.split("/")).map(x->"../").skip(1).collect(Collectors.joining()));
                                match.drawMatchedGraphs(pdg,graphs,"OUTPUT/matches/"+relative.substring(0,relative.length()-3)+"____"+fileName+  ".dot");
                                graphs.forEach(x -> x.updateAllMatchedNodes(x,ppdg));
                                num++;
                            }
                        }
//                    match.drawMatchedGraphs(fpdg,graphs,"OUTPUT/matches/text1.dot");
//                        if (graphs != null && graphs.stream().anyMatch(MatchedNode::isAllChildsMatched))


                    } catch (IOException e) {
                        System.out.println("Type File is Not available");
                    }
                }
//            }
        }
    }

    private Module getPythonModule(String fileName){
        ConcreatePythonParser parser = new ConcreatePythonParser();
        return parser.parse(fileName);
    }

    private String getPathToResources(String name){
        File f = new File(name);
        if (f.exists()) {
            return f.getAbsolutePath();
        }
        return getClass().getClassLoader().getResource(name).getPath();

    }
}
