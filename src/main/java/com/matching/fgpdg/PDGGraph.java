package com.matching.fgpdg;



import com.matching.fgpdg.nodes.PDGNode;
import org.python.antlr.ast.FunctionDef;

import java.io.Serializable;
import java.util.HashSet;

public class PDGGraph  implements Serializable {
    private static final long serialVersionUID = -5128703931982211886L;

    public HashSet<PDGNode> getNodes() {
        return nodes;
    }

    private HashSet<PDGNode> nodes = new HashSet<PDGNode>();

    private PDGBuildingContext context;
    public PDGGraph(FunctionDef md, PDGBuildingContext context) {
        this.context=context;
        this.context.setMethod(md);
    }

    public PDGGraph(PDGBuildingContext context){
        this.context=context;
    }
}
