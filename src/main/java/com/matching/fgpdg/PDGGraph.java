package com.matching.fgpdg;

import com.matching.fgpdg.nodes.*;
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

import static com.matching.fgpdg.nodes.PDGDataEdge.Type.*;


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
    protected HashSet<PDGNode> changedNodes = new HashSet<>();
    private PDGBuildingContext context;
    private HashMap<String, HashSet<PDGDataNode>> defStore = new HashMap<>();

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

    public PDGGraph(Module md, PDGBuildingContext context) {
        this.context = context;
        context.addScope();
        entryNode = new PDGEntryNode(md, PyObject.MODULE, "START");
        nodes.add(entryNode);
        statementNodes.add(entryNode);
        for (stmt stmt : md.getInternalBody()) {
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
            if (node.isDefinition())
                defs.add((PDGDataNode) node);
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
        if (pdg.isEmpty())
            return pdg;
        if (pdg.nodes.size() == 1)
            for (PDGNode node : pdg.nodes)
                if (node instanceof PDGDataNode)
                    return pdg;
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
        throw new RuntimeException("ERROR in getting the only output node!!!");
//		System.err.println("ERROR in getting the only output node!!!");
//		System.exit(-1);
//		return null;
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
            context.addLocalVariable(varName, "" + astNode.getInternalTarget().getCharStartIndex(), varType);
            PDGDataNode varp = new PDGDataNode(astNode.getInternalTarget(), astNode.getInternalTarget().getNodeType(),
                    "" + astNode.getInternalTarget().getCharStartIndex(), varType,
                    varName, false, true);
            pdg.mergeSequentialData(varp, PDGDataEdge.Type.DEFINITION);
            pdg.mergeSequentialData(new PDGDataNode(null, varp.getAstNodeType(),
                    varp.getKey(), varp.getDataType(), varp.getDataName()), REFERENCE);
        } else if (astNode.getInternalTarget() instanceof Tuple) {
//            PDGGraph gt = buildArgumentPDG(control,branch,astNode.getInternalTarget());
//            pdg.mergeBranches(gt);

            for (expr var : ((Tuple) astNode.getInternalTarget()).getInternalElts()) {
                String name = ((Name) var).getInternalId();
                String type = context.getTypeWrapper().getTypeInfo(((Name) var).getLineno(), ((Name) var).getCol_offset());
                context.addLocalVariable(name, "" + var.getCharStartIndex(), type);
                PDGDataNode varNode = new PDGDataNode(var, var.getNodeType(),
                        "" + var.getCharStartIndex(), type,
                        name, false, true);
                pdg.mergeSequentialData(varNode, PDGDataEdge.Type.DEFINITION);
                pdg.mergeSequentialData(new PDGDataNode(null, varNode.getAstNodeType(),
                        varNode.getKey(), varNode.getDataType(), varNode.getDataName()), REFERENCE);
            }
        } else {
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
        ArrayList<PDGDataNode> ldatanodes = new ArrayList<>();
        if (astNode.getInternalTargets().size() > 1) {
            PDGGraph[] pgs = new PDGGraph[astNode.getInternalTargets().size()];
            for (int i = 0; i < astNode.getInternalTargets().size(); i++) {
                if (astNode.getInternalTargets().get(i) instanceof Name &&
                        context.getTypeWrapper().getTypeInfo(astNode.getInternalTargets().get(i).getLine(),
                                astNode.getInternalTargets().get(i).getCharPositionInLine()) != null&&
                        context.getLocalVariableInfo(((Name) astNode.getInternalTargets().get(i)).getInternalId())==null


                ) {
                    context.addLocalVariable
                            (((Name) astNode.getInternalTargets().get(i)).getInternalId(), "" +
                                            astNode.getInternalTargets().get(i).getCharStartIndex(),
                                    context.getTypeWrapper().getTypeInfo(astNode.getInternalTargets().get(i).getLine(),
                                            astNode.getInternalTargets().get(i).getCharPositionInLine()));

                }
                pgs[i] = buildArgumentPDG(control, branch, astNode.getInternalTargets().get(i));

                ldatanodes.add(pgs[i].getOnlyDataOut());
            }
            lg = new PDGGraph(context);
            lg.mergeParallel(pgs);
        } else {
            if (astNode.getInternalTargets().get(0) instanceof Name &&
                    context.getTypeWrapper().getTypeInfo(astNode.getInternalTargets().get(0).getLine(),
                            astNode.getInternalTargets().get(0).getCharPositionInLine()) != null&&
                    context.getLocalVariableInfo(((Name)astNode.getInternalTargets().get(0)).getInternalId())==null) {
                context.addLocalVariable
                        (((Name) astNode.getInternalTargets().get(0)).getInternalId(), "" +
                                        astNode.getInternalTargets().get(0).getCharStartIndex(),
                                context.getTypeWrapper().getTypeInfo(astNode.getInternalTargets().get(0).getLine(),
                                        astNode.getInternalTargets().get(0).getCharPositionInLine()));

            }
            lg = buildPDG(control, branch, astNode.getInternalTargets().get(0));
            ldatanodes.add(lg.getOnlyDataOut());
        }

        PDGGraph rg = buildPDG(control, branch, astNode.getInternalValue());
        ArrayList<PDGActionNode> rets = rg.getReturns();
        if (rets.size() > 0) {
            Assertions.UNREACHABLE();
        }
        ArrayList<PDGDataNode> defs = rg.getDefinitions();
        if (defs.isEmpty()) {
            rg.mergeSequentialData(new PDGActionNode(control, branch,
                    astNode, PyObject.ASSIGN, null, null, "="), PARAMETER);
            for (PDGDataNode ldatanode : ldatanodes) {
                rg.mergeSequentialData(ldatanode, PDGDataEdge.Type.DEFINITION);
            }
        } else {
            if (defs.get(0).isDummy()) {
                for (PDGDataNode def : defs) {
                    rg.defStore.remove(def.getKey());
                    for (PDGDataNode ldatanode : ldatanodes) {
                        def.copyData(ldatanode);
                    }
                    HashSet<PDGDataNode> ns = rg.defStore.get(def.getKey());
                    if (ns == null) {
                        ns = new HashSet<>();
                        rg.defStore.put(def.getKey(), ns);
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
                for (PDGDataNode ldatanode : ldatanodes) {
                    rg.mergeSequentialData(ldatanode, PDGDataEdge.Type.DEFINITION);
                }
            }
        }
        rg.nodes.addAll(lg.nodes);
        rg.statementNodes.addAll(lg.statementNodes);
        for (PDGDataNode ldatanode : ldatanodes) {
            lg.dataSources.remove(ldatanode);
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
                String[] info = null;
                if (astNode.getInternalElts().get(i) instanceof Name) {
                    info = context.getLocalVariableInfo(((Name) astNode.getInternalElts().get(i)).getInternalId());
                }
                if (info != null) {
                    pgs[i] = new PDGGraph(context, new PDGDataNode(
                            (astNode.getInternalElts().get(i)), (astNode.getInternalElts().get(i)).getNodeType(), info[0], info[1],
                            ((Name) astNode.getInternalElts().get(i)).getInternalId(), false, false));
                } else if (astNode.getInternalElts().get(i) instanceof Name) {
                    context.addLocalVariable(
                            ((Name) astNode.getInternalElts().get(i)).getInternalId(), "" +
                                    (astNode.getInternalElts().get(i)).getCharStartIndex(),
                            context.getTypeWrapper().getTypeInfo((astNode.getInternalElts().get(i)).getLine(),
                                    ((Name) (astNode.getInternalElts().get(i))).getCol_offset()));
                    pgs[i] = buildArgumentPDG(control, branch, astNode.getInternalElts().get(i));
                } else {
                    pgs[i] = buildArgumentPDG(control, branch, astNode.getInternalElts().get(i));
                }
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
        } else if (context.getTypeWrapper().getTypeInfo(astNode.getLineno(), astNode.getCol_offset()) != null) {
            return new PDGGraph(context, new PDGDataNode(
                    astNode, astNode.getNodeType(), name,
                    context.getTypeWrapper().getTypeInfo(astNode.getLineno(), astNode.getCol_offset()),
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
        if (astNode.getInternalFunc() instanceof Attribute) {
            pgs[0] = buildArgumentPDG(control, branch,
                    ((Attribute) astNode.getInternalFunc()).getInternalValue());
            node = new PDGActionNode(control, branch,
                    astNode, astNode.getNodeType(), null,
                    pgs[0].getOnlyOut().getDataType() + "." + ((Attribute) astNode.getInternalFunc()).getInternalAttr() + "()",
                    ((Attribute) astNode.getInternalFunc()).getInternalAttr());
        } else if (astNode.getInternalFunc() instanceof Name) {
            pgs[0] = new PDGGraph(context);

            node = new PDGActionNode(control, branch,
                    astNode, astNode.getNodeType(), null,
                    "function" + "." + ((Name) astNode.getInternalFunc()).getInternalId() + "()",
                    ((Name) astNode.getInternalFunc()).getInternalId());
        }
        if (astNode.getInternalArgs().size() <= 100)
            for (int i = 0; i < astNode.getInternalArgs().size(); i++)
                pgs[i + 1] = buildArgumentPDG(control, branch, astNode.getInternalArgs().get(i));

        PDGGraph pdg = null;
        pgs[0].mergeSequentialData(node, PDGDataEdge.Type.RECEIVER);
        if (pgs.length > 0) {
            for (int i = 1; i < pgs.length; i++)
                pgs[i].mergeSequentialData(node, PARAMETER);
            pdg = new PDGGraph(context);
            pdg.mergeParallel(pgs);
        } else
            pdg = new PDGGraph(context, node);
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
        String type = pdg.getOnlyOut().getDataType();

        if (type != null && !type.endsWith("]"))
            type = type + "[.]";
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
            rg.mergeSequentialData(lnode, DEFINITION);

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
                astNode, astNode.getNodeType(), null, "[]", "<new>");
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
                              ListComp astNode) {
        context.addScope();
        PDGNode[] targeNodes;
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
                String name = ((Name) elt).getInternalId();
                String type = context.getTypeWrapper().getTypeInfo(elt.getLine(), elt.getCharPositionInLine());
                if (type == null)
                    type = context.getTypeWrapper().getTypeInfo(name);
                context.addLocalVariable(name, "" + elt.getCharStartIndex(), type);
                PDGDataNode pdn = new PDGDataNode(elt, elt.getNodeType(), "" + elt.getCharStartIndex(), type,
                        name, false, true);
                loopVariables.add(pdn);
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
                PDGGraph target = buildArgumentPDG(ifnode, "T", (expr) astNode.getProperty("target"));
//                target.mergeSequentialData(new PDGActionNode(ifnode, "T", null, PyObject.ASSIGN,
//                        null, null, "="), PARAMETER);
                target.mergeSequentialDataNoUpdatetoSinks(node, CONDITION);
                target.mergeSequentialDataNoUpdatetoSinks((PDGControlNode) astNode.getProperty("listNode"), PARAMETER);

                pdg.mergeBranches(target);
                ifpdgs[j] = pdg;
                j += 1;
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
        PDGGraph pdg=new PDGGraph(context);;
        PDGActionNode node = null;
        if (astNode.getInternalNameNodes().size()>0){
            pdgs= new PDGGraph[astNode.getInternalNameNodes().size()];
            for (int i=0;i<astNode.getInternalNameNodes().size();i++){
                pdgs[i]=buildArgumentPDG(control,branch,astNode.getInternalNameNodes().get(i));
            }
            pdg.mergeParallel(pdgs);
            node = new PDGActionNode(control, branch, astNode, astNode.getNodeType(),
                    null, null, "Global");
            pdg.mergeSequentialData(node,  PARAMETER);
        }
        else{
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
        if (astNode.getInternalValue()!=null){
            pdg = buildArgumentPDG(control, branch, astNode.getInternalValue());
            node = new PDGActionNode(control, branch, astNode, astNode.getNodeType(),
                    null, null, "yield");
            pdg.mergeSequentialData(node, PARAMETER);
        }
        else{
            node = new PDGActionNode(control, branch, astNode, astNode.getNodeType(),
                    null, null, "yield");
            pdg = new PDGGraph(context, node);
        }
        return pdg;
    }

}


