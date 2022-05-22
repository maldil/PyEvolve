package com.matching.fgpdg.nodes.ast;

import org.python.antlr.ast.Hole;

public class LazyHole extends Hole {

    @Override
    public String toString() {
        return ":[l" + getN() + "]";
    }
}
