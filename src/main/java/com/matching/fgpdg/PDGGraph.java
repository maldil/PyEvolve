package com.matching.fgpdg;

import com.matching.fgpdg.nodes.*;
import com.matching.fgpdg.nodes.ast.AlphanumericHole;
import com.matching.fgpdg.nodes.ast.LazyHole;
import com.utils.Assertions;


import org.python.antlr.ast.*;
import org.python.antlr.ast.List;
import org.python.antlr.ast.Module;
import org.python.antlr.base.excepthandler;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;
import org.python.core.AstList;
import org.python.core.PyObject;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static com.matching.fgpdg.nodes.PDGDataEdge.Type.*;

public class PDGGraph implements Serializable {
    private static final long serialVersionUID = -5128703931982211886L;
    protected PDGDataNode[] parameters;
    protected PDGNode entryNode, endNode;
    protected HashSet<PDGNode> nodes = new HashSet<PDGNode>();
    protected HashSet<PDGNode> statementNodes = new HashSet<>();
    protected HashSet<PDGDataNode> dataSources = new HashSet<>();
    protected HashSet<PDGHoleNode> holeDataSources = new HashSet<>();
    protected HashSet<PDGNode> statementSinks = new HashSet<>();
    protected HashSet<PDGNode> statementSources = new HashSet<>();
    protected HashSet<PDGNode> sinks = new HashSet<PDGNode>();
    protected HashSet<PDGNode> breaks = new HashSet<>();
    protected HashSet<PDGNode> returns = new HashSet<>();
    protected HashSet<PDGNode> changedNodes = new HashSet<>();
    private PDGBuildingContext context;
    private HashMap<String, HashSet<PDGDataNode>> defStore = new HashMap<>();
    private HashMap<String, HashSet<PDGHoleNode>> defHoleStore = new HashMap<>();
    private HashMap<Integer, PDGNode> idPDG = new HashMap<>();

