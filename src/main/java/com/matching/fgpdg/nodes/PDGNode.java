package com.matching.fgpdg.nodes;

import com.utils.Assertions;
import org.python.antlr.base.expr;
import org.python.core.PyObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public abstract class PDGNode {
    public static final String PREFIX_DUMMY = "dummy_";
    private HashMap<Object,Object> propertyMap = new HashMap<>();
    protected PyObject astNode;
    protected int astNodeType;
    protected String key;
    protected PDGNode control;
    protected String dataType;
    protected ArrayList<PDGEdge> inEdges = new ArrayList<PDGEdge>();
    protected ArrayList<PDGEdge> outEdges = new ArrayList<PDGEdge>();
    private static int nodeNumber = 0;
    private int id;


    public void setProperty(Object property,Object object){
        this.propertyMap.put(property,object);
    }


    public Object getProperty(Object property){
        return this.propertyMap.get(property);
    }

    public void setInEdges(ArrayList<PDGEdge> inEdges) {
        this.inEdges = inEdges;
    }

    public void setOutEdges(ArrayList<PDGEdge> outEdges) {
        this.outEdges = outEdges;
    }

    public int getId() {
        return id;
    }


    public void setId(int nid) {
         this.id =nid;
    }

    public int version;

    public PDGNode(PyObject astNode, int nodeType) {
        this.astNode = astNode;
        this.astNodeType = nodeType;
        this.id=nodeNumber;
        nodeNumber++;
    }

    public PDGNode(PyObject astNode, int nodeType, String key) {
        this(astNode, nodeType);
        this.key = key;
    }

    public String getDataType() {
        return dataType;
    }

    public static String getPrefixDummy() {
        return PREFIX_DUMMY;
    }

    public void setAstNodeType(int astNodeType) {
        this.astNodeType = astNodeType;
    }

    public String getDataName() {
        if (this instanceof PDGDataNode)
            return ((PDGDataNode) this).getDataName();
        else if (this instanceof PDGHoleNode) {
            return ((PDGHoleNode)this).getDataName();

        }

        return null;
    }

    public PDGNode getControl() {
        return control;
    }

    abstract public String getLabel();

    abstract public String getExasLabel();

    public String getKey() {
        return key;
    }

    public int getAstNodeType() {
        return astNodeType;
    }

    public PyObject getAstNode() {
        return astNode;
    }

    public ArrayList<PDGEdge> getInEdges() {
        return inEdges;
    }

    public ArrayList<PDGEdge> getOutEdges() {
        return outEdges;
    }

    public void addOutEdge(PDGEdge edge) {
        outEdges.add(edge);
    }

    public void addInEdge(PDGEdge edge) {
        inEdges.add(edge);
    }

    public boolean isLiteral(expr node) {
        Assertions.UNREACHABLE();
        isLiteral(node.getNodeType());
        return true;
    }

    public boolean isLiteral(int nodeType) {
        if (nodeType == PyObject.NUM ||
                nodeType == PyObject.STR)
            return true;
        return false;
    }

    public boolean isLiteral() {
        return isLiteral(astNodeType);
    }

    public void delete() {
        for (PDGEdge e : inEdges) {
            e.source.outEdges.remove(e);
        }
        inEdges.clear();
        for (PDGEdge e : outEdges)
            e.target.inEdges.remove(e);
        outEdges.clear();
        control = null;
    }

    public boolean isDefinition() {
        if (this instanceof PDGDataNode)
            return ((PDGDataNode) this).isDefinition();
        return false;
    }

    public boolean isStatement() {
        return control != null;
    }

    public ArrayList<PDGNode> getIncomingEmptyNodes() {
        ArrayList<PDGNode> nodes = new ArrayList<>();
        for (PDGEdge e : inEdges)
            if (e.source.isEmptyNode())
                nodes.add(e.source);
        return nodes;
    }

    ArrayList<PDGEdge> getInEdgesForExasVectorization() {
        ArrayList<PDGEdge> edges = new ArrayList<>();
        for (PDGEdge e : inEdges)
            if (!(e instanceof PDGDataEdge) || ((PDGDataEdge) e).type != PDGDataEdge.Type.DEPENDENCE)
                edges.add(e);
        return edges;
    }

    ArrayList<PDGEdge> getOutEdgesForExasVectorization() {
        ArrayList<PDGEdge> edges = new ArrayList<>();
        for (PDGEdge e : outEdges)
            if (!(e instanceof PDGDataEdge) || ((PDGDataEdge) e).type != PDGDataEdge.Type.DEPENDENCE)
                edges.add(e);
        return edges;
    }

    public boolean isEmptyNode() {
        return this instanceof PDGActionNode && ((PDGActionNode) this).name.equals("empty");
    }

    private void adjustControl(PDGNode empty, int index) {
        PDGControlEdge e = (PDGControlEdge) getInEdge(control);
        control.outEdges.remove(e);
        e.source = empty.control;
        empty.control.outEdges.add(index, e);
        e.label = empty.getInEdge(empty.control).getLabel();
        control = empty.control;
    }

    public PDGEdge getInEdge(PDGNode node) {
        for (PDGEdge e : inEdges)
            if (e.source == node)
                return e;
        return null;
    }

    public void adjustControl(PDGNode node, PDGNode empty) {
        int i = 0;
        while (outEdges.get(i).target != node) {
            i++;
        }
        int index = empty.control.getOutEdgeIndex(empty);
        while (i < outEdges.size() && !outEdges.get(i).target.isEmptyNode()) {
            index++;
            outEdges.get(i).target.adjustControl(empty, index);
        }
    }

    public ArrayList<PDGEdge> getInDependences() {
        ArrayList<PDGEdge> es = new ArrayList<>();
        for (PDGEdge e : inEdges)
            if (e instanceof PDGDataEdge && ((PDGDataEdge) e).type == PDGDataEdge.Type.DEPENDENCE)
                es.add(e);
        return es;
    }

    public int getOutEdgeIndex(PDGNode node) {
        int i = 0;
        while (i < outEdges.size()) {
            if (outEdges.get(i).target == node)
                return i;
            i++;
        }
        return -1;
    }

    public void addNeighbors(HashSet<PDGNode> nodes) {
        for (PDGEdge e : inEdges)
            if (!(e instanceof PDGDataEdge) || (((PDGDataEdge) e).type != PDGDataEdge.Type.DEPENDENCE && ((PDGDataEdge) e).type != PDGDataEdge.Type.REFERENCE)) {
                if (!e.source.isEmptyNode() && !nodes.contains(e.source)) {
                    nodes.add(e.source);
                    e.source.addNeighbors(nodes);
                }
            }
        for (PDGEdge e : outEdges)
            if (!(e instanceof PDGDataEdge) || (((PDGDataEdge) e).type != PDGDataEdge.Type.DEPENDENCE && ((PDGDataEdge) e).type != PDGDataEdge.Type.REFERENCE)) {
                if (!e.target.isEmptyNode() && !nodes.contains(e.target)) {
                    nodes.add(e.target);
                    e.target.addNeighbors(nodes);
                }
            }
    }

    public boolean isSame(PDGNode node) {
        if (key == null && node.key != null)
            return false;
        if (!key.equals(node.key))
            return false;
        if (this instanceof PDGActionNode)
            return ((PDGActionNode) this).isSame(node);
        if (this instanceof PDGDataNode)
            return ((PDGDataNode) this).isSame(node);
        if (this instanceof PDGControlNode)
            return ((PDGControlNode) this).isSame(node);
        return false;
    }

    public PDGNode getDefinition() {
        if (this instanceof PDGDataNode && this.inEdges.size() == 1 && this.inEdges.get(0) instanceof PDGDataEdge) {
            PDGDataEdge e = (PDGDataEdge) this.inEdges.get(0);
            if (e.type == PDGDataEdge.Type.REFERENCE)
                return e.source;
        }
        return null;
    }

    public ArrayList<PDGNode> getDefinitions() {
        ArrayList<PDGNode> defs = new ArrayList<>();
        if (this instanceof PDGDataNode) {
            for (PDGEdge e : this.inEdges) {
                if (e instanceof PDGDataEdge && ((PDGDataEdge) e).type == PDGDataEdge.Type.REFERENCE)
                    defs.add(e.source);
            }
        }
        return defs;
    }

    public ArrayList<PDGNode> getChangedDataNodes() {
        ArrayList<PDGNode> defs = new ArrayList<>();
        for (PDGEdge e : this.outEdges) {
            if (e instanceof PDGDataEdge && ((PDGDataEdge) e).type == PDGDataEdge.Type.DEFINITION)
                defs.add(e.source);
        }
        return defs;
    }

    public boolean hasInEdge(PDGNode node, String label) {
        for (PDGEdge e : inEdges)
            if (e.source == node && e.getLabel().equals(label))
                return true;
        return false;
    }

    public boolean hasInEdge(PDGEdge edge) {
        for (PDGEdge e : inEdges)
            if (e.source == edge.source && e.getLabel().equals(edge.getLabel()))
                return true;
        return false;
    }

    public boolean hasInNode(PDGNode preNode) {
        for (PDGEdge e : inEdges)
            if (e.source == preNode)
                return true;
        return false;
    }

    public boolean hasOutNode(PDGNode target) {
        for (PDGEdge e : outEdges)
            if (e.target == target)
                return true;
        return false;
    }

    public boolean isValid() {
        HashSet<PDGNode> s = new HashSet<>();
        for (PDGEdge e : outEdges) {
            if (e instanceof PDGDataEdge && ((PDGDataEdge) e).type == PDGDataEdge.Type.DEPENDENCE)
                continue;
            if (s.contains(e.target))
                return false;
            s.add(e.target);
        }
        return true;
    }

    public HashSet<PDGNode> getAllChildNodes(int depth,List<PDGNode> avoidCodeNode){
        if (depth==0){
            HashSet<PDGNode> inNodeList= new HashSet<>();
            inNodeList.add(this);
            return inNodeList;
        }
        depth--;
        HashSet<PDGNode> inNodeList= new HashSet<>();
        inNodeList.add(this);
        int finalDepth1 = depth;
        inEdges.stream().map(PDGEdge::getSource).filter(y->!avoidCodeNode.contains(y)).forEach(z->{
            avoidCodeNode.add(this);
            inNodeList.addAll(z.getAllChildNodes(finalDepth1,avoidCodeNode));
        });
        outEdges.stream().map(PDGEdge::getTarget).filter(y->!avoidCodeNode.contains(y)).forEach(z->{
            avoidCodeNode.add(this);
            inNodeList.addAll(z.getAllChildNodes(finalDepth1,avoidCodeNode));
        });
        return inNodeList;
    }

    public HashSet<PDGNode> getAllChildNodes(int depth){
        if (depth==0){return  new HashSet<>();}
        depth--;
        HashSet<PDGNode> inNodeList= new HashSet<>();
        inNodeList.add(this);
        int finalDepth = depth;
        inEdges.forEach(x->inNodeList.addAll(x.getSource().getAllChildNodes(finalDepth)));
        outEdges.forEach(x->inNodeList.addAll(x.getTarget().getAllChildNodes(finalDepth)));
        return inNodeList;
    }

    public abstract boolean  isEqualNodes(PDGNode node);

}
