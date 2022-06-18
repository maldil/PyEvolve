package com.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


import com.matching.fgpdg.MatchedNode;
import com.matching.fgpdg.PDGGraph;
import com.matching.fgpdg.nodes.*;


public class DotGraph {
    public static final String SHAPE_BOX = "box";
    public static final String SHAPE_DIAMOND = "diamond";
    public static final String SHAPE_ELLIPSE = "ellipse";
    public static final String SHAPE_TRIANGLE = "triangle";
    public static final String SHAPE_POLYGON = "polygon";
    public static final String SHAPE_PENTAGON = "pentagon";
    public static final String COLOR_BLACK = "black";
    public static final String COLOR_BLUE= "blue";
    public static final String COLOR_RED = "red";
    public static final String COLOR_GREEN= "red";
    public static final String COLOR_YELLOW= "yellow";
    public static final String STYLE_ROUNDED = "rounded";
    public static final String STYLE_DOTTED = "dotted";
    public static final String STYLE_SOLID = "solid";
    public static final String STYLE_DASHED = "dashed";
    public static String EXEC_DOT = "/usr/local/bin/dot"; // Windows
    private static int idCounter = 0;
    private StringBuilder graph = new StringBuilder();

    public DotGraph(StringBuilder sb) {
        this.graph = sb;
    }

    public DotGraph(PDGGraph pd){
        graph = new StringBuilder();
        graph.append(addStart());
        HashMap<PDGNode, Integer> ids = new HashMap<>();
        int id = 0;
        for (PDGNode node : pd.getNodes()) {
            ids.put(node, ++id);
            String color = null;
            if (node instanceof PDGHoleNode){
                if (((PDGHoleNode) node).isDataNode()){
                    color=COLOR_RED;
                }
                if (((PDGHoleNode) node).isActionNode()){
                    color=COLOR_GREEN;
                }
                if (((PDGHoleNode) node).isControlNode()){
                    color=COLOR_BLUE;
                }


            }
            addNode(id, node, color);
        }

        addEdges(pd, ids);

        graph.append(addEnd());

    }

    private void addEdges(PDGGraph pd, HashMap<PDGNode, Integer> ids) {
        for (PDGNode node : pd.getNodes()) {
            int tId = ids.get(node);
            for (PDGEdge e : node.getInEdges()) {
                int sId = ids.get(e.getSource());
                String label = e.getLabel();
                String color =null;
                if (label.equals("re_def")){
                    color=COLOR_RED;
                }

                if (label.equals("T") || label.equals("F"))
                    graph.append(addEdge(sId, tId, null, color, label));
                else if (e instanceof PDGControlEdge)
                    graph.append(addEdge(sId, tId, STYLE_SOLID, color, label));
                else
                    graph.append(addEdge(sId, tId, STYLE_DOTTED, color, label));
            }
        }
    }

    private void addNode(int id, PDGNode node, String color) {
        String shape = null;
        if (node instanceof PDGDataNode){
            shape = SHAPE_ELLIPSE;
        }
        else if (node instanceof PDGActionNode){
            shape = SHAPE_BOX;
        }
        else if (node instanceof PDGControlNode){
            shape = SHAPE_DIAMOND;
        }
        else if (node instanceof PDGAlphHole){
            shape = SHAPE_PENTAGON;
        }
        else if (node instanceof PDGLazyHole){
            shape = SHAPE_PENTAGON;
            color=COLOR_BLUE;
        }

        String style=null;
        if (node.version ==0)
            style=STYLE_DASHED;
        else if (node.version==1)
            style=STYLE_SOLID;
        graph.append(addNode(id, "("+node.getId()+")"+node.getLabel(), shape, style, color, color));
    }

    public DotGraph(PDGGraph fpdg, List<MatchedNode> graphs) {
        ArrayList<String> colors = new ArrayList<>();
        colors.add(COLOR_BLUE);
        colors.add(COLOR_RED);
        colors.add(COLOR_GREEN);
        colors.add(COLOR_BLACK);
        colors.add(COLOR_YELLOW);

        graph = new StringBuilder();
        graph.append(addStart());
        HashMap<PDGNode, Integer> ids = new HashMap<>();
        int id = 0;
        for (PDGNode node : fpdg.getNodes()) {
            String color=null;
            ids.put(node, ++id);

            for (int i =0;i<graphs.size();i++){
                for (PDGNode pdgNode : graphs.get(i).getCodePDGNodes()) {
                    if (pdgNode.equals(node)) {
                        color = colors.get(i%5);
                        break;
                    }
                }


//                if ()
//                if (Collections.frequency(graphs.get(i).getPDGNodes(),node)==1){
//                    color=colors.get(i%colors.size());
//
//                    break;
//                }
//                else if (Collections.frequency(graphs.get(i).getPDGNodes(),node)>1){
//                    color="green";
//                    break;
//                }
            }

            addNode(id, node, color);
        }

        addEdges(fpdg, ids);

        graph.append(addEnd());




    }


    public String addStart() {
        return "digraph G {\n";
    }

    public String addNode(int id, String label, String shape, String style,
                          String borderColor, String fontColor) {
        StringBuffer buf = new StringBuffer();
        buf.append(id + " [label=\"" + label + "\"");
        if (shape != null && !shape.isEmpty())
            buf.append(" shape=" + shape);
        if (style != null && !style.isEmpty())
            buf.append(" style=" + style);
        if (borderColor != null && !borderColor.isEmpty())
            buf.append(" color=" + borderColor);
        if (fontColor != null && !fontColor.isEmpty())
            buf.append(" fontcolor=" + fontColor);
        buf.append("]\n");

        return buf.toString();
    }

    public String addEdge(int sId, int eId, String style, String color,
                          String label) {
        StringBuffer buf = new StringBuffer();
        if (label == null)
            label = "";
        buf.append(sId + " -> " + eId + " [label=\"" + label + "\"");
        if (style != null && !style.isEmpty())
            buf.append(" style=" + style);
        if (color != null && !color.isEmpty())
            buf.append(" color=" + color);
        buf.append("];\n");

        return buf.toString();
    }

    public String addEnd() {
        return "}";
    }

    public String getGraph() {
        return this.graph.toString();
    }

    public void toDotFile(File file) {
        try {
            BufferedWriter fout = new BufferedWriter(new FileWriter(file));
            fout.append(this.graph.toString());
            fout.flush();
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void toGraphics(String file, String type) {
        Runtime rt = Runtime.getRuntime();

        String[] args = { EXEC_DOT, "-T" + type, file + ".dot", "-o",
                file + "." + type };
        try {
            Process p = rt.exec(args);
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