    public PDGGraph(FunctionDef md, PDGBuildingContext context) {
        this.context = context;
        context.addScope();
        this.context.setMethod(md);
        int numOfParameters = 0;
        parameters = new PDGDataNode[md.getInternalArgs().getInternalArgs().size()];

        entryNode = new PDGEntryNode(md, PyObject.FUNCTIONDEF, "START");
        nodes.add(entryNode);
        statementNodes.add(entryNode);
        for (int i = 0; i < md.getInternalArgs().getInternalArgs().size(); i++) {
            arg arg = md.getInternalArgs().getInternalArgs().get(i);
            String id = arg.getInternalArg();
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

    public PDGNode getPDGNode(int id) {
        if (idPDG.size() == 0) {
            for (PDGNode node : nodes) {
                idPDG.put(node.getId(), node);
            }
        }
        return idPDG.get(id);
    }

    public PDGGraph(Module md, PDGBuildingContext context) {
        this.context = context;
        context.addScope();
        entryNode = new PDGEntryNode(md, PyObject.MODULE, "START");
        nodes.add(entryNode);
        statementNodes.add(entryNode);
        for (stmt stmt : md.getInternalBody()) {
            if (stmt instanceof ImportFrom || stmt instanceof Import)
                continue;
            mergeSequential(Objects.requireNonNull(buildPDG(entryNode, "", stmt)));
        }
        adjustReturnNodes();
        adjustControlEdges();
        HashSet<PDGNode> toRemove = new HashSet<PDGNode>();
        for (PDGNode node : nodes) {
            if (node instanceof PDGEntryNode && node.getLabel().equals("START")) {
                for (PDGEdge edge : node.getOutEdges()) {
                    edge.getTarget().getInEdges().remove(edge);
                }
                toRemove.add(node);
            } else if (node instanceof PDGEntryNode && node.getLabel().equals("END")) {
                for (PDGEdge edge : node.getInEdges()) {
                    edge.getSource().getOutEdges().remove(edge);
                }
                toRemove.add(node);
            }
        }
        toRemove.forEach(x -> nodes.remove(x));

        context.removeScope();

    }

    public PDGGraph(PDGBuildingContext context, PDGNode node) {
        this(context);
        init(node);
    }

    public PDGGraph(PDGBuildingContext context) {
        this.context = context;
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

    private static <E> void clear(HashMap<String, HashSet<E>> map) {
        for (String key : map.keySet())
            map.get(key).clear();
        map.clear();
    }

    private static <E> void add(HashMap<String, HashSet<E>> target,
                                HashMap<String, HashSet<E>> source) {
        for (String key : source.keySet())
            if (target.containsKey(key))
                target.get(key).addAll(new HashSet<E>(source.get(key)));
            else
                target.put(key, new HashSet<E>(source.get(key)));
    }

    public HashSet<PDGNode> getNodes() {
        return nodes;
    }

    private void init(PDGNode node) {
        if (node instanceof PDGDataNode && !node.isLiteral())
            dataSources.add((PDGDataNode) node);
        if (node instanceof PDGHoleNode)
            holeDataSources.add((PDGHoleNode) node);
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
        String type = context.getTypeWrapper().getTypeInfo(astNode.getLine(), ((arg) astNode).getCharPositionInLine());
        if (type == null)
            type = context.getTypeWrapper().getTypeInfo(name);
        context.addLocalVariable(name, "" + astNode.getCharStartIndex(), type);
        PDGDataNode node = new PDGDataNode(astNode, astNode.getNodeType(),
                "" + astNode.getCharPositionInLine(), type,
                name, false, true);
        PDGGraph pdg = new PDGGraph(context, new PDGDataNode(null, PyObject.NULL_LITERAL, "null", "", "null"));
        pdg.mergeSequentialData(new PDGActionNode(control, branch,
                astNode, PyObject.ASSIGN, null, null, "="), PARAMETER);
        pdg.mergeSequentialData(node, PDGDataEdge.Type.DEFINITION);
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Starred astNode) {

        PDGGraph pdgGraph = buildArgumentPDG(control, branch, astNode.getInternalValue());
//        String name = ((Name)astNode.getInternalValue()).getInternalId();
//        String type = context.getTypeWrapper().getTypeInfo(astNode.getLine(), (astNode).getCharPositionInLine());
//        if (type == null)
//            type = context.getTypeWrapper().getTypeInfo(name);
//        context.addLocalVariable(name, "" + astNode.getCharStartIndex(), type);
        PDGDataNode node = new PDGDataNode(astNode, astNode.getNodeType(),
                "" + astNode.getCharPositionInLine(), "",
                "", false, true);
//        PDGGraph pdg = new PDGGraph(context, new PDGDataNode(null, PyObject.NULL_LITERAL, "null", "", "null"));
//        pdg.mergeSequentialData(new PDGActionNode(control, branch,
//                astNode, PyObject.ASSIGN, null, null, "="), PARAMETER);
        pdgGraph.mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
        return pdgGraph;
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
            for (int i = 1; i < pdgs.size(); i++) {
                if (pdgs.get(s).statementNodes.isEmpty())
                    s = i;
                else
                    pdgs.get(s).mergeSequential(pdgs.get(i));
            }
            if (s == pdgs.size())
                return new PDGGraph(context);
            pdg1 = pdgs.get(s);
            context.removeScope();
            return pdg1;
        }
        return new PDGGraph(context);
    }

    private boolean isEmpty() {
        return nodes.isEmpty();
    }

    private void updateDefStore(HashMap<String, HashSet<PDGDataNode>> store) {
        update(defStore, store);
    }

    private void updateHoleDefStore(HashMap<String, HashSet<PDGHoleNode>> store) {
        update(defHoleStore, store);
    }

    private void mergeSequential(PDGGraph pdg) {
        if (pdg.statementNodes.isEmpty())
            return;
        for (PDGDataNode source : new HashSet<>(pdg.dataSources)) {
            HashSet<PDGDataNode> defs = defStore.get(source.getKey());
            if (defs != null) {
                for (PDGDataNode def : defs)
                    if (def != null)
                        new PDGDataEdge(def, source, REFERENCE);
                if (!defs.contains(null))
                    pdg.dataSources.remove(source);
            }
        }
        for (PDGHoleNode source : new HashSet<>(pdg.holeDataSources)) {
            HashSet<PDGHoleNode> defs = defHoleStore.get(source.getKey());
            if (defs != null) {
                for (PDGHoleNode def : defs)
                    if (def != null)
                        new PDGDataEdge(def, source, REFERENCE);
                if (!defs.contains(null))
                    pdg.holeDataSources.remove(source);
            }
        }


        for (Map.Entry<String, HashSet<PDGDataNode>> sourcedefinitions : pdg.defStore.entrySet()) {
            for (PDGDataNode sourceNode : sourcedefinitions.getValue()) {
                if (defStore.get(sourcedefinitions.getKey()) != null) {
                    for (PDGDataNode dataNode : defStore.get(sourcedefinitions.getKey())) {
                        if (sourceNode != null && dataNode != null) {
                            new PDGDataEdge(dataNode, sourceNode, RE_DEFINITION);
                        }

                    }
                }

            }
        }

        for (Map.Entry<String, HashSet<PDGHoleNode>> sourcedefinitions : pdg.defHoleStore.entrySet()) {
            for (PDGHoleNode sourceNode : sourcedefinitions.getValue()) {
                if (defHoleStore.get(sourcedefinitions.getKey()) != null) {
                    for (PDGHoleNode dataNode : defHoleStore.get(sourcedefinitions.getKey())) {
                        if (sourceNode != null && dataNode != null) {
                            new PDGDataEdge(dataNode, sourceNode, RE_DEFINITION);
                        }

                    }
                }

            }
        }

        updateDefStore(pdg.defStore);
        updateHoleDefStore(pdg.defHoleStore);
        for (PDGNode sink : statementSinks) {
            for (PDGNode source : pdg.statementSources) {
                new PDGDataEdge(sink, source, PDGDataEdge.Type.DEPENDENCE);
            }
        }


        this.dataSources.addAll(pdg.dataSources);
        this.holeDataSources.addAll(pdg.holeDataSources);
        this.sinks.clear();
        this.statementSinks.clear();
        this.statementSinks.addAll(pdg.statementSinks);
        this.nodes.addAll(pdg.nodes);
        this.statementNodes.addAll(pdg.statementNodes);
        this.breaks.addAll(pdg.breaks);
        this.returns.addAll(pdg.returns);
        pdg.clear();
    }

    private void mergeSequentialHoleData(PDGHoleNode next, PDGDataEdge.Type type) {
        if (next.isStatement())
            for (PDGNode sink : statementSinks) {
                new PDGDataEdge(sink, next, PDGDataEdge.Type.DEPENDENCE);
            }
        if (type == PDGDataEdge.Type.DEFINITION) {
            HashSet<PDGHoleNode> ns = new HashSet<>();
            ns.add(next);
            defHoleStore.put(next.getKey(), ns);
        } else if (type == QUALIFIER) {
            holeDataSources.add((PDGHoleNode) next);
        } else if (type != REFERENCE && next.isDataNode()) {
            HashSet<PDGHoleNode> ns = defHoleStore.get(next.getKey());
            if (ns != null)
                for (PDGHoleNode def : ns) {
                    if (def.isDataNode())
                        new PDGDataEdge(def, next, REFERENCE);
                }
        }
        for (PDGNode node : sinks)
            new PDGDataEdge(node, next, type);
        sinks.clear();
        sinks.add(next);
        if (nodes.isEmpty() && next.isDataNode())
            holeDataSources.add(next);
        nodes.add(next);
        if (next.isStatement()) {
            statementNodes.add(next);
            if (statementSources.isEmpty())
                statementSources.add(next);
            statementSinks.clear();
            statementSinks.add(next);
        }

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
        } else if (type == QUALIFIER) {
            dataSources.add((PDGDataNode) next);
        } else if (type != REFERENCE && next instanceof PDGDataNode) {
            HashSet<PDGDataNode> ns = defStore.get(next.getKey());
            if (ns != null)
                for (PDGDataNode def : ns)
                    new PDGDataEdge(def, next, REFERENCE);
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

    private void mergeSequentialDataNoUpdatetoSinks(PDGNode next, PDGDataEdge.Type type) {
        if (next.isStatement())
            for (PDGNode sink : statementSinks) {
                new PDGDataEdge(sink, next, PDGDataEdge.Type.DEPENDENCE);
            }
        if (type == PDGDataEdge.Type.DEFINITION) {
            HashSet<PDGDataNode> ns = new HashSet<>();
            ns.add((PDGDataNode) next);
            defStore.put(next.getKey(), ns);
        } else if (type == QUALIFIER) {
            dataSources.add((PDGDataNode) next);
        } else if (type != REFERENCE && next instanceof PDGDataNode) {
            HashSet<PDGDataNode> ns = defStore.get(next.getKey());
            if (ns != null)
                for (PDGDataNode def : ns)
                    new PDGDataEdge(def, next, REFERENCE);
        }
        for (PDGNode node : sinks)
            new PDGDataEdge(node, next, type);
        if (nodes.isEmpty() && next instanceof PDGDataNode)
            dataSources.add((PDGDataNode) next);
        nodes.add(next);
        if (next.isStatement()) {
            statementNodes.add(next);
            if (statementSources.isEmpty())
                statementSources.add(next);
            statementSinks.add(next);
        }
    }

    private void mergeBranches(PDGGraph... pdgs) {
        HashMap<String, HashSet<PDGDataNode>> defStore = new HashMap<>();
        HashMap<String, Integer> defCounts = new HashMap<>();
        sinks.clear();
        statementSinks.clear();
        for (PDGGraph pdg : pdgs) {
            nodes.addAll(pdg.nodes);
            statementNodes.addAll(pdg.statementNodes);
            sinks.addAll(pdg.sinks);
            statementSinks.addAll(pdg.statementSinks);
            for (PDGDataNode source : new HashSet<PDGDataNode>(pdg.dataSources)) {
                HashSet<PDGDataNode> defs = this.defStore.get(source.getKey());
                if (defs != null) {
                    for (PDGDataNode def : defs)
                        if (def != null)
                            new PDGDataEdge(def, source, REFERENCE);
                    if (!defs.contains(null))
                        pdg.dataSources.remove(source);
                }
            }
            for (PDGHoleNode source : new HashSet<PDGHoleNode>(pdg.holeDataSources)) {
                HashSet<PDGHoleNode> defs = this.defHoleStore.get(source.getKey());
                if (defs != null) {
                    for (PDGHoleNode def : defs)
                        if (def != null && def.isDataNode())
                            new PDGDataEdge(def, source, REFERENCE);
                    if (!defs.contains(null))
                        pdg.holeDataSources.remove(source);
                }
            }
            holeDataSources.addAll(pdg.holeDataSources);
            dataSources.addAll(pdg.dataSources);
            // statementSources.addAll(pdg.statementSources);
            breaks.addAll(pdg.breaks);
            returns.addAll(pdg.returns);
        }
        for (PDGGraph pdg : pdgs) {
            HashMap<String, HashSet<PDGDataNode>> localStore = copyDefStore();
            updateDefStore(localStore, defCounts, pdg.defStore);
            add(defStore, localStore);
            pdg.clear();
        }
        for (String key : defCounts.keySet())
            if (defCounts.get(key) < pdgs.length)
                defStore.get(key).add(null);
        clearDefStore();
        this.defStore = defStore;
    }

    private void clear() {
        nodes.clear();
        statementNodes.clear();
        dataSources.clear();
        holeDataSources.clear();
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

    private ArrayList<PDGDataNode> getDefinitions() {
        ArrayList<PDGDataNode> defs = new ArrayList<>();
        for (PDGNode node : sinks)
            if (node instanceof PDGDataNode && node.isDefinition())
                defs.add((PDGDataNode) node);
        return defs;
    }

    private ArrayList<PDGHoleNode> getHoleDefinitions() {
        ArrayList<PDGHoleNode> defs = new ArrayList<>();
        for (PDGNode node : sinks)
            if (node instanceof PDGHoleNode && node.isDefinition())
                defs.add((PDGHoleNode) node);
        return defs;
    }

    private ArrayList<PDGActionNode> getReturns() {
        ArrayList<PDGActionNode> nodes = new ArrayList<>();
        for (PDGNode node : statementSinks)
            if (node.getAstNodeType() == PyObject.RETURN)
                nodes.add((PDGActionNode) node);
        return nodes;
    }

    private PDGGraph buildArgumentPDG(PDGNode control, String branch,
                                      PyObject exp) {
        PDGGraph pdg = buildPDG(control, branch, exp);
        if (pdg ==null){
            System.out.println();
            buildPDG(control, branch, exp);
        }
        if (pdg.isEmpty())
            return pdg;
        if (pdg.nodes.size() == 1)
            for (PDGNode node : pdg.nodes)
                if (node instanceof PDGDataNode)
                    return pdg;
                else if (node instanceof PDGAlphHole && ((PDGAlphHole) node).isDataNode()){
                    return pdg;
                }
        ArrayList<PDGDataNode> defs = pdg.getDefinitions();
        if (!defs.isEmpty()) {
            PDGDataNode def = defs.get(0);
            pdg.mergeSequentialData(new PDGDataNode(null, def.getAstNodeType(), def.getKey(),
                    ((PDGDataNode) def).getDataType(), ((PDGDataNode) def).getDataName(),
                    def.isField(), false), REFERENCE);
            return pdg;
        }
        ArrayList<PDGActionNode> rets = pdg.getReturns();
        if (rets.size() > 0) {
            int startChar = 0;
            int length = 0;
            if (exp instanceof expr) {
                startChar = ((expr) exp).getCharStartIndex();
                length = ((expr) exp).getCharStopIndex() - ((expr) exp).getCharStartIndex();
            } else if (exp instanceof stmt) {
                startChar = ((stmt) exp).getCharStartIndex();
                length = ((stmt) exp).getCharStopIndex() - ((stmt) exp).getCharStartIndex();
            } else {
                Assertions.UNREACHABLE();
            }
            PDGDataNode dummy = new PDGDataNode(null, PyObject.NAME,
                    PDGNode.PREFIX_DUMMY + startChar + "_"
                            + length, rets.get(0).getDataType(), PDGNode.PREFIX_DUMMY, false, true);
            for (PDGActionNode ret : rets) {
                ret.setAstNodeType(PyObject.ASSIGN);
                ret.setName("=");
                pdg.extend(ret, new PDGDataNode(dummy), PDGDataEdge.Type.DEFINITION);
            }
            pdg.mergeSequentialData(new PDGDataNode(null, dummy.getAstNodeType(),
                    dummy.getKey(), dummy.getDataType(), dummy.getDataName()), REFERENCE);
            return pdg;
        }
        PDGNode node = pdg.getOnlyOut();
        if (node instanceof PDGDataNode)
            return pdg;
//        int startChar = 0;
//        int length = 0;
//        if (exp instanceof expr) {
//            startChar = ((expr) exp).getCharStartIndex();
//            length = ((expr) exp).getCharStopIndex() - ((expr) exp).getCharStartIndex();
//        } else if (exp instanceof stmt) {
//            startChar = ((stmt) exp).getCharStartIndex();
//            length = ((stmt) exp).getCharStopIndex() - ((stmt) exp).getCharStartIndex();
//        }
//        else if (exp instanceof Index){
//            startChar = ((Index) exp).getCharStartIndex();
//            length = ((Index) exp).getCharStopIndex() - ((Index) exp).getCharStartIndex();
//        }
//        else {
//            Assertions.UNREACHABLE();
//        }
//        PDGDataNode dummy = new PDGDataNode(null, PyObject.NAME,
//                PDGNode.PREFIX_DUMMY + startChar + "_"
//                        + length, node.getDataType(), PDGNode.PREFIX_DUMMY, false, true);
//        pdg.mergeSequentialData(new PDGActionNode(control, branch,
//                null, PyObject.ASSIGN, null, null, "="), PARAMETER);
//        pdg.mergeSequentialData(dummy, PDGDataEdge.Type.DEFINITION);
//        pdg.mergeSequentialData(new PDGDataNode(null, dummy.getAstNodeType(), dummy.getKey(),
//                dummy.getDataType(), dummy.getDataName()), REFERENCE);
        return pdg;
    }

    private void clearDefStore() {
        clear(defStore);
    }

    private PDGGraph buildPDG(PDGNode control, String branch, PyObject node) {
        if (node instanceof For)
            return buildPDG(control, branch, (For) node);
        if (node instanceof Assign)
            return buildPDG(control, branch, (Assign) node);
        if (node instanceof AugAssign)
            return buildPDG(control, branch, (AugAssign) node);
        if (node instanceof Tuple)
            return buildPDG(control, branch, (Tuple) node);
        if (node instanceof Name)
            return buildPDG(control, branch, (Name) node);
        if (node instanceof Num)
            return buildPDG(control, branch, (Num) node);
        if (node instanceof AstList)
            return buildPDG(control, branch, (AstList) node);
        if (node instanceof Call)
            return buildPDG(control, branch, (Call) node);
        if (node instanceof Return)
            return buildPDG(control, branch, (Return) node);
        if (node instanceof Expr)
            return buildPDG(control, branch, (Expr) node);
        if (node instanceof BinOp)
            return buildPDG(control, branch, (BinOp) node);
        if (node instanceof Subscript)
            return buildPDG(control, branch, (Subscript) node);
        if (node instanceof Index)
            return buildPDG(control, branch, (Index) node);
        if (node instanceof arg)
            return buildPDG(control, branch, (arg) node);
        if (node instanceof Str)
            return buildPDG(control, branch, (Str) node);
        if (node instanceof Lambda)
            return buildPDG(control, branch, (Lambda) node);
        if (node instanceof With)
            return buildPDG(control, branch, (With) node);
        if (node instanceof withitem)
            return buildPDG(control, branch, (withitem) node);
        if (node instanceof If)
            return buildPDG(control, branch, (If) node);
        if (node instanceof Compare)
            return buildPDG(control, branch, (Compare) node);
        if (node instanceof List)
            return buildPDG(control, branch, (List) node);
        if (node instanceof ListComp)
            return buildPDG(control, branch, (ListComp) node);
        if (node instanceof comprehension)
            return buildPDG(control, branch, (comprehension) node);
        if (node instanceof SetComp)
            return buildPDG(control, branch, (SetComp) node);
        if (node instanceof GeneratorExp)
            return buildPDG(control, branch, (GeneratorExp) node);
        if (node instanceof TryExcept)
            return buildPDG(control, branch, (TryExcept) node);
        if (node instanceof ExceptHandler)
            return buildPDG(control, branch, (ExceptHandler) node);
        if (node instanceof While)
            return buildPDG(control, branch, (While) node);
        if (node instanceof Global)
            return buildPDG(control, branch, (Global) node);
        if (node instanceof Yield)
            return buildPDG(control, branch, (Yield) node);
        if (node instanceof Attribute)
            return buildPDG(control, branch, (Attribute) node);
        if (node instanceof Slice)
            return buildPDG(control, branch, (Slice) node);
        if (node instanceof Dict)
            return buildPDG(control, branch, (Dict) node);
        if (node instanceof UnaryOp)
            return buildPDG(control, branch, (UnaryOp) node);
        if (node instanceof ImportFrom)
            return buildPDG(control, branch, (ImportFrom) node);
        if (node instanceof Import)
            return buildPDG(control, branch, (Import) node);
        if (node instanceof BoolOp)
            return buildPDG(control, branch, (BoolOp) node);
        if (node instanceof Break)
            return buildPDG(control, branch, (Break) node);
        if (node instanceof AlphanumericHole)
            return buildPDG(control, branch, (AlphanumericHole) node);
        if (node instanceof LazyHole)
            return buildPDG(control, branch, (LazyHole) node);
        if (node instanceof ExtSlice)
            return buildPDG(control, branch, (ExtSlice) node);
        if (node instanceof Assert)
            return buildPDG(control, branch, (Assert) node);
        if (node instanceof FunctionDef)
            return buildPDG(control, branch, (FunctionDef) node);
        if (node instanceof IfExp)
            return buildPDG(control, branch, (IfExp) node);
        if (node instanceof Raise)
            return buildPDG(control, branch, (Raise) node);
        if (node instanceof ClassDef)
            return buildPDG(control, branch, (ClassDef) node);
        if (node instanceof TryFinally)
            return buildPDG(control, branch, (TryFinally) node);
        if (node instanceof DictComp)
            return buildPDG(control, branch, (DictComp) node);
        if (node instanceof Starred)
            return buildPDG(control, branch, (Starred) node);
        if (node instanceof Continue)
            return buildPDG(control, branch, (Continue) node);
        if (node instanceof Delete)
            return buildPDG(control, branch, (Delete) node);
        if (node instanceof Pass)
            return buildPDG(control, branch, (Pass) node);
        if (node instanceof org.python.antlr.ast.Set)
            return buildPDG(control, branch, (org.python.antlr.ast.Set) node);
        if (node instanceof Ellipsis)
            return buildPDG(control, branch, (Ellipsis) node);
        if (node instanceof Bytes)
            return buildPDG(control, branch, (Bytes) node);
        if (node instanceof Nonlocal)
            return buildPDG(control, branch, (Nonlocal) node);
        if (node instanceof YieldFrom)
            return buildPDG(control, branch, (YieldFrom) node);
        if (node instanceof ErrorExpr)
            return buildPDG(control, branch, (ErrorExpr) node);
        if (node instanceof ErrorStmt)
            return buildPDG(control, branch, (ErrorStmt) node);
        if (node instanceof AsyncFunctionDef)
            return buildPDG(control, branch, (AsyncFunctionDef) node);
        if (node ==null)
            return new PDGGraph(context);
        Assertions.UNREACHABLE(node.getClass().toString());
        return null;
    }

    private void mergeSequentialControl(PDGNode next, String label) {
        sinks.clear();
        sinks.add(next);
        statementSinks.clear();
        statementSinks.add(next);
        if (statementNodes.isEmpty())
            statementSources.add(next);
        nodes.add(next);
        statementNodes.add(next);
    }


    private void mergeSequentialControl(PDGGraph pdg) {
        if (pdg.statementNodes.isEmpty())
            return;
        if (this.statementNodes.isEmpty() || pdg.statementNodes.isEmpty()) {
            System.err.println("Merge an empty pdg.graph!!!");
            System.exit(-1);
        }
        this.sinks.clear();
        this.statementSinks.clear();
        this.statementSinks.addAll(pdg.statementSinks);
        this.nodes.addAll(pdg.nodes);
        this.statementNodes.addAll(pdg.statementNodes);
        this.breaks.addAll(pdg.breaks);
        this.returns.addAll(pdg.returns);
        pdg.clear();
    }

    private PDGNode getOnlyOut() {
        if (sinks.size() == 1)
            for (PDGNode n : sinks)
                return n;
//        throw new RuntimeException("ERROR in getting the only output node!!!");
//		System.err.println("ERROR in getting the only output node!!!");
//		System.exit(-1);
		return null;
    }

    private void extend(PDGNode ret, PDGDataNode node, PDGDataEdge.Type type) {
        HashSet<PDGDataNode> ns = new HashSet<>();
        ns.add((PDGDataNode) node);
        defStore.put(node.getKey(), ns);
        nodes.add(node);
        sinks.remove(ret);
        sinks.add(node);
        new PDGDataEdge(ret, node, type);
    }

    private void adjustBreakNodes(String id) {
        for (PDGNode node : new HashSet<PDGNode>(breaks)) {
            if ((node.getKey() == null && id == null) || node.getKey().equals(id)) {
                sinks.add(node);
                statementSinks.add(node);
                breaks.remove(node);
            }
        }
    }

    private HashMap<String, HashSet<PDGDataNode>> copyDefStore() {
        HashMap<String, HashSet<PDGDataNode>> store = new HashMap<>();
        for (String key : defStore.keySet())
            store.put(key, new HashSet<>(defStore.get(key)));
        return store;
    }

    private void mergeParallel(PDGGraph... pdgs) {
        HashMap<String, HashSet<PDGDataNode>> defStore = new HashMap<>();
        HashMap<String, Integer> defCounts = new HashMap<>();
        for (PDGGraph pdg : pdgs) {
            HashMap<String, HashSet<PDGDataNode>> localStore = copyDefStore();
            nodes.addAll(pdg.nodes);
            statementNodes.addAll(pdg.statementNodes);
            sinks.addAll(pdg.sinks);
            statementSinks.addAll(pdg.statementSinks);
            dataSources.addAll(pdg.dataSources);
            holeDataSources.addAll(pdg.holeDataSources);
            statementSources.addAll(pdg.statementSources);
            breaks.addAll(pdg.breaks);
            returns.addAll(pdg.returns);
            updateDefStore(localStore, defCounts, pdg.defStore);
            add(defStore, localStore);
            pdg.clear();
        }
        clearDefStore();
        this.defStore = defStore;
    }

    private void updateDefStore(HashMap<String, HashSet<PDGDataNode>> target,
                                HashMap<String, Integer> defCounts,
                                HashMap<String, HashSet<PDGDataNode>> source) {
        for (String key : source.keySet())
            target.put(key, new HashSet<>(source.get(key)));
        for (String key : target.keySet()) {
            int c = 1;
            if (defCounts.containsKey(key))
                c += defCounts.get(key);
            defCounts.put(key, c);
        }
    }

    private PDGDataNode getOnlyDataOut() {
        if (sinks.size() == 1)
            for (PDGNode n : sinks)
                if (n instanceof PDGDataNode)
                    return (PDGDataNode) n;
        System.err.println("ERROR in getting the only data output node!!!" + this.context.getFilePath());

        return null;
    }

    private PDGHoleNode getOnlyHoleDataOut() {
        if (sinks.size() == 1)
            for (PDGNode n : sinks)
                if (n instanceof PDGAlphHole && ((PDGHoleNode) n).isDataNode())
                    return (PDGAlphHole) n;
                else if (n instanceof PDGLazyHole && ((PDGLazyHole) n).isDataNode())
                    return (PDGLazyHole) n;
        System.err.println("ERROR in getting the only data output node!!!" + this.context.getFilePath());
        return null;
    }

    private void delete(PDGNode node) {
        if (statementSinks.contains(node))
            for (PDGEdge e : node.getInEdges())
                if (e instanceof PDGDataEdge) {
                    if (((PDGDataEdge) e).getType() == PDGDataEdge.Type.DEPENDENCE)
                        statementSinks.add(e.getSource());
                    else if (((PDGDataEdge) e).getType() == PARAMETER)
                        sinks.add(e.getSource());
                }
        if (sinks.contains(node) && node instanceof PDGDataNode) {
            for (PDGEdge e : node.getInEdges())
                if (e.getSource() instanceof PDGDataNode)
                    sinks.add(e.getSource());
        }
        if (statementSources.contains(node))
            for (PDGEdge e : node.getOutEdges())
                if (e instanceof PDGDataEdge
                        && ((PDGDataEdge) e).getType() == PDGDataEdge.Type.DEPENDENCE)
                    statementSources.add(e.getTarget());
        nodes.remove(node);
        changedNodes.remove(node);
        statementNodes.remove(node);
        dataSources.remove(node);
        holeDataSources.remove(node);
        statementSources.remove(node);
        sinks.remove(node);
        statementSinks.remove(node);
        node.delete();
    }


    private PDGGraph buildPDG(PDGNode control, String branch,
                              For astNode) {
        context.addScope();
        PDGGraph pdg = buildArgumentPDG(control, branch, astNode.getInternalIter());
//        pdg.mergeSequentialData(new PDGActionNode(control, branch,
//                astNode, ASTNode.ASSIGNMENT, null, null, "="), PARAMETER);
        if (astNode.getInternalTarget() instanceof Name) {
            String varName = ((Name) astNode.getInternalTarget()).getInternalId();
            String varType = context.getTypeWrapper().getTypeInfo(((Name) astNode.getInternalTarget()).getLineno(),
                    ((Name) astNode.getInternalTarget()).getCol_offset());

            if (varType == null) {
                varType = context.getTypeWrapper().getTypeInfo(varName);
            }
            if (varType == null) {
                varType = "Any";
            }

            context.addLocalVariable(varName, "" + astNode.getInternalTarget().getCharStartIndex(), varType);
            PDGDataNode varp = new PDGDataNode(astNode.getInternalTarget(), astNode.getInternalTarget().getNodeType(),
                    "" + astNode.getInternalTarget().getCharStartIndex(), varType,
                    varName, false, true);
            pdg.mergeSequentialData(varp, PDGDataEdge.Type.DEFINITION);
            pdg.mergeSequentialData(new PDGDataNode(null, varp.getAstNodeType(),
                    varp.getKey(), varp.getDataType(), varp.getDataName()), REFERENCE);
        }
        else if (astNode.getInternalTarget() instanceof List) {
            for (expr var : ((List) astNode.getInternalTarget()).getInternalElts()) {
                if (var instanceof Name) {
                    String name = ((Name) var).getInternalId();
                    String type = context.getTypeWrapper().getTypeInfo(((Name) var).getLineno(), ((Name) var).getCol_offset());
                    if (type == null) {
                        type = context.getTypeWrapper().getTypeInfo(name);
                    }
                    if (type == null) {
                        type = "Any";
                    }
                    context.addLocalVariable(name, "" + var.getCharStartIndex(), type);
                    PDGDataNode varNode = new PDGDataNode(var, var.getNodeType(),
                            "" + var.getCharStartIndex(), type,
                            name, false, true);
                    pdg.mergeSequentialData(varNode, PDGDataEdge.Type.DEFINITION);
                    pdg.mergeSequentialData(new PDGDataNode(null, varNode.getAstNodeType(),
                            varNode.getKey(), varNode.getDataType(), varNode.getDataName()), REFERENCE);
                } else if (var instanceof Tuple) {
                    //TODO impliment this
                }
            }

        }
        else if (astNode.getInternalTarget() instanceof Tuple) {
//            PDGGraph gt = buildArgumentPDG(control,branch,astNode.getInternalTarget());
//            pdg.mergeBranches(gt);
            for (expr var : ((Tuple) astNode.getInternalTarget()).getInternalElts()) {
                if (var instanceof Name) {
                    String name = ((Name) var).getInternalId();
                    String type = context.getTypeWrapper().getTypeInfo(((Name) var).getLineno(), ((Name) var).getCol_offset());
                    if (type == null) {
                        type = context.getTypeWrapper().getTypeInfo(name);
                    }
                    if (type == null) {
                        type = "Any";
                    }
                    context.addLocalVariable(name, "" + var.getCharStartIndex(), type);
                    PDGDataNode varNode = new PDGDataNode(var, var.getNodeType(),
                            "" + var.getCharStartIndex(), type,
                            name, false, true);
                    pdg.mergeSequentialData(varNode, PDGDataEdge.Type.DEFINITION);
                    pdg.mergeSequentialData(new PDGDataNode(null, varNode.getAstNodeType(),
                            varNode.getKey(), varNode.getDataType(), varNode.getDataName()), REFERENCE);
                } else if (var instanceof Tuple) {
                    //TODO impliment this
                }
            }
        } else if (astNode.getInternalTarget() instanceof Hole) {
            String varName = ":[[l" + ((Hole) astNode.getInternalTarget()).getN() + "]]";
            String varType = context.getTypeWrapper().getGuards().getTypeOfTemplateVariable(varName);

            String[] info = context.getLocalVariableInfo(varName);
            if (info == null && varType != null) {
                context.addLocalVariable(varName, "" + astNode.getInternalTarget().getCharStartIndex(), varType);
            }
            PDGHoleNode varp = null;
            if (astNode.getInternalTarget() instanceof AlphanumericHole) {
                varp = new PDGAlphHole(astNode.getInternalTarget(), astNode.getInternalTarget().getNodeType(), context.getTypeWrapper().getGuards().getValueOfTemplateVariable(varName),
                        "" + astNode.getInternalTarget().getCharStartIndex(), varType, varName, true, false, false);
                pdg.mergeSequentialHoleData(varp, PDGDataEdge.Type.DEFINITION);
                pdg.mergeSequentialHoleData(new PDGAlphHole(null, varp.getAstNodeType(), context.getTypeWrapper().getGuards().getValueOfTemplateVariable(varName),
                        varp.getKey(), varp.getDataType(), varp.getDataName(), true, false, false), REFERENCE);
            } else if (astNode.getInternalTarget() instanceof LazyHole) {
                varp = new PDGLazyHole(astNode.getInternalTarget(), astNode.getInternalTarget().getNodeType(), context.getTypeWrapper().getGuards().getValueOfTemplateVariable(varName),
                        "" + astNode.getInternalTarget().getCharStartIndex(), varType, varName, true, false, false);
                pdg.mergeSequentialHoleData(varp, PDGDataEdge.Type.DEFINITION);
                pdg.mergeSequentialHoleData(new PDGLazyHole(null, varp.getAstNodeType(), context.getTypeWrapper().getGuards().getValueOfTemplateVariable(varName),
                        varp.getKey(), varp.getDataType(), varp.getDataName(), true, false, false), REFERENCE);
            } else {
                Assertions.UNREACHABLE();
            }
        }
        else if (astNode.getInternalTarget() instanceof Attribute){
//            TODO impliment this case
        }
        else if (astNode.getInternalTarget() instanceof Subscript){
//            TODO impliment this case
        }
        else {
            System.out.println(astNode.getInternalTarget().getClass());
            Assertions.UNREACHABLE();
        }
        PDGControlNode node = new PDGControlNode(control, branch, astNode, astNode.getNodeType());
        pdg.mergeSequentialData(node, PDGDataEdge.Type.CONDITION);
        pdg.mergeSequentialControl(new PDGActionNode(node, "",
                null, PyObject.EMPTY_STATEMENT, null, null, "empty"), "");
        PDGGraph bg = buildPDG(node, "", astNode.getBody());
        if (!bg.isEmpty())
            pdg.mergeSequential(bg);
        pdg.adjustBreakNodes("");
        context.removeScope();
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Assign astNode) {
        PDGGraph lg;
        ArrayList<PDGNode> ldatanodes = new ArrayList<>();
        if (astNode.getInternalTargets().size() > 1) {
            PDGGraph[] pgs = new PDGGraph[astNode.getInternalTargets().size()];
            for (int i = 0; i < astNode.getInternalTargets().size(); i++) {
                if (astNode.getInternalTargets().get(i) instanceof Name &&
                        context.getTypeWrapper().getTypeInfo(astNode.getInternalTargets().get(i).getLine(),
                                astNode.getInternalTargets().get(i).getCharPositionInLine(), ((Name) astNode.getInternalTargets().get(i)).getInternalId()) != null
                ) {
                    String[] info = context.getLocalVariableInfo(((Name) astNode.getInternalTargets().get(i)).getInternalId());
                    if (info == null) {
                        context.addLocalVariable
                                (((Name) astNode.getInternalTargets().get(i)).getInternalId(), "" +
                                                astNode.getInternalTargets().get(i).getCharStartIndex(),
                                        context.getTypeWrapper().getTypeInfo(astNode.getInternalTargets().get(i).getLine(),
                                                astNode.getInternalTargets().get(i).getCharPositionInLine(), ((Name) astNode.getInternalTargets().get(i)).getInternalId()));
                    } else if (!info[1].equals(context.getTypeWrapper().getTypeInfo(astNode.getInternalTargets().get(i).getLine(),
                            astNode.getInternalTargets().get(i).getCharPositionInLine(), ((Name) astNode.getInternalTargets().get(i)).getInternalId()))) {
                        context.updateTypeOfVariable(((Name) astNode.getInternalTargets().get(i)).getInternalId(),
                                context.getTypeWrapper().getTypeInfo(astNode.getInternalTargets().get(i).getLine(),
                                        astNode.getInternalTargets().get(i).getCharPositionInLine(), ((Name) astNode.getInternalTargets().get(i)).getInternalId()));
                    }
                } else if (astNode.getInternalTargets().get(i) instanceof Hole && context.getTypeWrapper().getGuards().getTypes().get(":[[l" + ((Hole) astNode.getInternalTargets().get(i)).getN() + "]]") != null) {
                    String[] info = context.getLocalVariableInfo(":[[l" + ((Hole) astNode.getInternalTargets().get(i)).getN() + "]]");
                    if (info == null) {
                        context.addLocalVariable(":[[l" + ((Hole) astNode.getInternalTargets().get(i)).getN() + "]]",
                                ""+astNode.getInternalTargets().get(i).getCharStartIndex(), context.getTypeWrapper().getGuards().getTypes().get(":[[l" + ((Hole) astNode.getInternalTargets().get(i)).getN() + "]]").snd);

                    }
                }
                pgs[i] = buildArgumentPDG(control, branch, astNode.getInternalTargets().get(i));
                ldatanodes.add(pgs[i].getOnlyDataOut());
            }
            lg = new PDGGraph(context);
            lg.mergeParallel(pgs);
        } else {
            if (astNode.getInternalTargets().get(0) instanceof Name &&
                    context.getTypeWrapper().getTypeInfo(astNode.getInternalTargets().get(0).getLine(),
                            astNode.getInternalTargets().get(0).getCharPositionInLine(),
                            ((Name) astNode.getInternalTargets().get(0)).getInternalId()) != null) {

                String[] info = context.getLocalVariableInfo((((Name) astNode.getInternalTargets().get(0)).getInternalId()));
                if (info == null) {
                    context.addLocalVariable
                            (((Name) astNode.getInternalTargets().get(0)).getInternalId(), "" +
                                            astNode.getInternalTargets().get(0).getCharStartIndex(),
                                    context.getTypeWrapper().getTypeInfo(astNode.getInternalTargets().get(0).getLine(),
                                            astNode.getInternalTargets().get(0).getCharPositionInLine(), ((Name) astNode.getInternalTargets().get(0)).getInternalId()));

                } else if (!info[1].equals(context.getTypeWrapper().getTypeInfo(astNode.getInternalTargets().get(0).getLine(),
                        astNode.getInternalTargets().get(0).getCharPositionInLine(), ((Name) astNode.getInternalTargets().get(0)).getInternalId()))) {
                    context.updateTypeOfVariable(((Name) astNode.getInternalTargets().get(0)).getInternalId(),
                            context.getTypeWrapper().getTypeInfo(astNode.getInternalTargets().get(0).getLine(),
                                    astNode.getInternalTargets().get(0).getCharPositionInLine(), ((Name) astNode.getInternalTargets().get(0)).getInternalId()));

                }
            } else if (astNode.getInternalTargets().get(0) instanceof Hole &&
                    context.getTypeWrapper().getGuards().getTypes().get(":[[l" + ((Hole) astNode.getInternalTargets().get(0)).getN() + "]]") != null) {
                String[] info = context.getLocalVariableInfo(((Hole)astNode.getInternalTargets().get(0)).toString());
                if (info==null)
                    context.addLocalVariable(":[[l" + ((Hole) astNode.getInternalTargets().get(0)).getN() + "]]",
                        ""+astNode.getInternalTargets().get(0).getCharStartIndex(),
                            context.getTypeWrapper().getGuards().getTypes().get(":[[l" +
                                    ((Hole) astNode.getInternalTargets().get(0)).getN() + "]]").snd);

            }
            lg = buildPDG(control, branch, astNode.getInternalTargets().get(0));
            if (lg.getOnlyDataOut() != null)
                ldatanodes.add(lg.getOnlyDataOut());
            else if (lg.getOnlyHoleDataOut() != null)
                ldatanodes.add(lg.getOnlyHoleDataOut());
        }

        PDGGraph rg = buildPDG(control, branch, astNode.getInternalValue());

        ArrayList<PDGActionNode> rets = rg.getReturns();

        if (rets.size() > 0) {
            Assertions.UNREACHABLE();
        }
        ArrayList<PDGDataNode> defs = rg.getDefinitions();
        ArrayList<PDGHoleNode> holeDefs = rg.getHoleDefinitions();


        if (defs.isEmpty() && holeDefs.isEmpty()) {
            rg.mergeSequentialData(new PDGActionNode(control, branch,
                    astNode, PyObject.ASSIGN, null, null, "="), PARAMETER);
            for (PDGNode ldatanode : ldatanodes) {
                if (ldatanode instanceof PDGDataNode)
                    rg.mergeSequentialData(ldatanode, PDGDataEdge.Type.DEFINITION);
            }

            for (PDGNode ldatanode : ldatanodes) {
                if (ldatanode instanceof PDGHoleNode)
                    rg.mergeSequentialHoleData((PDGHoleNode) ldatanode, PDGDataEdge.Type.DEFINITION);
            }

        } else {
            if (defs.get(0).isDummy()) {
                for (PDGDataNode def : defs) {
                    rg.defStore.remove(def.getKey());
                    for (PDGNode ldatanode : ldatanodes) {
                        if (ldatanode instanceof PDGDataNode)
                            def.copyData((PDGDataNode) ldatanode);

                    }
                    HashSet<PDGDataNode> ns = rg.defStore.get(def.getKey());
                    if (ns == null) {
                        ns = new HashSet<>();
                        rg.defStore.put(def.getKey(), ns);
                    }
                    ns.add(def);
                }
                for (PDGHoleNode def : holeDefs) {
                    rg.defHoleStore.remove(def.getKey());
                    for (PDGNode ldatanode : ldatanodes) {
                        if (ldatanode instanceof PDGHoleNode) {
                            def.copyData((PDGHoleNode) ldatanode);
                        }
                    }
                    HashSet<PDGHoleNode> ns = rg.defHoleStore.get(def.getKey());
                    if (ns == null) {
                        ns = new HashSet<>();
                        rg.defHoleStore.put(def.getKey(), ns);
                    }
                    ns.add(def);
                }
            } else {
                PDGDataNode def = defs.get(0);
                rg.mergeSequentialData(new PDGDataNode(null, def.getAstNodeType(),
                                def.getKey(), def.getDataType(), def.getDataName()),
                        REFERENCE);
                rg.mergeSequentialData(new PDGActionNode(control, branch,
                        astNode, PyObject.ASSIGN, null, null, "="), PARAMETER);
                for (PDGNode ldatanode : ldatanodes) {
                    if (ldatanode instanceof PDGDataNode)
                        rg.mergeSequentialData(ldatanode, PDGDataEdge.Type.DEFINITION);
                    else if (ldatanode instanceof PDGHoleNode)
                        rg.mergeSequentialHoleData((PDGHoleNode) ldatanode, PDGDataEdge.Type.DEFINITION);
                }
            }
        }
        rg.nodes.addAll(lg.nodes);
        rg.statementNodes.addAll(lg.statementNodes);
        for (PDGNode ldatanode : ldatanodes) {
            if (ldatanode instanceof PDGDataNode)
                lg.dataSources.remove(ldatanode);
            else if (ldatanode instanceof PDGHoleNode)
                lg.holeDataSources.remove(ldatanode);
        }
        rg.dataSources.addAll(lg.dataSources);
        rg.statementSources.addAll(lg.statementSources);
        lg.clear();
        return rg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              AugAssign astNode) {
        PDGGraph g1 = buildPDG(control, branch, astNode.getInternalTarget());
        PDGGraph g2 = buildArgumentPDG(control, branch, astNode.getInternalValue());
        PDGActionNode opNode = new PDGActionNode(control, branch,
                null, PyObject.AUGASSIGN, null, null, "=" + astNode.getInternalOp().name());
        g1.mergeSequentialData(opNode, PARAMETER);
        g2.mergeSequentialData(opNode, PARAMETER);
        PDGGraph pdg = new PDGGraph(context);
        pdg.mergeParallel(g1, g2);
        pdg.mergeSequentialData(new PDGActionNode(control, branch,
                astNode, PyObject.ASSIGN, null, null, "="), PARAMETER);
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Tuple astNode) {
        PDGDataNode dummy = new PDGDataNode(null, PyObject.NAME,
                PDGNode.PREFIX_DUMMY + astNode.getCharStopIndex(), "boolean", PDGNode.PREFIX_DUMMY, false, true);
        PDGGraph[] pgs = new PDGGraph[astNode.getInternalElts().size()];
        if (astNode.getInternalElts().size() <= 10) {
            for (int i = 0; i < astNode.getInternalElts().size(); i++) {
                pgs[i] = buildArgumentPDG(control, branch, astNode.getInternalElts().get(i));
//                String[] info = null;
//                if (astNode.getInternalElts().get(i) instanceof Name) {
//                    info = context.getLocalVariableInfo(((Name) astNode.getInternalElts().get(i)).getInternalId());
//                }
//                if (info != null) {
//                    pgs[i] = new PDGGraph(context, new PDGDataNode(
//                            (astNode.getInternalElts().get(i)), (astNode.getInternalElts().get(i)).getNodeType(), info[0], info[1],
//                            ((Name) astNode.getInternalElts().get(i)).getInternalId(), false, false));
//                } else if (astNode.getInternalElts().get(i) instanceof Name) {
//                    context.addLocalVariable(
//                            ((Name) astNode.getInternalElts().get(i)).getInternalId(), "" +
//                                    (astNode.getInternalElts().get(i)).getCharStartIndex(),
//                            context.getTypeWrapper().getTypeInfo((astNode.getInternalElts().get(i)).getLine(),
//                                    ((Name) (astNode.getInternalElts().get(i))).getCol_offset(),
//                                    ((Name) astNode.getInternalElts().get(i)).getInternalId()));
//                    pgs[i] = buildArgumentPDG(control, branch, astNode.getInternalElts().get(i));
//                } else {
//                    pgs[i] = buildArgumentPDG(control, branch, astNode.getInternalElts().get(i));
//                }
            }
        } else {
            pgs = new PDGGraph[0];
        }
        PDGNode node = new PDGActionNode(control, branch, astNode, astNode.getNodeType(), null, "()", "()");
        if (pgs.length > 0) {
            for (PDGGraph pg : pgs) {
                if (pg != null)
                    pg.mergeSequentialData(node, PARAMETER);
            }
            PDGGraph pdg = new PDGGraph(context);
            pdg.mergeParallel(pgs);
            if (astNode.getParent() instanceof Assign) {
                pdg.mergeSequentialData(dummy, DEFINITION);
            }
            return pdg;
        } else {
            PDGGraph pdg = new PDGGraph(context, node);
            if (astNode.getParent() instanceof Assign) {
                pdg.mergeSequentialData(dummy, DEFINITION);
            }
//			pdg.mergeSequentialData(dummy,Type.DEFINITION);
            return pdg;
        }
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Num astNode) {

        return new PDGGraph(context, new PDGDataNode(
                astNode, astNode.getNodeType(), astNode.getInternalN().toString(), "number",
                astNode.getInternalN().toString()));

    }


    private PDGGraph buildPDG(PDGNode control, String branch,
                              Name astNode) {
        String name = astNode.getInternalId();
        String[] info = context.getLocalVariableInfo(name);
        if (info != null) {
            if (info[1]==null)
                info[1]="Any";
            return new PDGGraph(context, new PDGDataNode(
                    astNode, astNode.getNodeType(), info[0], info[1],
                    name, false, false));
        }
        String type = context.getFieldType(name); //TODO update get filed types
        if (type != null) {
            PDGGraph pdg = new PDGGraph(context, new PDGDataNode(
                    null, PyObject.SELF_EXPRESSION, "self",
                    "self", "self"));
            pdg.mergeSequentialData(new PDGDataNode(astNode, PyObject.FIELD_ACCESS,
                    "self." + name, type, name, true,
                    false), QUALIFIER);
            return pdg;
        }
        if (context.getImportsMap().containsKey(name)) {
            return new PDGGraph(context, new PDGDataNode(
                    astNode, astNode.getNodeType(), name, context.getImportsMap().get(name),
                    name, false, false));
        } else if (context.getTypeWrapper().getTypeInfo(astNode.getLineno(), astNode.getCol_offset(), astNode.getInternalId()) != null) {
            return new PDGGraph(context, new PDGDataNode(
                    astNode, astNode.getNodeType(), name,
                    context.getTypeWrapper().getTypeInfo(astNode.getLineno(), astNode.getCol_offset(), astNode.getInternalId()),
                    name, false, false));
        } else if (Character.isUpperCase(name.charAt(0))) {
            return new PDGGraph(context, new PDGDataNode(
                    astNode, astNode.getNodeType(), name, name,
                    name, false, false));
        }

        PDGGraph pdg = new PDGGraph(context, new PDGDataNode(
                null, PyObject.SELF_EXPRESSION, "self",
                "self", "self"));
        pdg.mergeSequentialData(new PDGDataNode(astNode, PyObject.FIELD_ACCESS,
                "self." + name, "UNKNOWN", name, true,
                false), QUALIFIER);
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Call astNode) {

        if (astNode.getInternalFunc() instanceof Name && ((Name) astNode.getInternalFunc()).getInternalId().equals("exit")) {
            PDGActionNode node = new PDGActionNode(control, branch,
                    astNode, astNode.getNodeType(), null, "exit()", "exit()");
            PDGGraph pdg = new PDGGraph(context, node);
            pdg.returns.add(node);
            pdg.sinks.remove(node);
            pdg.statementSinks.remove(node);
            return pdg;
        }

        PDGGraph[] pgs = new PDGGraph[astNode.getInternalArgs().size() + 1];
        if (astNode.getInternalArgs().size() > 100)
            pgs = new PDGGraph[1];
        PDGActionNode node = null;
        PDGHoleNode hnode = null;
        if (astNode.getInternalFunc() instanceof Attribute) {
            pgs[0] = buildArgumentPDG(control, branch,
                    ((Attribute) astNode.getInternalFunc()).getInternalValue());
            if (pgs[0].getOnlyDataOut() == null) {
                if (!((Attribute) astNode.getInternalFunc()).getInternalAttr().equals("Hole")) {
                    node = new PDGActionNode(control, branch,
                            astNode, astNode.getNodeType(), null,
                            ((Attribute) astNode.getInternalFunc()).getInternalAttr() + "()",
                            ((Attribute) astNode.getInternalFunc()).getInternalAttr());
                } else {
                    if (((Attribute) astNode.getInternalFunc()).getInternalHole() instanceof AlphanumericHole) {
                        hnode = new PDGAlphHole(astNode, astNode.getNodeType(),
                                context.getTypeWrapper().getGuards().getValueOfTemplateVariable(((Attribute) astNode.getInternalFunc()).getInternalAttr()),
                                null,
                                pgs[0].getOnlyHoleDataOut().getDataType() + "." + ((Attribute) astNode.getInternalFunc()).getInternalHole().toString() + "()",
                                ((Attribute) astNode.getInternalFunc()).getInternalHole().toString(),
                                false, true, false);
                    } else if (((Attribute) astNode.getInternalFunc()).getInternalHole() instanceof LazyHole) {
                        hnode = new PDGLazyHole(astNode, astNode.getNodeType(),
                                context.getTypeWrapper().getGuards().getValueOfTemplateVariable(((Attribute) astNode.getInternalFunc()).getInternalAttr()),
                                null,
                                pgs[0].getOnlyHoleDataOut().getDataType() + "." + ((Attribute) astNode.getInternalFunc()).getInternalHole().toString() + "()",
                                ((Attribute) astNode.getInternalFunc()).getInternalHole().toString(),
                                false, true, false);
                    } else {
                        Assertions.UNREACHABLE();
                    }
                }
            } else {
                if (!((Attribute) astNode.getInternalFunc()).getInternalAttr().equals("Hole")) {
                    node = new PDGActionNode(control, branch,
                            astNode, astNode.getNodeType(), null,
                            pgs[0].getOnlyOut().getDataType() + "." + ((Attribute) astNode.getInternalFunc()).getInternalAttr() + "()",
                            ((Attribute) astNode.getInternalFunc()).getInternalAttr());
                } else {
                    if (((Attribute) astNode.getInternalFunc()).getInternalHole()==null){
                        node = new PDGActionNode(control, branch,
                                astNode, astNode.getNodeType(), null,
                                pgs[0].getOnlyOut().getDataType() + "." + ((Attribute) astNode.getInternalFunc()).getInternalAttr() + "()",
                                ((Attribute) astNode.getInternalFunc()).getInternalAttr());
                    }
                    else if (((Attribute) astNode.getInternalFunc()).getInternalHole() instanceof AlphanumericHole) {
                        hnode = new PDGAlphHole(astNode, astNode.getNodeType(),
                                context.getTypeWrapper().getGuards().getValueOfTemplateVariable(((Attribute) astNode.getInternalFunc()).getInternalAttr()),
                                null,
                                pgs[0].getOnlyOut().getDataType() + "." + ((Attribute) astNode.getInternalFunc()).getInternalHole().toString() + "()",
                                ((Attribute) astNode.getInternalFunc()).getInternalHole().toString(),
                                false, true, false);
                    } else if (((Attribute) astNode.getInternalFunc()).getInternalHole() instanceof LazyHole) {
                        hnode = new PDGLazyHole(astNode, astNode.getNodeType(),
                                context.getTypeWrapper().getGuards().getValueOfTemplateVariable(((Attribute) astNode.getInternalFunc()).getInternalAttr()),
                                null,
                                pgs[0].getOnlyOut().getDataType() + "." + ((Attribute) astNode.getInternalFunc()).getInternalHole().toString() + "()",
                                ((Attribute) astNode.getInternalFunc()).getInternalHole().toString(),
                                false, true, false);
                    } else {
                        System.out.println(((Attribute) astNode.getInternalFunc()).getInternalAttr());
                        System.out.println(((Attribute) astNode.getInternalFunc()).getInternalHole().getClass());
                        Assertions.UNREACHABLE();
                    }
                }


            }

        } else if (astNode.getInternalFunc() instanceof Name) {
            pgs[0] = new PDGGraph(context);

            node = new PDGActionNode(control, branch,
                    astNode, astNode.getNodeType(), null,
                    "function" + "." + ((Name) astNode.getInternalFunc()).getInternalId() + "()",
                    ((Name) astNode.getInternalFunc()).getInternalId());
        } else if (astNode.getInternalFunc() instanceof Call) {
            pgs[0] = buildArgumentPDG(control, branch, astNode.getInternalFunc());

            node = new PDGActionNode(control, branch,
                    astNode, astNode.getNodeType(), null,
                    "function" + "." + "()", "");
        } else if (astNode.getInternalFunc() instanceof Subscript) {
            pgs[0] = buildArgumentPDG(control, branch,
                    ((Subscript) astNode.getInternalFunc()).getInternalValue());
            node = new PDGActionNode(control, branch,
                    astNode, ((Subscript) astNode.getInternalFunc()).getInternalSlice().getNodeType(), null,
                    null, "");
        }

        else if (astNode.getInternalFunc() instanceof BoolOp){
            pgs[0] = buildArgumentPDG(control, branch,
                    ((BoolOp) astNode.getInternalFunc()));
            node = new PDGActionNode(control, branch,
                    astNode, ( astNode.getInternalFunc()).getNodeType(), null,null, ((BoolOp) astNode.getInternalFunc()).getInternalOp().toString());

        }
        else if (astNode.getInternalFunc() instanceof BinOp){
            pgs[0] = buildArgumentPDG(control, branch, astNode.getInternalFunc());

            node = new PDGActionNode(control, branch,
                    astNode, astNode.getNodeType(), null,
                    null, "binop");
        }
        else if (astNode.getInternalFunc() instanceof Str){
            pgs[0] = buildArgumentPDG(control, branch, astNode.getInternalFunc());
            node = new PDGActionNode(control, branch,
                    astNode, astNode.getNodeType(), null,
                    "str", "Str");
        }


        else if (astNode.getInternalFunc() instanceof Hole) {

            if (astNode.getInternalFunc() instanceof AlphanumericHole) {

                hnode = new PDGAlphHole(astNode.getInternalFunc(), astNode.getInternalFunc().getNodeType(), context.getTypeWrapper().getGuards().getValueOfTemplateVariable(astNode.getInternalFunc().toString()),
                        "" + astNode.getInternalFunc().getCharStartIndex(),
                        context.getTypeWrapper().getGuards().getTypeOfTemplateVariable(astNode.getInternalFunc().toString()), context.getTypeWrapper().getGuards().getValueOfTemplateVariable(astNode.getInternalFunc().toString()), false, true, false);
            } else if (astNode.getInternalFunc() instanceof LazyHole) {
                hnode = new PDGLazyHole(astNode.getInternalFunc(), astNode.getInternalFunc().getNodeType(), context.getTypeWrapper().getGuards().getValueOfTemplateVariable(astNode.getInternalFunc().toString()),
                        "" + astNode.getInternalFunc().getCharStartIndex(),
                        context.getTypeWrapper().getGuards().getTypeOfTemplateVariable(astNode.getInternalFunc().toString()), context.getTypeWrapper().getGuards().getValueOfTemplateVariable(astNode.getInternalFunc().toString()), false, true, false);
            }

        }
        else if (astNode.getInternalFunc() instanceof Lambda){
            pgs[0] = buildArgumentPDG(control, branch,
                    astNode.getInternalFunc());
            node = new PDGActionNode(control, branch,
                    astNode, ( astNode.getInternalFunc()).getNodeType(), null,null, "lambda");
        }
        else if (astNode.getInternalFunc() instanceof IfExp){
            pgs[0] = buildArgumentPDG(control, branch,
                    astNode.getInternalFunc());
            node = new PDGActionNode(control, branch,
                    astNode, ( astNode.getInternalFunc()).getNodeType(), null,null, "ifexp");
        }
        else if (astNode.getInternalFunc() instanceof UnaryOp){
            pgs[0] = buildArgumentPDG(control, branch,
                    astNode.getInternalFunc());
            node = new PDGActionNode(control, branch,
                    astNode, ( astNode.getInternalFunc()).getNodeType(), null,null, "UnaryOp");
        }
        else{
            Assertions.UNREACHABLE( );
        }

        PDGGraph pdg = null;
        if (astNode.getInternalArgs().size() <= 100) {
            for (int i = 0; i < astNode.getInternalArgs().size(); i++)
                pgs[i + 1] = buildArgumentPDG(control, branch, astNode.getInternalArgs().get(i));
        }
        if (node != null) {
            pgs[0].mergeSequentialData(node, PDGDataEdge.Type.RECEIVER);
            if (pgs.length > 0) {
                for (int i = 1; i < pgs.length; i++)
                    pgs[i].mergeSequentialData(node, PARAMETER);
                pdg = new PDGGraph(context);
                pdg.mergeParallel(pgs);
            } else
                pdg = new PDGGraph(context, node);
        } else if (hnode != null) {
            if (pgs[0] != null) {
                pgs[0].mergeSequentialHoleData(hnode, PDGDataEdge.Type.RECEIVER);
            }
            if (pgs.length > 0) {
                for (int i = 1; i < pgs.length; i++)
                    pgs[i].mergeSequentialHoleData(hnode, PARAMETER);
                pdg = new PDGGraph(context);
                pdg.mergeParallel(Arrays.stream(pgs).skip(1).toArray(size -> new PDGGraph[astNode.getInternalArgs().size()]));
            } else
                pdg = new PDGGraph(context, hnode);
        }
        return pdg;
    }


    private PDGGraph buildPDG(PDGNode control, String branch,
                              Return astNode) {
        PDGGraph pdg = null;
        PDGActionNode node = null;
        if (astNode.getInternalValue() != null) {
            pdg = buildArgumentPDG(control, branch, astNode.getInternalValue());
            node = new PDGActionNode(control, branch, astNode, astNode.getNodeType(),
                    null, null, "return");
            pdg.mergeSequentialData(node, PARAMETER);
        } else {
            node = new PDGActionNode(control, branch, astNode, astNode.getNodeType(),
                    null, null, "return");
            pdg = new PDGGraph(context, node);
        }
        pdg.returns.add(node);
        pdg.sinks.clear();
        pdg.statementSinks.clear();
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Expr astNode) {
        PDGGraph pdg = buildPDG(control, branch, astNode.getInternalValue());
        ArrayList<PDGActionNode> rets = pdg.getReturns();
        if (rets.size() > 0) {
            for (PDGNode ret : new HashSet<PDGNode>(rets)) {
                for (PDGEdge e : new HashSet<PDGEdge>(ret.getInEdges()))
                    if (e.getSource() instanceof PDGDataNode)
                        pdg.delete(e.getSource());
                pdg.delete(ret);
            }
        }
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              BinOp astNode) {
        PDGGraph pdg = new PDGGraph(context);
        PDGGraph lg = buildArgumentPDG(control, branch,
                astNode.getInternalLeft());
        PDGGraph rg = buildArgumentPDG(control, branch,
                astNode.getInternalRight());
        PDGActionNode node = new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), null, null, astNode.getInternalOp().toString());
        lg.mergeSequentialData(node, PARAMETER);
        rg.mergeSequentialData(node, PARAMETER);
        pdg.mergeParallel(lg, rg);

        return pdg;

    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Subscript astNode) {

        PDGGraph pdg = buildArgumentPDG(control, branch, astNode.getInternalValue());
        String type=null;
        if (pdg.getOnlyOut()!=null)
            type = pdg.getOnlyOut().getDataType();

        if (type != null && !type.endsWith("]"))
            type = type + "[.]";
        else
            type = "Any";
        PDGNode node = new PDGDataNode(astNode, astNode.getNodeType(),
                context.getKey(astNode), type,
                astNode.toString());
        pdg.mergeSequentialData(node, QUALIFIER);
        PDGGraph ig = buildArgumentPDG(control, branch, astNode.getInternalSlice());
        ig.mergeSequentialData(node, PARAMETER);
        pdg.mergeBranches(ig);
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Index astNode) {
        return buildPDG(control, branch, astNode.getInternalValue());
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Str astNode) {
        String lit = astNode.getInternalS().toString();

        return new PDGGraph(context, new PDGDataNode(
                astNode, astNode.getNodeType(), lit, "String",
                lit));
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Lambda astNode) {
        // TODO Implement this
        context.addScope();
//        PDGControlNode lambda = new PDGControlNode(control, branch, astNode, astNode.getNodeType());
//        PDGDataNode[] args = new PDGDataNode[astNode.getInternalArgs().getInternalArgs().size()];
//        int i = 0;
//        for ( arg internalArg : astNode.getInternalArgs().getInternalArgs()) {
//
//            String name = internalArg.getInternalArg();
//            String type = context.getTypeWrapper().getTypeInfo(internalArg.getLine(), internalArg.getCharPositionInLine());
//            if (type == null)
//                type = context.getTypeWrapper().getTypeInfo(name);
//            context.addLocalVariable(name, "" + internalArg.getCharStartIndex(), type);
//            PDGDataNode pdn = new PDGDataNode(internalArg, internalArg.getNodeType(), "" + internalArg.getCharStartIndex(), type,
//                    name, false, true);
//            this.parameters[numOfParameters++] = new PDGDataNode(
//                    arg, PyObject.NAME, info[0], info[1],
//                    "PARAM_" + arg.getInternalArg(), false, true)
//            args[i]=pdn;
//            i+=1;
//        }
//
//        PDGGraph graph = buildArgumentPDG(control, branch, astNode.getInternalBody());
//        for (PDGDataNode loopVariable : args) {
//            graph.mergeSequentialData(loopVariable, DEFINITION);
//        }
//        graph.mergeSequentialData(lambda, PARAMETER);

//        astNode.get
//        PDGGraph[] pgs = new PDGGraph[astNode.getInternalGenerators().size()];
//        PDGControlNode listComp = new PDGControlNode(control, branch, astNode, astNode.getNodeType());
//        int numOfComparators = 0;
//        for (comprehension generator : astNode.getInternalGenerators()) {
//            generator.setProperty("target", astNode.getInternalElt());
//            generator.setProperty("listNode", listComp);
//            pgs[numOfComparators] = buildArgumentPDG(control, branch, generator);
//            numOfComparators++;
//        }
//
////        PDGGraph lg = buildArgumentPDG(control, branch,astNode.getInternalElt());
//        if (pgs.length > 1) {
//            for (int i = pgs.length - 1; i > 0; i--) {
//                pgs[i - 1].mergeSequential(pgs[i]);
//            }
//        }
//        pgs[0].mergeSequentialData(listComp, PARAMETER);
        context.removeScope();


        return new PDGGraph(context);
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              With astNode) {
        context.addScope();
        PDGControlNode node = new PDGControlNode(control, branch,
                astNode, astNode.getNodeType());
        PDGGraph[] pgs = new PDGGraph[astNode.getInternalItems().size()];

        for (int i = 0; i < astNode.getInternalItems().size(); i++)
            pgs[i] = buildArgumentPDG(control, branch, astNode.getInternalItems().get(i));

        PDGGraph pdg = null;
        if (pgs.length > 0) {
            for (int i = 0; i < pgs.length; i++)
                pgs[i].mergeSequentialData(node, PARAMETER);
            pdg = new PDGGraph(context);
            pdg.mergeParallel(pgs);
        } else
            pdg = new PDGGraph(context, node);

        PDGGraph ebg = new PDGGraph(context, new PDGActionNode(node, "T",
                null, PyObject.EMPTY_STATEMENT, null, null, "empty"));
        PDGGraph bg = buildPDG(node, "T", astNode.getBody());
        if (!bg.isEmpty())
            ebg.mergeSequential(bg);
        PDGGraph eg = new PDGGraph(context, new PDGActionNode(node, "F",
                null, PyObject.EMPTY_STATEMENT, null, null, "empty"));
        pdg.mergeBranches(ebg, eg);


        pdg.adjustBreakNodes("");

        context.removeScope();
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              withitem astNode) {


        if (astNode.getInternalOptional_vars() != null) {
            PDGGraph lg = buildPDG(control, branch, astNode.getInternalOptional_vars());
            PDGDataNode lnode = lg.getOnlyDataOut();

            PDGGraph rg = buildPDG(control, branch, astNode.getInternalContext_expr());
            rg.mergeSequentialData(new PDGActionNode(control, branch,
                    astNode, PyObject.WITHITEM, null, null, "AS"), PARAMETER);
            if (lnode != null) {
                rg.mergeSequentialData(lnode, DEFINITION);
            } else if (lg.getOnlyHoleDataOut() != null) {
                rg.mergeSequentialHoleData(lg.getOnlyHoleDataOut(), DEFINITION);
            }
            else {
                //TODO implement the else branch
            }
            rg.nodes.addAll(lg.nodes);
            rg.statementNodes.addAll(lg.statementNodes);
            lg.dataSources.remove(lnode);
            rg.dataSources.addAll(lg.dataSources);
            rg.statementSources.addAll(lg.statementSources);
            lg.clear();
            return rg;
        } else {
            PDGGraph gr = buildPDG(control, branch, astNode.getInternalContext_expr());
            gr.mergeSequentialData(new PDGActionNode(control, branch,
                    astNode, PyObject.WITHITEM, null, null, "AS"), PARAMETER);
            return gr;
        }
    }


    private PDGGraph buildPDG(PDGNode control, String branch,
                              If astNode) {
        context.addScope();
        PDGGraph pdg = buildArgumentPDG(control, branch,
                astNode.getInternalTest());
        PDGControlNode node = new PDGControlNode(control, branch,
                astNode, astNode.getNodeType());
        pdg.mergeSequentialData(node, CONDITION);

        PDGGraph etg = new PDGGraph(context, new PDGActionNode(node, "T",
                null, PyObject.EMPTY_STATEMENT, null, null, "empty"));

        PDGGraph tg = buildPDG(node, "T", astNode.getBody());
        if (!tg.isEmpty())
            etg.mergeSequential(tg);
        PDGGraph efg = new PDGGraph(context, new PDGActionNode(node, "F",
                null, PyObject.EMPTY_STATEMENT, null, null, "empty"));

        if (astNode.getInternalOrelse() != null) {
            PDGGraph fg = buildPDG(node, "F", astNode.getOrelse());
            if (!fg.isEmpty())
                efg.mergeSequential(fg);
        }
        pdg.mergeBranches(etg, efg);

        context.removeScope();
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Compare astNode) {
        PDGGraph pdg = new PDGGraph(context);
        PDGGraph lg = buildArgumentPDG(control, branch,
                astNode.getInternalLeft());
        PDGGraph rg = buildArgumentPDG(control, branch,
                astNode.getInternalComparators().get(0));
        PDGNode node = new PDGActionNode(control, branch, astNode, astNode.getNodeType(), null, null, astNode.getInternalOps().get(0).toString());

        lg.mergeSequentialData(node, PARAMETER);
        rg.mergeSequentialData(node, PARAMETER);
        pdg.mergeParallel(lg, rg);

//        if (astNode.getInternalComparators().size()>1){
//            for (int i = 0; i < astNode.getInternalComparators().size(); i++) {
//                PDGGraph tmp = buildArgumentPDG(control, branch,astNode.getInternalComparators().get(i));
////                PDGNode node1= new PDGActionNode(control, branch, astNode, astNode.getNodeType(), null, null, astNode.getInternalOps().get(i).toString());
//                tmp.mergeSequentialData(node, PARAMETER);
//                pdg.mergeParallel(tmp);
//            }
//        } TODO handel astNode.getInternalComparators().size() cases


//        PDGActionNode node = new PDGActionNode(control, branch,
//                astNode, astNode.getNodeType(), null, null, astNode.get);

        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              List astNode) {

        PDGGraph[] pgs = new PDGGraph[astNode.getInternalElts().size()];
        if (astNode.getInternalElts().size() <= 10) {
            for (int i = 0; i < astNode.getInternalElts().size(); i++)
                pgs[i] = buildArgumentPDG(control, branch, astNode.getInternalElts().get(i));
        } else
            pgs = new PDGGraph[0];
        PDGNode node = new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), null, "[]", "[list]");
        if (pgs.length > 0) {
            for (PDGGraph pg : pgs)
                pg.mergeSequentialData(node, PARAMETER);
            PDGGraph pdg = new PDGGraph(context);
            pdg.mergeParallel(pgs);
            return pdg;
        } else
            return new PDGGraph(context, node);
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              org.python.antlr.ast.Set astNode) {
        PDGGraph[] pgs = new PDGGraph[astNode.getInternalElts().size()];
        if (astNode.getInternalElts().size() <= 10) {
            for (int i = 0; i < astNode.getInternalElts().size(); i++)
                pgs[i] = buildArgumentPDG(control, branch, astNode.getInternalElts().get(i));
        } else
            pgs = new PDGGraph[0];
        PDGNode node = new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), null, "{}", "{set}");
        if (pgs.length > 0) {
            for (PDGGraph pg : pgs)
                pg.mergeSequentialData(node, PARAMETER);
            PDGGraph pdg = new PDGGraph(context);
            pdg.mergeParallel(pgs);
            return pdg;
        } else
            return new PDGGraph(context, node);
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Ellipsis astNode) {
        return new PDGGraph(context, new PDGDataNode(
                astNode, astNode.getNodeType(), "...", "...",
                "..."));
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Bytes astNode) {
        String lit = astNode.getInternalS();
        return new PDGGraph(context, new PDGDataNode(
                astNode, astNode.getNodeType(), lit, "Bytes",
                lit));

    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              ListComp astNode) {
        context.addScope();
        if (astNode.getInternalGenerators().size()==0){
            return new PDGGraph(context, new PDGActionNode(control, branch,
                    astNode, astNode.getNodeType(), null, null, "listcomp"));
        }
        PDGGraph[] pgs = new PDGGraph[astNode.getInternalGenerators().size()];
        PDGControlNode listComp = new PDGControlNode(control, branch, astNode, astNode.getNodeType());
        int numOfComparators = 0;
        for (comprehension generator : astNode.getInternalGenerators()) {
            generator.setProperty("target", astNode.getInternalElt());
            generator.setProperty("listNode", listComp);
            pgs[numOfComparators] = buildArgumentPDG(control, branch, generator);
            numOfComparators++;
        }

//        PDGGraph lg = buildArgumentPDG(control, branch,astNode.getInternalElt());
        if (pgs.length > 1) {
            for (int i = pgs.length - 1; i > 0; i--) {
                pgs[i - 1].mergeSequential(pgs[i]);
            }
        }
        pgs[0].mergeSequentialData(listComp, PARAMETER);
        context.removeScope();
        return pgs[0];
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              DictComp astNode) {
        if (astNode.getInternalGenerators().size()==0){
            return new PDGGraph(context, new PDGActionNode(control, branch,
                    astNode, astNode.getNodeType(), null, null, "diccomp"));
        }
        context.addScope();
        PDGGraph[] pgs = new PDGGraph[astNode.getInternalGenerators().size()];
        PDGControlNode dictComp = new PDGControlNode(control, branch, astNode, astNode.getNodeType());
        int numOfComparators = 0;
        for (comprehension generator : astNode.getInternalGenerators()) {
            generator.setProperty("targetKey", astNode.getInternalKey());
            generator.setProperty("target", astNode.getInternalValue());
            generator.setProperty("listNode", dictComp);
            pgs[numOfComparators] = buildArgumentPDG(control, branch, generator);
            numOfComparators++;
        }
//        PDGGraph lg = buildArgumentPDG(control, branch,astNode.getInternalElt());
        if (pgs.length > 1) {
            for (int i = pgs.length - 1; i > 0; i--) {
                pgs[i - 1].mergeSequential(pgs[i]);
            }
        }
        if(pgs.length==0)
            System.out.println();
        pgs[0].mergeSequentialData(dictComp, PARAMETER);
        context.removeScope();
        return pgs[0];
    }


    private PDGGraph buildPDG(PDGNode control, String branch,
                              comprehension astNode) {
        java.util.List<PDGDataNode> loopVariables = new ArrayList<>();
        if (astNode.getInternalTarget() instanceof Name) {
            String name = ((Name) astNode.getInternalTarget()).getInternalId();
            String type = context.getTypeWrapper().getTypeInfo(astNode.getInternalTarget().getLine(),
                    astNode.getInternalTarget().getCharPositionInLine());
            if (type == null)
                type = context.getTypeWrapper().getTypeInfo(name);
            context.addLocalVariable(name, "" + astNode.getInternalTarget().getCharStartIndex(), type);
            PDGDataNode pdn = new PDGDataNode(astNode.getInternalTarget(),
                    astNode.getInternalTarget().getNodeType(), "" + astNode.getInternalTarget().getCharStartIndex(), type,
                    name, false, true);
            loopVariables.add(pdn);

        } else if (astNode.getInternalTarget() instanceof Tuple) {
            for (expr elt : ((Tuple) astNode.getInternalTarget()).getInternalElts()) {
                if (elt instanceof Name){
                    String name = ((Name) elt).getInternalId();
                    String type = context.getTypeWrapper().getTypeInfo(elt.getLine(), elt.getCharPositionInLine());
                    if (type == null)
                        type = context.getTypeWrapper().getTypeInfo(name);
                    context.addLocalVariable(name, "" + elt.getCharStartIndex(), type);
                    PDGDataNode pdn = new PDGDataNode(elt, elt.getNodeType(), "" + elt.getCharStartIndex(), type,
                            name, false, true);
                    loopVariables.add(pdn);
                }
                else if (elt instanceof Tuple){
                    //TODO impliment this
                }

            }

        }

        PDGGraph graph = buildArgumentPDG(control, branch, astNode.getInternalIter());
        for (PDGDataNode loopVariable : loopVariables) {
            graph.mergeSequentialData(loopVariable, DEFINITION);
        }
        PDGControlNode node = new PDGControlNode(control, branch, astNode, PyObject.COMPREHENSION);
        graph.mergeSequentialData(node, CONDITION);
        PDGGraph[] ifpdgs = new PDGGraph[astNode.getInternalIfs().size()];
        int j = 0;
        if (astNode.getInternalIfs().size() > 0) {
            for (expr anIf : astNode.getInternalIfs()) {
                PDGGraph pdg = buildArgumentPDG(control, branch,
                        anIf);
                PDGControlNode ifnode = new PDGControlNode(control, branch,
                        anIf, PyObject.IF);
                pdg.mergeSequentialData(ifnode, CONDITION);

//                target.mergeSequentialData(new PDGActionNode(ifnode, "T", null, PyObject.ASSIGN,
//                        null, null, "="), PARAMETER);
                if (astNode.getProperty("targetKey") != null) {
                    PDGGraph target = buildArgumentPDG(ifnode, "T", (expr) astNode.getProperty("target"));
                    target.mergeSequentialDataNoUpdatetoSinks(node, CONDITION);
                    target.mergeSequentialDataNoUpdatetoSinks((PDGControlNode) astNode.getProperty("listNode"), PARAMETER);
                    PDGGraph value = buildArgumentPDG(ifnode, "T", (expr) astNode.getProperty("targetKey"));
                    value.mergeSequentialDataNoUpdatetoSinks(node, CONDITION);
                    value.mergeSequentialDataNoUpdatetoSinks((PDGControlNode) astNode.getProperty("listNode"), PARAMETER);
                    pdg.mergeBranches(target, value);
                    ifpdgs[j] = pdg;
                    j += 1;

                } else {
                    PDGGraph target = buildArgumentPDG(ifnode, "T", (expr) astNode.getProperty("target"));
                    target.mergeSequentialDataNoUpdatetoSinks(node, CONDITION);
                    target.mergeSequentialDataNoUpdatetoSinks((PDGControlNode) astNode.getProperty("listNode"), PARAMETER);

                    pdg.mergeBranches(target);
                    ifpdgs[j] = pdg;
                    j += 1;
                }

            }
            PDGGraph targetg = new PDGGraph(context);
            targetg.mergeParallel(ifpdgs);
            graph.mergeSequential(targetg);

        } else {
            PDGGraph target1 = buildArgumentPDG(control, branch, (expr) astNode.getProperty("target"));
            target1.mergeSequentialData((PDGControlNode) astNode.getProperty("listNode"), CONDITION);
            graph.mergeSequential(target1);

        }


        graph.mergeSequentialControl(new PDGActionNode(node, "",
                null, PyObject.EMPTY_STATEMENT, null, null, "empty"), "");
        graph.adjustBreakNodes("");

        return graph;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              SetComp astNode) {
        context.addScope();
        PDGGraph[] pgs = new PDGGraph[astNode.getInternalGenerators().size()];
        PDGControlNode setComp = new PDGControlNode(control, branch, astNode, astNode.getNodeType());
        int numOfComparators = 0;
        for (comprehension generator : astNode.getInternalGenerators()) {
            generator.setProperty("target", astNode.getInternalElt());
            generator.setProperty("listNode", setComp);
            pgs[numOfComparators] = buildArgumentPDG(control, branch, generator);
            numOfComparators++;
        }

//        PDGGraph lg = buildArgumentPDG(control, branch,astNode.getInternalElt());
        if (pgs.length > 1) {
            for (int i = pgs.length - 1; i > 0; i--) {
                pgs[i - 1].mergeSequential(pgs[i]);
            }
        }
        pgs[0].mergeSequentialData(setComp, PARAMETER);
        context.removeScope();
        return pgs[0];
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              GeneratorExp astNode) {
        context.addScope();
        PDGGraph[] pgs = new PDGGraph[astNode.getInternalGenerators().size()];
        PDGControlNode setComp = new PDGControlNode(control, branch, astNode, astNode.getNodeType());
        int numOfComparators = 0;
        for (comprehension generator : astNode.getInternalGenerators()) {
            generator.setProperty("target", astNode.getInternalElt());
            generator.setProperty("listNode", setComp);
            pgs[numOfComparators] = buildArgumentPDG(control, branch, generator);
            numOfComparators++;
        }

//        PDGGraph lg = buildArgumentPDG(control, branch,astNode.getInternalElt());
        if (pgs.length > 1) {
            for (int i = pgs.length - 1; i > 0; i--) {
                pgs[i - 1].mergeSequential(pgs[i]);
            }
        }
        pgs[0].mergeSequentialData(setComp, PARAMETER);
        context.removeScope();
        return pgs[0];
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              TryExcept astNode) {

        context.pushTry();
        PDGGraph pdg = new PDGGraph(context);
        PDGControlNode node = new PDGControlNode(control, branch,
                astNode, astNode.getNodeType());
        pdg.mergeSequentialControl(node, "");

        PDGGraph[] gs = new PDGGraph[astNode.getInternalHandlers().size() + 1];
        gs[0] = buildPDG(node, "T", astNode.getBody());

        for (int i = 0; i < astNode.getInternalHandlers().size(); i++) {
            excepthandler cc = astNode.getInternalHandlers().get(i);
            gs[i + 1] = buildPDG(node, "F", cc);
        }
        pdg.mergeBranches(gs);

        context.popTry();
        return pdg;

    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              ExceptHandler astNode) {
        context.addScope();

        String name = astNode.getInternalName();
        expr type = astNode.getInternalType();
        if (type instanceof Name) {
            context.addLocalVariable(name, "" + type.getCharStartIndex(), ((Name) type).getInternalId());
        }
        PDGControlNode node = new PDGControlNode(control, branch,
                astNode, astNode.getNodeType());

        PDGGraph pdg = new PDGGraph(context, node);
        PDGGraph cg = new PDGGraph(context, new PDGActionNode(node, "",
                null, PyObject.EMPTY_STATEMENT, null, null, "empty"));
        if (astNode.getInternalBody().size() > 0) {
            cg.mergeSequential(buildPDG(node, "", astNode.getBody()));
        }
        pdg.mergeSequentialControl(cg);

        context.removeScope();
        return pdg;

    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              While astNode) {
        context.addScope();
        PDGGraph pdg = buildArgumentPDG(control, branch,
                astNode.getInternalTest());
        PDGControlNode node = new PDGControlNode(control, branch,
                astNode, astNode.getNodeType());
        pdg.mergeSequentialData(node, CONDITION);
        PDGGraph ebg = new PDGGraph(context, new PDGActionNode(node, "T",
                null, PyObject.EMPTY_STATEMENT, null, null, "empty"));
        PDGGraph bg = buildPDG(node, "T", astNode.getBody());
        if (!bg.isEmpty())
            ebg.mergeSequential(bg);
        PDGGraph eg = new PDGGraph(context, new PDGActionNode(node, "F",
                null, PyObject.EMPTY_STATEMENT, null, null, "empty"));
        pdg.mergeBranches(ebg, eg);
        /*
         * pdg.sinks.remove(node); pdg.statementSinks.remove(node);
         */
        pdg.adjustBreakNodes("");

        context.removeScope();
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Global astNode) {
        PDGGraph[] pdgs;
        PDGGraph pdg = new PDGGraph(context);
        ;
        PDGActionNode node = null;
        if (astNode.getInternalNameNodes().size() > 0) {
            pdgs = new PDGGraph[astNode.getInternalNameNodes().size()];
            for (int i = 0; i < astNode.getInternalNameNodes().size(); i++) {
                pdgs[i] = buildArgumentPDG(control, branch, astNode.getInternalNameNodes().get(i));
            }
            pdg.mergeParallel(pdgs);
            node = new PDGActionNode(control, branch, astNode, astNode.getNodeType(),
                    null, null, "Global");
            pdg.mergeSequentialData(node, PARAMETER);
        } else {
            node = new PDGActionNode(control, branch, astNode, astNode.getNodeType(),
                    null, null, "Global");
            pdg = new PDGGraph(context, node);
        }

        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Nonlocal astNode) {
        PDGGraph[] pdgs;
        PDGGraph pdg = new PDGGraph(context);
        ;
        PDGActionNode node = null;
        if (astNode.getInternalNameNodes().size() > 0) {
            pdgs = new PDGGraph[astNode.getInternalNameNodes().size()];
            for (int i = 0; i < astNode.getInternalNameNodes().size(); i++) {
                pdgs[i] = buildArgumentPDG(control, branch, astNode.getInternalNameNodes().get(i));
            }
            pdg.mergeParallel(pdgs);
            node = new PDGActionNode(control, branch, astNode, astNode.getNodeType(),
                    null, null, "Global");
            pdg.mergeSequentialData(node, PARAMETER);
        } else {
            node = new PDGActionNode(control, branch, astNode, astNode.getNodeType(),
                    null, null, "Global");
            pdg = new PDGGraph(context, node);
        }

        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Yield astNode) {
        PDGGraph pdg = null;
        PDGActionNode node = null;
        if (astNode.getInternalValue() != null) {
            pdg = buildArgumentPDG(control, branch, astNode.getInternalValue());
            node = new PDGActionNode(control, branch, astNode, astNode.getNodeType(),
                    null, null, "yield");
            pdg.mergeSequentialData(node, PARAMETER);
        } else {
            node = new PDGActionNode(control, branch, astNode, astNode.getNodeType(),
                    null, null, "yield");
            pdg = new PDGGraph(context, node);
        }
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              YieldFrom astNode) {
        PDGGraph pdg = null;
        PDGActionNode node = null;
        if (astNode.getInternalValue() != null) {
            pdg = buildArgumentPDG(control, branch, astNode.getInternalValue());
            node = new PDGActionNode(control, branch, astNode, astNode.getNodeType(),
                    null, null, "yieldfrom");
            pdg.mergeSequentialData(node, PARAMETER);
        } else {
            node = new PDGActionNode(control, branch, astNode, astNode.getNodeType(),
                    null, null, "yieldfrom");
            pdg = new PDGGraph(context, node);
        }
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Attribute astNode) {

        PDGGraph pdg = buildArgumentPDG(control, branch, astNode.getInternalValue());
        PDGDataNode node = pdg.getOnlyDataOut();
        if (node != null) {
            if (astNode.getInternalAttr() != null) {
                if (node.getDataType()==null){
                    System.out.println();
                    buildArgumentPDG(control, branch, astNode.getInternalValue());
                }
                if (node.getDataType().startsWith("UNKNOWN")) {
                    String name = astNode.getInternalAttr();
//                    if (Character.isUpperCase(name.charAt(0))) {
                    return new PDGGraph(context, new PDGDataNode(astNode, PyObject.FIELD_ACCESS, getFullNameOfAttribute(astNode),
                            getFullNameOfAttribute(astNode), astNode.getInternalAttr(), true, false));
//                    }
                } else {
                    pdg.mergeSequentialData(
                            new PDGDataNode(astNode, PyObject.FIELD_ACCESS, getFullNameOfAttribute(astNode),
                                    node.getDataType() + "." + astNode.getInternalAttr(),
                                    astNode.getInternalAttr(), true, false), QUALIFIER);
                }
            }
        } else if (pdg.getOnlyHoleDataOut() != null) {
            PDGHoleNode holeDataOut = pdg.getOnlyHoleDataOut();
            if (astNode.getInternalHole() instanceof AlphanumericHole) {
                pdg.mergeSequentialHoleData(new PDGAlphHole(astNode.getInternalHole(),
                                astNode.getInternalHole().getNodeType(),
                                context.getTypeWrapper().getGuards().getValueOfTemplateVariable(astNode.getInternalAttr()),
                                getFullNameOfAttribute(astNode),
                                pdg.getOnlyHoleDataOut().getDataType() + "." + astNode.getInternalAttr(),
                                astNode.getInternalAttr(), true, false, false)
                        , QUALIFIER);

            } else if (astNode.getInternalHole() instanceof LazyHole) {
                pdg.mergeSequentialHoleData(new PDGLazyHole(astNode.getInternalHole(),
                                astNode.getInternalHole().getNodeType(),
                                context.getTypeWrapper().getGuards().getValueOfTemplateVariable(astNode.getInternalAttr()),
                                getFullNameOfAttribute(astNode),
                                pdg.getOnlyHoleDataOut().getDataType() + "." + astNode.getInternalAttr(),
                                astNode.getInternalAttr(), true, false, false)
                        , QUALIFIER);

            }
        } else {
//            Assertions.UNREACHABLE( );
        }

        return pdg;
//        return null;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              UnaryOp astNode) {
        PDGGraph pdg = buildArgumentPDG(control, branch, astNode.getOperand());
        if (astNode.getInternalOp() == unaryopType.Invert || astNode.getInternalOp() == unaryopType.Not || astNode.getInternalOp() == unaryopType.USub
                || astNode.getInternalOp() == unaryopType.UAdd
        ) {
            pdg.mergeSequentialData(
                    new PDGActionNode(control, branch, astNode, astNode.getNodeType(),
                            null, null, astNode.getInternalOp().toString()),
                    PARAMETER);
        } else
            Assertions.UNREACHABLE(astNode.getInternalOp().toString());
        return pdg;

    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              AlphanumericHole astNode) {


        String name = ":[[l" + (astNode).getInternalN() + "]]";
        String[] info = context.getLocalVariableInfo(name);
        if (info != null) {
            return new PDGGraph(context, new PDGAlphHole(
                    astNode, astNode.getNodeType(), context.getTypeWrapper().getGuards().getValueOfTemplateVariable(name), info[0], info[1],
                    name, true, false, false));
        }

        String type = context.getTypeWrapper().getGuards().getTypeOfTemplateVariable(name);
        String value = context.getTypeWrapper().getGuards().getValueOfTemplateVariable(name);
        if (type != null) {
            context.addLocalVariable(name, ""+astNode.getCharStartIndex(), type);
            PDGGraph pdg = new PDGGraph(context, new PDGAlphHole(
                    astNode, astNode.getNodeType(), value, ""+astNode.getCharStartIndex(), type, name, true, false, false));

            return pdg;
        }
//        if (context.getImportsMap().containsKey(name)) {
//            return new PDGGraph(context, new PDGDataNode(
//                    astNode, astNode.getNodeType(), name, context.getImportsMap().get(name),
//                    name, false, false));
//        } else if (context.getTypeWrapper().getTypeInfo(astNode.getLineno(), astNode.getCol_offset(),astNode.getInternalId()) != null) {
//            return new PDGGraph(context, new PDGDataNode(
//                    astNode, astNode.getNodeType(), name,
//                    context.getTypeWrapper().getTypeInfo(astNode.getLineno(), astNode.getCol_offset(),astNode.getInternalId()),
//                    name, false, false));
//        } else if (Character.isUpperCase(name.charAt(0))) {
//            return new PDGGraph(context, new PDGDataNode(
//                    astNode, astNode.getNodeType(), name, name,
//                    name, false, false));
//        }
//
//        PDGGraph pdg = new PDGGraph(context, new PDGDataNode(
//                null, PyObject.SELF_EXPRESSION, "self",
//                "self", "self"));
//        pdg.mergeSequentialData(new PDGDataNode(astNode, PyObject.FIELD_ACCESS,
//                "self." + name, "UNKNOWN", name, true,
//                false), QUALIFIER);
//        return pdg;


        return new PDGGraph(context);

    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              LazyHole astNode) {
        String name = ":[l" + (astNode).getInternalN() + "]";
        String[] info = context.getLocalVariableInfo(name);
        if (info != null) {
            return new PDGGraph(context, new PDGLazyHole(
                    astNode, astNode.getNodeType(), context.getTypeWrapper().getGuards().getValueOfTemplateVariable(name), info[0], info[1],
                    name, true, false, false));
        }
        String type = context.getTypeWrapper().getGuards().getTypeOfTemplateVariable(name);
        String value = context.getTypeWrapper().getGuards().getValueOfTemplateVariable(name);
        if (type != null) {
            context.addLocalVariable(name, "", type);
            return new PDGGraph(context, new PDGLazyHole(
                    astNode, astNode.getNodeType(), value, ""+astNode.getCharStartIndex(), type, name, true, false, false));
        }
        return new PDGGraph(context);
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              BoolOp astNode) {

        PDGActionNode node = new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), null, null, astNode.getInternalOp().toString());
        PDGGraph[] pgs = new PDGGraph[astNode.getInternalValues().size()];

        for (int i = 0; i < astNode.getInternalValues().size(); i++)
            pgs[i] = buildArgumentPDG(control, branch, astNode.getInternalValues().get(i));

        PDGGraph pdg = null;
        if (pgs.length > 0) {
            for (int i = 0; i < pgs.length; i++)
                pgs[i].mergeSequentialData(node, PARAMETER);
            pdg = new PDGGraph(context);
            pdg.mergeParallel(pgs);
        } else
            pdg = new PDGGraph(context, node);
        return pdg;

    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Break astNode) {
        PDGActionNode node = new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), astNode.getText(), null,
                "break");
        PDGGraph pdg = new PDGGraph(context, node);
        pdg.breaks.add(node);
        pdg.sinks.remove(node);
        pdg.statementSinks.remove(node);
        return pdg;

    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Import astNode) {
        return new PDGGraph(context);

    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              ImportFrom astNode) {
        return new PDGGraph(context);

    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Slice astNode) {
        PDGGraph[] pdgs = new PDGGraph[3];
        if (astNode.getInternalLower() != null) {
            PDGGraph pdgGraph = buildArgumentPDG(control, branch, astNode.getInternalLower());
            pdgs[0] = pdgGraph;
        }
        if (astNode.getInternalStep() != null) {
            PDGGraph pdgGraph = buildArgumentPDG(control, branch, astNode.getInternalStep());
            pdgs[1] = pdgGraph;
        }
        if (astNode.getInternalUpper() != null) {
            PDGGraph pdgGraph = buildArgumentPDG(control, branch, astNode.getInternalUpper());
            pdgs[2] = pdgGraph;
        }
        String type = null;
        if (Arrays.stream(pdgs).anyMatch(Objects::nonNull))
            type = Arrays.stream(pdgs).filter(Objects::nonNull).collect(Collectors.toList()).get(0).getOnlyOut().getDataType();

        if (type != null && !type.endsWith("]"))
            type = type + "[.]";
        PDGNode node = new PDGActionNode(astNode, astNode.getNodeType(),
                "" + astNode.getCharStartIndex(), type,
                astNode.toString());
        for (PDGGraph pg : Arrays.stream(pdgs).filter(Objects::nonNull).collect(Collectors.toList())) {
            pg.mergeSequentialData(node, PARAMETER);
        }
        PDGGraph pdg = new PDGGraph(context);

        pdg.mergeParallel(Arrays.stream(pdgs).filter(Objects::nonNull).toArray(PDGGraph[]::new));
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              FunctionDef astNode) {
        return new PDGGraph(context);

    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              AsyncFunctionDef astNode) {
        return new PDGGraph(context);
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              ExtSlice astNode) {

        PDGGraph[] pgs = new PDGGraph[astNode.getInternalDims().size()];

        if (astNode.getInternalDims().size() <= 10) {
            for (int i = 0; i < astNode.getInternalDims().size(); i++)
                pgs[i] = buildArgumentPDG(control, branch, astNode.getInternalDims().get(i));
        } else
            pgs = new PDGGraph[0];

        PDGNode node = new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), null, ":", "<:>");
        if (pgs.length > 0) {
            for (PDGGraph pg : pgs)
                pg.mergeSequentialData(node, PARAMETER);
            PDGGraph pdg = new PDGGraph(context);
            pdg.mergeParallel(pgs);
            return pdg;
        } else
            return new PDGGraph(context, node);
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Delete astNode) {
        PDGGraph[] pdgs = new PDGGraph[astNode.getInternalTargets().size()];

        PDGNode node = new PDGActionNode(control, branch, astNode, astNode.getNodeType(), null, null, "delete");
        for (int i = 0; i < pdgs.length; i++) {
            pdgs[i] = buildArgumentPDG(control, branch, astNode.getInternalTargets().get(i));
            pdgs[i].mergeSequentialData(node, PARAMETER);
        }
        PDGGraph pdg = new PDGGraph(context);
        pdg.mergeParallel(pdgs);
        // skip astNode.getMessage()
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Pass astNode) {
        return new PDGGraph(context, new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), null, null, "pass"));
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Assert astNode) {
        return new PDGGraph(context, new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), null, null, "assert"));
