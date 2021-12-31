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
    public static final String COLOR_BLACK = "black";
    public static final String COLOR_BLUE= "blue";
    public static final String COLOR_RED = "red";
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
                if (label.equals("T") || label.equals("F"))
                    graph.append(addEdge(sId, tId, null, null, label));
                else
                    graph.append(addEdge(sId, tId, STYLE_DOTTED, null, label));
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

        String style=null;
        if (node.version ==0)
            style=STYLE_DASHED;
        else if (node.version==1)
            style=STYLE_SOLID;
        graph.append(addNode(id, node.getLabel(), shape, style, color, color));
    }

    public DotGraph(PDGGraph fpdg, List<MatchedNode> graphs) {
        ArrayList<String> colors = new ArrayList<>();
        colors.add(COLOR_BLUE);
        colors.add(COLOR_RED);
        graph = new StringBuilder();
        graph.append(addStart());
        HashMap<PDGNode, Integer> ids = new HashMap<>();
        int id = 0;
        for (PDGNode node : fpdg.getNodes()) {
            ids.put(node, ++id);
            String color = null;
            for (int i =0;i<graphs.size();i++){
                if (Collections.frequency(graphs.get(i).getPDGNodes(),node)==1){
                    color=colors.get(i%colors.size());

                    break;
                }
                else if (Collections.frequency(graphs.get(i).getPDGNodes(),node)>1){
                    color="green";
                    break;
                }
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