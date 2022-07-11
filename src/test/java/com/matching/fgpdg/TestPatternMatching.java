package com.matching.fgpdg;

import com.matching.ConcreatePythonParser;
import com.matching.fgpdg.nodes.Guards;
import com.matching.fgpdg.nodes.TypeInfo.TypeWrapper;
import com.utils.DotGraph;
import com.utils.FileIO;
import com.utils.GitUtils;
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
    public void
    getPattern1() throws Exception {
        String content = FileIO.readStringFromFile("repos.csv");
        Scanner sc = new Scanner(content);
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            System.out.println("Analysing project "+line);
            String projectPath = Configurations.PROJECT_REPOSITORY+line;
            analyseProject(projectPath);
        }
    }

    private Module getPythonModuleForTemplate(String fileName) throws Exception {
        ConcreatePythonParser parser = new ConcreatePythonParser();
        return parser.parseTemplates(FileIO.readStringFromFile(fileName));
    }

    private void analyseProject(String projectPath) throws Exception {
        for (File pattern : getAllPatternFiles(getPathToResources("author/project/GroupOfPatterns/"))) {
            Module patternModule = getPythonModuleForTemplate(pattern.getAbsolutePath());
            processProjectForPattern(projectPath,pattern.getPath() ,patternModule);
        }
    }

    private void processProjectForPattern(String projectPath, String patternPath,Module patternModule) throws IOException {
        File dir = new File(projectPath);
        if (dir.listFiles()==null)
            return;
        ArrayList<File> files = Utils.getPythonFiles(Objects.requireNonNull(dir.listFiles()));
        Guards guards = new Guards(com.utils.Utils.getFileContent(getPathToResources(patternPath)),patternModule);
        TypeWrapper wrapper = new TypeWrapper(guards);
        PDGBuildingContext patternContext = new PDGBuildingContext(patternModule.getInternalBody().stream().filter(x -> x instanceof Import
                || x instanceof ImportFrom).collect(Collectors.toList()),wrapper);
        PDGGraph ppdg = new PDGGraph(patternModule,patternContext);

        DotGraph pdg1 = new DotGraph(ppdg);
        pdg1.toDotFile(new File("./OUTPUT/"  +"____pattern_code__file___"+".dot"));
        String projectName = new File(Configurations.PROJECT_REPOSITORY).toURI().
                relativize(new File(projectPath).toURI()).getPath();
        String gitHubProjectName =  "https://github.com/"+projectName+"blob/"+GitUtils.getBranch(GitUtils.connect(projectPath))+"/";
        String gitHubMyProjectName =  "https://github.com/"+  "maldil/"+projectName.split("/")[1] +"/blob/"+GitUtils.getBranch(GitUtils.connect(projectPath))+"/";

        for (File file : files) {
            System.out.println("Processing "+file.getAbsolutePath());
            if (file.getAbsolutePath().contains("msaf/pymf/bnmf.py")){
                System.out.println();
            }
            String gitHubFilePath = gitHubProjectName+new File(Configurations.PROJECT_REPOSITORY+"/"+projectName).toURI().
                    relativize(new File(file.toURI()).toURI()).getPath();
            Module parse = getPythonModule (file.getAbsolutePath());
            if (parse!=null){
                List<stmt> codeImports = parse.getInternalBody().stream().filter(x -> x instanceof Import
                        || x instanceof ImportFrom).collect(Collectors.toList());
                ArrayList<FunctionDef> functions = getAllFunctions(parse);
//            if (file.getAbsolutePath().equals("/Users/malinda/Documents/Research3/PROJECT_REPO/keras-team/keras/keras/datasets/mnist.py")) {
                int num = 0;
                if (functions.size()>0) {
                    for (FunctionDef function : functions) {
                        System.out.println("Function: " + function.getInternalName());
                        try {
                            String gitHubLocation= gitHubFilePath+"#L"+function.getLineno();
                            String gitHubMyLocation = gitHubMyProjectName+new File(Configurations.PROJECT_REPOSITORY+"/"+projectName).toURI().
                                    relativize(new File(file.toURI()).toURI()).getPath()+"#L"+function.getLineno();
                            String relative = new File(Configurations.PROJECT_REPOSITORY).toURI().relativize(new File(file.getAbsolutePath()).toURI()).getPath();
                            PDGBuildingContext cContext = new PDGBuildingContext(codeImports, relative);
                            System.out.println(function.getInternalName());
                            PDGGraph pdg = new PDGGraph(function, cContext);
                            DotGraph dg = new DotGraph(pdg);
                            dg.toDotFile(new File("./OUTPUT/" + "____code__file___" + ".dot"));
                            MatchPDG match = new MatchPDG();
                            List<MatchedNode> graphs = match.getSubGraphs(ppdg, pdg, patternContext, cContext);
                            if (graphs != null) {
                                graphs.forEach(x -> x.updateAllMatchedNodes(x, ppdg));
                                if (graphs.stream().anyMatch(MatchedNode::isAllChildsMatched)) {
                                    String paterntName = "";
                                    if (function.getParent() != null && function.getParent() instanceof FunctionDef)
                                        paterntName = ((FunctionDef) function.getParent()).getInternalName();
                                    else if (function.getParent() != null && function.getParent() instanceof ClassDef)
                                        paterntName = ((ClassDef) function.getParent()).getInternalName();
                                    String fileName = paterntName + "____" + function.getInternalName();
                                    File f = new File("OUTPUT/matches/" + relative.substring(0, relative.length() - 3) + "____" + fileName + ".html");
                                    if(f.exists() && !f.isDirectory()) {
                                        fileName = paterntName + "____" + function.getInternalName()+"____" + num;
                                    }
                                    com.utils.Utils.markNodesInCode(file.getAbsolutePath(), graphs,
                                            "OUTPUT/matches/" + relative.substring(0, relative.length() - 3) + "____" + fileName + ".html",
                                            Arrays.stream(relative.split("/")).map(x -> "../").skip(1).collect(Collectors.joining()),gitHubLocation+"@"+gitHubMyLocation);
                                    match.drawMatchedGraphs(pdg, graphs, "OUTPUT/matches/" + relative.substring(0, relative.length() - 3) + "____" + fileName + ".dot");
                                    graphs.forEach(x -> x.updateAllMatchedNodes(x, ppdg));
                                    num += 1;
                                }
                            }
//                    match.drawMatchedGraphs(fpdg,graphs,"OUTPUT/matches/text1.dot");
//                        if (graphs != null && graphs.stream().anyMatch(MatchedNode::isAllChildsMatched))
                        } catch (IOException e) {
                            System.out.println("Type File is Not available");
                        }
                    }
                }
                else {
                    System.out.println("No functions");
                }
            }
        }
    }

    private ArrayList<File> getAllPatternFiles(String path){
        File dir = new File(path);
        if (dir.listFiles()==null)
            return new ArrayList<> ();
        return Utils.getPythonFiles(Objects.requireNonNull(dir.listFiles()));
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