//        if (astNode.getInternalTest()==null && astNode.getInternalMsg()==null )
//            return new PDGGraph(context, new PDGActionNode(control, branch,
//                    astNode, astNode.getNodeType(), null, null, "assert"));
//        else if (astNode.getInternalTest()!=null && astNode.getInternalMsg()==null){
//            PDGGraph pdg = buildArgumentPDG(control, branch,
//                    astNode.getInternalTest());
//            PDGNode node = new PDGActionNode(control, branch,
//                    astNode, astNode.getNodeType(), null, null, "assert");
//            pdg.mergeSequentialData(node,  PARAMETER);
//            // skip astNode.getMessage()
//            return pdg;
//        }
//        else if (astNode.getInternalTest()==null && astNode.getInternalMsg()!=null){
//            PDGGraph pdg = buildArgumentPDG(control, branch,
//                    astNode.getInternalMsg());
//            PDGNode node = new PDGActionNode(control, branch,
//                    astNode, astNode.getNodeType(), null, null, "assert");
//            pdg.mergeSequentialData(node,  PARAMETER);
//            // skip astNode.getMessage()
//            return pdg;
//        }
//        else{
//
//        }

    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              IfExp astNode) {

        PDGDataNode dummy = new PDGDataNode(null, PyObject.NAME,
                PDGNode.PREFIX_DUMMY + astNode.getCharStopIndex(), "boolean", PDGNode.PREFIX_DUMMY, false, true);

        PDGGraph pdg = buildArgumentPDG(control, branch,
                astNode.getInternalTest());
        PDGControlNode node = new PDGControlNode(control, branch,
                astNode, PyObject.IFEXP);
        pdg.mergeSequentialData(node, CONDITION);
        PDGGraph tg = buildArgumentPDG(node, "T", astNode.getInternalBody());
        tg.mergeSequentialData(new PDGActionNode(node, "T", null, PyObject.ASSIGN,
                null, null, "="), PARAMETER);
        PDGGraph fg = buildArgumentPDG(node, "F", astNode.getInternalOrelse());
        fg.mergeSequentialData(new PDGActionNode(node, "F", null, PyObject.ASSIGN,
                null, null, "="), PARAMETER);
        fg.mergeSequentialData(new PDGDataNode(dummy), DEFINITION);
        pdg.mergeBranches(tg, fg);
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Raise astNode) {
        PDGActionNode node = new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), null, null, "raise");
        if (astNode.getInternalExc() != null) {
            PDGGraph pdg = buildArgumentPDG(control, branch, astNode.getInternalExc());

            pdg.mergeSequentialData(node, PARAMETER);
            pdg.sinks.remove(node);
            pdg.statementSinks.remove(node);
            return pdg;
        } else {
            PDGGraph pdg = new PDGGraph(context, node);
            pdg.sinks.remove(node);
            pdg.statementSinks.remove(node);
            return pdg;
        }
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              TryFinally astNode) {
        context.pushTry();
        PDGGraph pdg = new PDGGraph(context);
        PDGControlNode node = new PDGControlNode(control, branch,
                astNode, astNode.getNodeType());
        pdg.mergeSequentialControl(node, "");

        PDGGraph[] gs = new PDGGraph[2];
        gs[0] = buildPDG(node, "T", astNode.getBody());
        gs[1] = buildPDG(node, "F", astNode.getFinalbody());
        pdg.mergeBranches(gs);
        context.popTry();
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Continue astNode) {
        PDGActionNode node = new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), "", null,
                "continue");
        PDGGraph pdg = new PDGGraph(context, node);
        pdg.breaks.add(node);
        pdg.sinks.remove(node);
        pdg.statementSinks.remove(node);
        return pdg;
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Dict astNode) {
        java.util.List<expr> internalKeys = astNode.getInternalKeys();
        java.util.List<expr> internalValues = astNode.getInternalValues();
        PDGGraph[] pgs = new PDGGraph[internalKeys.size()];
        if (internalKeys.size() <= 10) {
            for (int i = 0; i < internalKeys.size(); i++) {
                PDGGraph[] pdgs_dic_items = new PDGGraph[2];
                expr keys = internalKeys.get(i);
                expr value = internalValues.get(i);
                String keyTypes = context.getTypeWrapper().getTypeInfo(keys.getLine(), keys.getCharPositionInLine());
                String valueTypes = context.getTypeWrapper().getTypeInfo(keys.getLine(), keys.getCharPositionInLine());
                PDGDataNode dataNode = new PDGDataNode(
                        astNode, PyObject.DICTIONARY_ITEM, "key:" + keys + ",value:" + value, "key:" + keyTypes + ",value:" + valueTypes,
                        "key:" + keys + ",value:" + value);
                PDGGraph keyPDG = buildArgumentPDG(control, branch, keys);
                keyPDG.mergeSequentialData(dataNode, PARAMETER);
                PDGGraph valuePDG = buildArgumentPDG(control, branch, value);
                valuePDG.mergeSequentialData(dataNode, PARAMETER);
                PDGGraph pdg = new PDGGraph(context);
                pdgs_dic_items[0] = keyPDG;
                pdgs_dic_items[1] = valuePDG;
                pdg.mergeParallel(pdgs_dic_items);
                pgs[i] = pdg;
            }
        } else
            pgs = new PDGGraph[0];

        PDGNode node = new PDGActionNode(control, branch,
                astNode, astNode.getNodeType(), null, "{}", "{new}");
        if (pgs.length > 0) {
            for (PDGGraph pg : pgs)
                pg.mergeSequentialData(node, PARAMETER);
            PDGGraph pdg = new PDGGraph(context);
            pdg.mergeParallel(pgs);
            return pdg;
        } else
            return new PDGGraph(context, node);

    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              ErrorExpr astNode) {
        return new PDGGraph(context);
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              ErrorStmt astNode) {
        return new PDGGraph(context);
    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              ClassDef astNode) {
        return new PDGGraph(context);
    }

    private String getFullNameOfAttribute(Attribute atr) {
        if (atr.getInternalValue() instanceof Name)
            return ((Name) atr.getInternalValue()).getInternalId() + atr.getInternalAttr();
        else if (atr.getInternalValue() instanceof AlphanumericHole)
            return (atr.getInternalValue()).toString() + atr.getInternalAttr();
        else if (atr.getInternalValue() instanceof LazyHole)
            return (atr.getInternalValue()).toString() + atr.getInternalAttr();
        else if (atr.getInternalValue() instanceof Subscript) {
            return getFullNameOfAttribute((Subscript) atr.getInternalValue()) + "." + atr.getInternalAttr();
        } else if (atr.getInternalValue() instanceof Attribute) {
            return getFullNameOfAttribute((Attribute) atr.getInternalValue()) + atr.getInternalAttr();
        }
        else if (atr.getInternalValue() instanceof Call) {
            return getFullNameOfAttribute((Call) atr.getInternalValue()) + atr.getInternalAttr();
        }
        else if (atr.getInternalValue() instanceof Str) {
            return ((Str) atr.getInternalValue()).getInternalS() + atr.getInternalAttr();
        }
        else {
            return "";
        }
    }

    private String getFullNameOfAttribute(Call atr) {
        if (atr.getInternalFunc() instanceof Name){
            return ((Name)atr.getInternalFunc()).getInternalId()+"()";
        }
        else if (atr.getInternalFunc() instanceof Attribute){
            return getFullNameOfAttribute(((Attribute)atr.getInternalFunc()))+"()";
        }
        else{
            return "";
        }
    }

    private String getFullNameOfAttribute(Subscript atr) {
        if (atr.getInternalValue() instanceof Name && atr.getInternalSlice() instanceof Index) {
            if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Num)
                return ((Name) atr.getInternalValue()).getInternalId() + "[" + ((Num) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalN() + "]";
            if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Name)
                return ((Name) atr.getInternalValue()).getInternalId() + "[" + ((Name) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalId() + "]";
            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Str)
                return ((Name) atr.getInternalValue()).getInternalId() + "[" + ((Str) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalS() + "]";
            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof BinOp)
                return ((Name) atr.getInternalValue()).getInternalId() + "[" + ((BinOp) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalRight()
                        + ((BinOp) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalOp().name() +
                        ((BinOp) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalLeft() + "]";
            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Subscript)
                return ((Name) atr.getInternalValue()).getInternalId() + "[" + getFullNameOfAttribute( (Subscript)((Index) atr.getInternalSlice()).getInternalValue())  + "]";
            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof List){
                return ((Name) atr.getInternalValue()).getInternalId() +"[]";
            }
            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Attribute){
                return ((Name) atr.getInternalValue()).getInternalId() + "[" + getFullNameOfAttribute((Attribute)((Index) atr.getInternalSlice()).getInternalValue()) + "]";
            }
            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Call){
                return ((Name) atr.getInternalValue()).getInternalId() + "[" + getFullNameOfAttribute((Call)((Index) atr.getInternalSlice()).getInternalValue()) + "]";
            }
            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Str)
                return ((Name) atr.getInternalValue()).getInternalId() + "[" + ((Str) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalS() + "]";
            else
                return ((Name) atr.getInternalValue()).getInternalId() + "[" +  "]";
        } else if (atr.getInternalValue() instanceof AlphanumericHole)
            return (atr.getInternalValue()).toString() + "[" + ((Num) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalN() + "]";
        else if (atr.getInternalValue() instanceof LazyHole)
            return (atr.getInternalValue()).toString() + "[" + ((Num) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalN() + "]";
        else if (atr.getInternalValue() instanceof Subscript && atr.getInternalSlice() instanceof Index) {
            if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Num)
                return getFullNameOfAttribute((Subscript) atr.getInternalValue()) + "[" + ((Num) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalN() + "]";
            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Name)
                return getFullNameOfAttribute((Subscript) atr.getInternalValue()) + "[" + ((Name) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalId() + "]";
            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Attribute){
                return getFullNameOfAttribute((Subscript) atr.getInternalValue()) + "[" + getFullNameOfAttribute ((Attribute) ((Index) atr.getInternalSlice()).getInternalValue()) + "]";
            }
            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Str){
                return getFullNameOfAttribute((Subscript) atr.getInternalValue()) + "[" + ((Str) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalS() + "]";
            }
            else
                return getFullNameOfAttribute((Subscript) atr.getInternalValue()) + "[" +  "]";
        }
        else if (atr.getInternalSlice() instanceof ExtSlice){
            return  "::";
        }
        else if (atr.getInternalSlice() instanceof Slice){
            return  ":";
        }
        else if (atr.getInternalValue() instanceof Attribute) {
            if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Num)
                return getFullNameOfAttribute((Attribute) atr.getInternalValue()) + "[" + ((Num) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalN() + "]";
            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Name)
                return getFullNameOfAttribute((Attribute) atr.getInternalValue()) + "[" + ((Name) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalId() + "]";
            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Subscript)
                return getFullNameOfAttribute((Attribute) atr.getInternalValue()) + "[ ]";
            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Call){
                return getFullNameOfAttribute((Attribute) atr.getInternalValue()) + "[" + getFullNameOfAttribute((Call)((Index) atr.getInternalSlice()).getInternalValue()) + "]";
            }
            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Attribute){
                return getFullNameOfAttribute((Attribute) atr.getInternalValue()) + "[" + getFullNameOfAttribute((Attribute)((Index) atr.getInternalSlice()).getInternalValue()) + "]";
            }
            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof UnaryOp){
                return getFullNameOfAttribute((Attribute) atr.getInternalValue()) + "[" + ((UnaryOp) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalOp().toString() + "]";
            }
            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Str){
                return getFullNameOfAttribute((Attribute) atr.getInternalValue()) + "[" + ((Str) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalS() + "]";
            }
            else
                return getFullNameOfAttribute((Attribute) atr.getInternalValue()) + "[ ]";
        } else if (atr.getInternalValue() instanceof Subscript) {
            if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Num)
                return getFullNameOfAttribute((Subscript) atr.getInternalValue()) + "[" + ((Num) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalN() + "]";
            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Name)
                return getFullNameOfAttribute((Subscript) atr.getInternalValue()) + "[" + ((Name) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalId() + "]";
            else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Subscript)
                return getFullNameOfAttribute((Subscript) atr.getInternalValue()) + "[ ]";
            else
                return getFullNameOfAttribute((Subscript) atr.getInternalValue()) + "[" + ((Str) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalS() + "]";
        } else if (atr.getInternalValue() instanceof Call) {
            if (((Call) atr.getInternalValue()).getInternalFunc() instanceof Name) {
                String fuName = ((Name) ((Call) atr.getInternalValue()).getInternalFunc()).getInternalId() + "()";
                if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Num)
                    return fuName + "[" + ((Num) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalN() + "]";
                else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Name)
                    return fuName + "[" + ((Name) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalId() + "]";
                else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Subscript)
                    return fuName + "[ ]";
                else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Str)
                    return fuName + "[" + ((Str) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalS() + "]";
                else
                    return fuName + "[" + "]";
            }
            else{
                if (atr.getInternalSlice() instanceof Index &&  ((Call) atr.getInternalValue()).getInternalFunc() instanceof Attribute ) {
                    if ( ((Index) atr.getInternalSlice()).getInternalValue() instanceof Num)
                        return getFullNameOfAttribute((Attribute) ((Call) atr.getInternalValue()).getInternalFunc())
                                + "[" + ((Num) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalN() + "]";
                    else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Name)
                        return getFullNameOfAttribute((Attribute) ((Call) atr.getInternalValue()).getInternalFunc())
                                + "[" + ((Name) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalId() + "]";
                    else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Subscript)
                        return getFullNameOfAttribute((Attribute) ((Call) atr.getInternalValue()).getInternalFunc())
                                + "[ ]";

                    else if (((Index) atr.getInternalSlice()).getInternalValue() instanceof Str)
                        return getFullNameOfAttribute((Attribute) ((Call) atr.getInternalValue()).getInternalFunc())
                                + "[" + ((Str) ((Index) atr.getInternalSlice()).getInternalValue()).getInternalS() + "]";
                    else
                        return getFullNameOfAttribute((Attribute) ((Call) atr.getInternalValue()).getInternalFunc())
                                + "["  + "]";
                }
                else if (((Call) atr.getInternalValue()).getInternalFunc() instanceof Subscript)
                    return getFullNameOfAttribute((Subscript) ((Call) atr.getInternalValue()).getInternalFunc());
                else if (((Call) atr.getInternalValue()).getInternalFunc() instanceof Attribute){
                    return getFullNameOfAttribute((Attribute) ((Call) atr.getInternalValue()).getInternalFunc());
                }
                else{
                    return "";
                }

            }
        }
        else {

            return "[]";
        }
    }
}


