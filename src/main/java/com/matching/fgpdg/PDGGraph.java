package com.matching.fgpdg;

import com.matching.fgpdg.nodes.*;
import com.utils.Assertions;
import org.python.antlr.ast.*;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;
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
    protected HashSet<PDGNode> changedNodes = new HashSet<>();
    private PDGBuildingContext context;
    private HashMap<String, HashSet<PDGDataNode>> defStore = new HashMap<>();

    public PDGGraph(FunctionDef md, PDGBuildingContext context) {
        this.context = context;
        context.addScope();
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
//            this.parameters[numOfParameters++] = new PDGDataNode(
//                    arg, PyObject.NAME, info[0], info[1],
//                    "PARAM_" + arg.getInternalArg(), false, true);
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
        String type = TypeWrapper.getTypeInfo(astNode.getLine(), astNode.getCharPositionInLine(), name);
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
                    def.isField(), false), PDGDataEdge.Type.REFERENCE);
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
                    dummy.getKey(), dummy.getDataType(), dummy.getDataName()), PDGDataEdge.Type.REFERENCE);
            return pdg;
        }
        PDGNode node = pdg.getOnlyOut();
        if (node instanceof PDGDataNode)
            return pdg;
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
                        + length, node.getDataType(), PDGNode.PREFIX_DUMMY, false, true);
        pdg.mergeSequentialData(new PDGActionNode(control, branch,
                null, PyObject.ASSIGN, null, null, "="), PDGDataEdge.Type.PARAMETER);
        pdg.mergeSequentialData(dummy, PDGDataEdge.Type.DEFINITION);
        pdg.mergeSequentialData(new PDGDataNode(null, dummy.getAstNodeType(), dummy.getKey(),
                dummy.getDataType(), dummy.getDataName()), PDGDataEdge.Type.REFERENCE);
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
                    else if (((PDGDataEdge) e).getType() == PDGDataEdge.Type.PARAMETER)
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

        if (astNode.getInternalTarget() instanceof Name) {
            String varName = ((Name) astNode.getInternalTarget()).getInternalId();
            String varType = TypeWrapper.getTypeInfo(((Name) astNode.getInternalTarget()).getLineno(),
                    ((Name) astNode.getInternalTarget()).getCol_offset(), varName);
            context.addLocalVariable(varName, "" + astNode.getInternalTarget().getCharStartIndex(), varType);
            PDGDataNode varp = new PDGDataNode(astNode.getInternalTarget(), astNode.getInternalTarget().getNodeType(),
                    "" + astNode.getInternalTarget().getCharStartIndex(), varType,
                    varName, false, true);
            pdg.mergeSequentialData(varp, PDGDataEdge.Type.DEFINITION);
            pdg.mergeSequentialData(new PDGDataNode(null, varp.getAstNodeType(),
                    varp.getKey(), varp.getDataType(), varp.getDataName()), PDGDataEdge.Type.REFERENCE);
        } else if (astNode.getInternalTarget() instanceof Tuple) {
            for (expr var : ((Tuple) astNode.getInternalTarget()).getInternalElts()) {
                String name = ((Name) var).getInternalId();
                String type = TypeWrapper.getTypeInfo(((Name) var).getLineno(), ((Name) var).getCol_offset(), name);
                context.addLocalVariable(name, "" + var.getCharStartIndex(), type);
                PDGDataNode varNode = new PDGDataNode(var, var.getNodeType(),
                        "" + var.getCharStartIndex(), type,
                        name, false, true);
                pdg.mergeSequentialData(varNode, PDGDataEdge.Type.DEFINITION);
                pdg.mergeSequentialData(new PDGDataNode(null, varNode.getAstNodeType(),
                        varNode.getKey(), varNode.getDataType(), varNode.getDataName()), PDGDataEdge.Type.REFERENCE);
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
                pgs[i] = buildArgumentPDG(control, branch, astNode.getInternalTargets().get(i));
                ldatanodes.add(pgs[i].getOnlyDataOut());
            }
            lg = new PDGGraph(context);
            lg.mergeParallel(pgs);
        } else {
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
                    astNode, PyObject.ASSIGN, null, null, "="), PDGDataEdge.Type.PARAMETER);
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
                        PDGDataEdge.Type.REFERENCE);
                rg.mergeSequentialData(new PDGActionNode(control, branch,
                        astNode, PyObject.ASSIGN, null, null, "="), PDGDataEdge.Type.PARAMETER);
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
        Assertions.UNREACHABLE();

        return null;

    }

    private PDGGraph buildPDG(PDGNode control, String branch,
                              Tuple astNode) {
        Assertions.UNREACHABLE();

        return null;

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
            PDGGraph pdg = new PDGGraph(context, new PDGDataNode(
                    astNode, astNode.getNodeType(), info[0], info[1],
                    name, false, false));
            return pdg;
        }
        String type = context.getFieldType(name); //TODO update get filed types
        if (type != null) {
            PDGGraph pdg = new PDGGraph(context, new PDGDataNode(
                    null, PyObject.SELF_EXPRESSION, "self",
                    "self", "self"));
            pdg.mergeSequentialData(new PDGDataNode(astNode, PyObject.FIELD_ACCESS,
                    "self." + name, type, name, true,
                    false), PDGDataEdge.Type.QUALIFIER);
            return pdg;
        }
        if (Character.isUpperCase(name.charAt(0))) {
            PDGGraph pdg = new PDGGraph(context, new PDGDataNode(
                    astNode, astNode.getNodeType(), name, name,
                    name, false, false));
            return pdg;
        } else if (context.getImportsMap().containsKey(name)) {
            PDGGraph pdg = new PDGGraph(context, new PDGDataNode(
                    astNode, astNode.getNodeType(), name, name,
                    name, false, false));
            return pdg;
        }
        PDGGraph pdg = new PDGGraph(context, new PDGDataNode(
                null, PyObject.SELF_EXPRESSION, "self",
                "self", "self"));
        pdg.mergeSequentialData(new PDGDataNode(astNode, PyObject.FIELD_ACCESS,
                "self." + name, "UNKNOWN", name, true,
                false), PDGDataEdge.Type.QUALIFIER);
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
                    pgs[0].getOnlyOut().getDataType() + "." + ((Name) astNode.getInternalFunc()).getInternalId() + "()",
                    ((Name) astNode.getInternalFunc()).getInternalId());
        }
        if (astNode.getInternalArgs().size() <= 100)
            for (int i = 0; i < astNode.getInternalArgs().size(); i++)
                pgs[i + 1] = buildArgumentPDG(control, branch, astNode.getInternalArgs().get(i));

        PDGGraph pdg = null;
        pgs[0].mergeSequentialData(node, PDGDataEdge.Type.RECEIVER);
        if (pgs.length > 0) {
            for (int i = 1; i < pgs.length; i++)
                pgs[i].mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
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
            pdg.mergeSequentialData(node, PDGDataEdge.Type.PARAMETER);
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
}
