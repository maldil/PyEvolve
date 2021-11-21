package com.matching.fgpdg;


import com.matching.fgpdg.nodes.*;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.arg;
import org.python.core.AstList;
import org.python.core.PyObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class PDGGraph implements Serializable {
    private static final long serialVersionUID = -5128703931982211886L;
    protected PDGDataNode[] parameters;
    protected PDGNode entryNode, endNode;
    protected HashSet<PDGNode> nodes = new HashSet<PDGNode>();
    protected HashSet<PDGNode> statementNodes = new HashSet<>();
    protected HashSet<PDGDataNode> dataSources = new HashSet<>();
    protected HashSet<PDGNode> statementSinks = new HashSet<>();
    protected HashSet<PDGNode> statementSources = new HashSet<>();
    protected HashSet<PDGNode> sinks = new HashSet<PDGNode>();
    protected HashSet<PDGNode> breaks = new HashSet<>();
    protected HashSet<PDGNode> returns = new HashSet<>();
    private PDGBuildingContext context;
    private HashMap<String, HashSet<PDGDataNode>> defStore = new HashMap<>();

    public HashSet<PDGNode> getNodes() {
        return nodes;
    }

    public PDGGraph(FunctionDef md, PDGBuildingContext context) {
        this.context = context;
        this.context.setMethod(md);
        int numOfParameters = md.getInternalArgs().getInternalArgs().size();
        parameters = new PDGDataNode[numOfParameters];
        entryNode = new PDGEntryNode(md, PyObject.FUNCTIONDEF, "START");
        nodes.add(entryNode);
        statementNodes.add(entryNode);
        for (int i = 0; i < md.getInternalArgs().getInternalArgs().size(); i++) {
            arg arg = md.getInternalArgs().getInternalArgs().get(i);
            String id = arg.getInternalArg();
            //context.addLocalVariable(id, "" + d.getStartPosition());
            mergeSequential(buildPDG(entryNode, "", arg));
            String[] info = context.getLocalVariableInfo(id);  //TODO handel the Type information
            this.parameters[numOfParameters++] = new PDGDataNode(
                    arg, PyObject.NAME, info[0], info[1],
                    "PARAM_" + arg.getInternalArg(), false, true);
        }
        context.pushTry();
        if (((AstList) md.getBody()).size() != 0) {
            AstList body = (AstList) md.getBody();
            mergeSequential(buildPDG(entryNode, "", body));
        }
        adjustReturnNodes();
        adjustControlEdges();
        context.removeScope();
    }

    public PDGGraph(PDGBuildingContext context, PDGNode node) {
        this(context);
        init(node);
    }

    private void init(PDGNode node) {
        if (node instanceof PDGDataNode && !node.isLiteral())
            dataSources.add((PDGDataNode) node);
        sinks.add(node);
        nodes.add(node);
        if (node.isStatement()) {
            statementNodes.add(node);
            statementSources.add(node);
            statementSinks.add(node);
        }
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              arg astNode) {
        String name = astNode.getInternalArg();
        String type = TypeWrapper.getTypeInfo(astNode.getLine(),astNode.getCharPositionInLine(),name);
        context.addLocalVariable(name, "" + astNode.getCharStartIndex(), type);
        PDGDataNode node = new PDGDataNode(astNode, astNode.getNodeType(),
                "" + astNode.getCharPositionInLine(), type,
                name, false, true);
        PDGGraph pdg = new PDGGraph(context, new PDGDataNode(null, PyObject.NULL_LITERAL, "null", "", "null"));
        pdg.mergeSequentialData(new PDGActionNode(control, branch,
                astNode, PyObject.ASSIGN, null, null, "="), PDGDataEdge.Type.PARAMETER);
        pdg.mergeSequentialData(node, PDGDataEdge.Type.DEFINITION);
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              AstList body) {
        if (body.size() > 0 && body.size() <= 100) {
            context.addScope();
            PDGGraph pdg1;
            ArrayList<PDGGraph> pdgs = new ArrayList<>();
            for (int i = 0; i < body.size(); i++) {
//                if (body.get(i) instanceof expr) continue; TODO implement
                PDGGraph pdg = buildPDG(control, branch, (PyObject) body.get(i));
                if (!pdg.isEmpty())
                    pdgs.add(pdg);
            }
            int s = 0;
            for (int i = 1; i < pdgs.size(); i++)
                if (pdgs.get(s).statementNodes.isEmpty())
                    s = i;
                else
                    pdgs.get(s).mergeSequential(pdgs.get(i));
            if (s == pdgs.size())
                return new PDGGraph(context);
            pdg1= pdgs.get(s);

            context.removeScope();
            return pdg1;
        }
        return new PDGGraph(context);
    }

    private boolean isEmpty() {
        return nodes.isEmpty();
    }

    private PDGGraph buildPDG(PDGNode control, String branch, PyObject node) {

        return new PDGGraph(context);
    }
    private void updateDefStore(HashMap<String, HashSet<PDGDataNode>> store) {
        update(defStore, store);
    }

    private static <E> void update(HashMap<String, HashSet<E>> target,
                                   HashMap<String, HashSet<E>> source) {
        for (String key : source.keySet()) {
            HashSet<E> s = source.get(key);
            if (s.contains(null)) {
                if (target.containsKey(key)) {
                    s.remove(null);
                    target.get(key).addAll(new HashSet<E>(s));
                } else
                    target.put(key, new HashSet<E>(s));
            } else
                target.put(key, new HashSet<E>(s));
        }
    }

    private void mergeSequential(PDGGraph pdg) {
        if (pdg.statementNodes.isEmpty())
            return;
        for (PDGDataNode source : new HashSet<>(pdg.dataSources)) {
            HashSet<PDGDataNode> defs = defStore.get(source.getKey());
            if (defs != null) {
                for (PDGDataNode def : defs)
                    if (def != null)
                        new PDGDataEdge(def, source, PDGDataEdge.Type.REFERENCE);
                if (!defs.contains(null))
                    pdg.dataSources.remove(source);
            }
        }
        updateDefStore(pdg.defStore);
        for (PDGNode sink : statementSinks) {
            for (PDGNode source : pdg.statementSources) {
                new PDGDataEdge(sink, source, PDGDataEdge.Type.DEPENDENCE);
            }
        }
        this.dataSources.addAll(pdg.dataSources);
        this.sinks.clear();
        this.statementSinks.clear();
        this.statementSinks.addAll(pdg.statementSinks);
        this.nodes.addAll(pdg.nodes);
        this.statementNodes.addAll(pdg.statementNodes);
        this.breaks.addAll(pdg.breaks);
        this.returns.addAll(pdg.returns);
        pdg.clear();
    }

    private void mergeSequentialData(PDGNode next, PDGDataEdge.Type type) {
        if (next.isStatement())
            for (PDGNode sink : statementSinks) {
                new PDGDataEdge(sink, next, PDGDataEdge.Type.DEPENDENCE);
            }
        if (type == PDGDataEdge.Type.DEFINITION) {
            HashSet<PDGDataNode> ns = new HashSet<>();
            ns.add((PDGDataNode) next);
            defStore.put(next.getKey(), ns);
        } else if (type == PDGDataEdge.Type.QUALIFIER) {
            dataSources.add((PDGDataNode) next);
        } else if (type != PDGDataEdge.Type.REFERENCE && next instanceof PDGDataNode) {
            HashSet<PDGDataNode> ns = defStore.get(next.getKey());
            if (ns != null)
                for (PDGDataNode def : ns)
                    new PDGDataEdge(def, next, PDGDataEdge.Type.REFERENCE);
        }
        for (PDGNode node : sinks)
            new PDGDataEdge(node, next, type);
        sinks.clear();
        sinks.add(next);
        if (nodes.isEmpty() && next instanceof PDGDataNode)
            dataSources.add((PDGDataNode) next);
        nodes.add(next);
        if (next.isStatement()) {
            statementNodes.add(next);
            if (statementSources.isEmpty())
                statementSources.add(next);
            statementSinks.clear();
            statementSinks.add(next);
        }
    }

    private void clear() {
        nodes.clear();
        statementNodes.clear();
        dataSources.clear();
        statementSources.clear();
        sinks.clear();
        statementSinks.clear();
        breaks.clear();
        returns.clear();
        clearDefStore();
    }

    private void adjustReturnNodes() {
        sinks.addAll(returns);
        statementSinks.addAll(returns);
        returns.clear();
        endNode = new PDGEntryNode(null, PyObject.FUNCTIONDEF, "END");
        for (PDGNode sink : statementSinks)
            new PDGDataEdge(sink, endNode, PDGDataEdge.Type.DEPENDENCE);
        sinks.clear();
        statementSinks.clear();
        nodes.add(endNode);
        statementNodes.remove(entryNode);
    }

    private void adjustControlEdges() {
        for (PDGNode node : statementNodes) {
            ArrayList<PDGNode> ens = node.getIncomingEmptyNodes();
            if (ens.size() == 1 && node.getInDependences().size() == 1) {
                PDGNode en = ens.get(0);
                if (node.getControl() != en.getControl()) {
                    node.getControl().adjustControl(node, en);
                }
            }
        }
    }

    private void clearDefStore() {
        clear(defStore);
    }

    private static <E> void clear(HashMap<String, HashSet<E>> map) {
        for (String key : map.keySet())
            map.get(key).clear();
        map.clear();
    }

    public PDGGraph(PDGBuildingContext context) {
        this.context = context;
    }
}
