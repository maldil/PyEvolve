package com.utils;

import com.matching.fgpdg.MatchedNode;
import com.matching.fgpdg.PDGGraph;
import com.matching.fgpdg.nodes.PDGActionNode;
import com.matching.fgpdg.nodes.PDGDataNode;
import com.matching.fgpdg.nodes.PDGNode;
import org.apache.commons.io.IOUtils;
import org.python.antlr.ast.Assign;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Utils {



    static class Interval
    {
        int start;
        int end;

        public Interval(int start, int end)
        {
            super();
            this.start = start;
            this.end = end;
        }

        public int getStart(){
            return start;
        }

        public int getEnd(){
            return end;
        }

        @Override
        public String toString() {
            return "["+this.start+","+this.end+"]";
        }

        @Override
        public boolean equals(Object o) {

            // If the object is compared with itself then return true
            if (o == this) {
                return true;
            }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
            if (!(o instanceof Interval)) {
                return false;
            }

            // typecast o to Complex so that we can compare data members
            Interval c = (Interval) o;

            // Compare the data members and return accordingly
            return start == c.start
                    && end == c.end;
        }
    };

//    public static void isAllNodesMatched(List<MatchedNode> grphs, PDGGraph pattern){
//        for (MatchedNode grph : grphs) {
//            for (PDGNode node : pattern.getNodes()) {
//
//            }
//
//            grph.getCodePDGNodes()
//        }
//
//    }

    public static void markNodesInCode(String code, List<MatchedNode> pdgs,String fileName, String stylefile,String link) throws IOException {
        if(new File(code).exists())
        {
            code = getFileContent(code);
        }

        List<Interval> duration = new ArrayList<>();
        for (MatchedNode pdg : pdgs) {
            for (PDGNode node : pdg.getCodePDGNodes()) {
                if (node.getAstNode()!=null && (node instanceof PDGDataNode || node instanceof PDGActionNode)){
                    if (node.getAstNode() instanceof expr){
                        duration.add(new Interval(((expr)node.getAstNode()).getCharStartIndex(),((expr)node.getAstNode()).getCharStopIndex()));
                    }
                    else if (node.getAstNode() instanceof stmt){
                        System.out.println(node.getAstNode().getClass());
                        duration.add(new Interval(((stmt)node.getAstNode()).getCharStartIndex(),((stmt)node.getAstNode()).getCharStopIndex()));
                    }
                    else if (node.getAstNode() instanceof org.python.antlr.ast.Assign){
                        duration.add(new Interval(((Assign)node.getAstNode()).getCharStartIndex(),((stmt)node.getAstNode()).getCharStopIndex()));
                    }
                    else if (node.getAstNode() instanceof org.python.antlr.ast.arg){
                        duration.add(new Interval(((org.python.antlr.ast.arg)node.getAstNode()).getCharStartIndex(),((org.python.antlr.ast.arg)node.getAstNode()).getCharStopIndex()));
                    }
                    else
                        Assertions.UNREACHABLE();
                }
            }
        }
        writeMatchCodeToHTML(code, fileName, duration,stylefile,link);
    }

    private static void writeMatchCodeToHTML(String code, String fileName, List<Interval> duration, String styleFile,String githubLink) {
        String s = "\n<a href=\""+githubLink.split("@")[0] +"\">GitHubLink</a>"+"\n\n"+
                "\n<a href=\""+githubLink.split("@")[1] +"\">GitMyHubLink</a>"+"\n\n"+
                markupCode(duration.stream().map(x -> {
            ArrayList<Integer> list = new ArrayList<>();
            list.add(x.start);
            list.add(x.end-x.start);
            return list;
        }).collect(Collectors.toCollection(ArrayList::new)), code);

        StringBuilder sampleChange = new StringBuilder();
        sampleChange.append("<link rel=\"stylesheet\" href=\""+styleFile+"/default.css\">\n" +
                "<script src=\""+styleFile+"/highlight.pack.js\"></script> \n" +
                "<script>hljs.initHighlightingOnLoad();</script>\n");
        sampleChange.append("<html>");

        String markedupHTML = "<pre><code class='java'>" + s + "</code></pre>";
        sampleChange.append(markedupHTML);
        System.out.println(s);

        FileIO.writeStringToFile(sampleChange.toString(),
                fileName);
    }

    public static List<Interval> getSortedList(List<Interval> duration){
        List<Interval> intervals =  (List<Interval>) ((ArrayList<Interval>) duration).clone();;
        while (intervals!=null) {
            intervals = prepareList(intervals);
            if (intervals != null)
                duration = (List<Interval>) ((ArrayList<Interval>) intervals).clone();
        }
        return duration;
    }

    private static List<Interval> prepareList(List<Interval> duration){
        List<Interval> duration_copy1= (List<Interval>) ((ArrayList<Interval>) duration).clone();
        List<Interval> duration_copy2=(List<Interval>) ((ArrayList<Interval>) duration).clone();;
        boolean changed=false;
        duration_copy1.sort(Comparator.comparingInt(Interval::getStart));
        int k=0;
        while(k<duration_copy1.size()){
            if (k+1<duration_copy1.size()&&duration_copy1.get(k).end>=duration_copy1.get(k+1).start){
                Interval interval1 = duration_copy1.get(k);
                Interval interval2 = duration_copy1.get(k+1);
                duration_copy1.remove(k);
                duration_copy2.remove(k+1);
                List<Interval> result = duration_copy1.stream()
                        .distinct()
                        .filter(duration_copy2::contains)
                        .collect(Collectors.toList());
                Interval mergeNode = mergeNodes(interval1, interval2);
                if (!result.contains(mergeNode))
                    result.add(k,mergeNode);
                duration_copy1= (List<Interval>) ((ArrayList<Interval>) result).clone();
                duration_copy2=(List<Interval>) ((ArrayList<Interval>) result).clone();
                changed=true;
            }
            else{
                k++;
            }
        }
        return !changed ? null: duration_copy1;
    }

    private static Interval mergeNodes(Interval first, Interval second){ // mergers two overlapping intervals into one
        return new Interval(Math.min(first.start, second.start), Math.max(first.end, second.end)) ;
    }

    public static String getFileContent(String path) throws IOException {
        FileInputStream fisTargetFile = new FileInputStream(new File(path));
        return IOUtils.toString(fisTargetFile, "UTF-8");
    }

    private static String markupCode(ArrayList<ArrayList<Integer>> highlights, String str) {
        StringBuilder markedupString = new StringBuilder();
        Collections.sort(highlights, new Comparator<ArrayList<Integer>>() {
            @Override
            public int compare(ArrayList<Integer> l1, ArrayList<Integer> l2) {
                return l1.get(0).compareTo(l2.get(0));
            }
        });
        Object[] sortedArray = highlights.toArray();
        if (sortedArray.length > 0) {
            ArrayList<Integer> first = ((ArrayList<Integer>) sortedArray[0]);
            int fPos = first.get(0);
//            for (int i = 0; i < 4; i++) {   //add first five rules before th
//                fPos = str.lastIndexOf('\n', fPos - 1);
//                if (fPos == -1) {
//                    fPos = 0;
//                    break;
//                }
//            }
            fPos=0;
            markedupString.append(str.substring(fPos, first.get(0)).replace("<", "&lt;").replace(">", "&gt;").replace("#", "&#47&#47").replace("'", "&quot").replace("\"\"\"", ""));
            int end = -1;
            for (int i = 0; i < sortedArray.length - 1; i++) {
                ArrayList<Integer> al = (ArrayList<Integer>) sortedArray[i];
                if (al.get(0) + al.get(1) > end) {
                    markedupString.append("<a id=\"change\">");
//					markedupString.append("<b>");
                    markedupString.append(str.substring(Math.max(al.get(0), end), al.get(0) + al.get(1)).replace("<", "&lt;").replace(">", "&gt;").replace("#", "&#47&#47").replace("'", "&quot").replace("\"\"\"", ""));
                    markedupString.append("</a>");
//					markedupString.append("</b>");
                    end = al.get(0) + al.get(1);
                }
                if (i < sortedArray.length) {
                    ArrayList<Integer> next = (ArrayList<Integer>) sortedArray[i + 1];
                    if (next.get(0) > end)
                        markedupString.append(str.substring(end, next.get(0)).replace("<", "&lt;").replace(">", "&gt;").replace("#", "&#47&#47").replace("'", "&quot").replace("\"\"\"", ""));
                    else if (next.get(0) + next.get(1) > end)
                        System.err.print(""); // DEBUG
                }
            }
            ArrayList<Integer> last = ((ArrayList<Integer>) sortedArray[sortedArray.length - 1]);
            if (last.get(0) + last.get(1) > end) {
                markedupString.append("<a id=\"change\">");
//				markedupString.append("<b>");
                markedupString.append(str.substring(Math.max(last.get(0), end), last.get(0) + last.get(1)).replace("<", "&lt;").replace(">", "&gt;").replace("#", "&#47&#47").replace("'", "&quot").replace("\"\"\"", ""));
//				markedupString.append("</b>");
                markedupString.append("</a>");

                end = last.get(0) + last.get(1);
            }
            int lPos = end;  //add next five rules after the change

//            for (int i = 0; i < 4; i++) {
//                lPos = str.indexOf('\n', lPos + 1);
//                if (lPos == -1) {
//                    lPos = str.length();
//                    break;
//                }
//            }
            lPos=str.length();
            markedupString.append(str.substring(end, lPos).replace("<", "&lt;").replace(">", "&gt;").replace("#", "&#47&#47").replace("'", "&quot").replace("\"\"\"", ""));
        }
        return String.valueOf(markedupString);
    }

    public static PDGNode getMaxDOF(HashSet<PDGNode> nodes){
        int maxDOF=0;
        PDGNode maxPDGNode=null;
        for (PDGNode node : nodes) {
            int dof = node.getInEdges().size()+node.getOutEdges().size();
            if (maxDOF<dof){
                maxDOF=dof;
                maxPDGNode=node;
            }
        }
        return maxPDGNode;
    }

//    public MatchedNode pruneMatchedNode(MatchedNode node){
//
//
//
//    }
}
