package com.adaptrule;

import com.matching.fgpdg.nodes.Guards;

public class Rule {
    private Guards guard;
    private String LHS;
    private String RHS;

    public void setGuard(Guards guard) {
        this.guard = guard;
    }

    public void setLHS(String LHS) {
        this.LHS = LHS;
    }

    public void setRHS(String RHS) {
        this.RHS = RHS;
    }

    public Guards getGuard() {
        return guard;
    }

    public String getLHS() {
        return LHS;
    }

    public String getRHS() {
        return RHS;
    }
}
