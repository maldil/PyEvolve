package com.inferrules.core;

import com.inferrules.core.languageAdapters.Language;
import org.antlr.v4.runtime.misc.Interval;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the node of the parse tree; basically it is a wrapper of @org.antlr.v4.runtime.tree.ParseTree.
 */
public class Node {


    /**
     * Captures the string value of the node represented.
     * It includes spaces, newlines or tabs.
     */
    private final String value;
    private final  Language language;
    private final boolean isKeyword;
    private final boolean isSymbol;
    private final List<Node> children;
    private final Interval sourceInterval;
    /**
     * Captures the string value of the node represented.
     * It DOES NOT includes spaces, newlines or tabs.
     * It is useful for checking if two nodes are equal
     */
    private final String text;



    public Node(String value,  Language language, Interval sourceInterval, String text) {
        this.text = text;
        this.value = value;
        this.language = language;
        this.sourceInterval = sourceInterval;
        this.isKeyword = language.getKwds().contains(value);
        this.isSymbol = !isKeyword && language.getKwdSymbols().contains(value);
        this.children = new ArrayList<>();
    }

    public Node(String value, List<Node> children,  Language language, Interval sourceInterval, String text) {
        this.text = text;
        this.value = value;
        this.language = language;
        this.sourceInterval = sourceInterval;
        this.isKeyword = language.getKwds().contains(value);
        this.isSymbol = !isKeyword && language.getKwdSymbols().contains(value);
        this.children = children;
    }

    public String getValue() {
        return value;
    }

    public List<Node> getChildren() {
        return children;
    }

    public boolean isSymbol() {
        return isSymbol;
    }

    public boolean isKeyword() {
        return isKeyword;
    }

    public boolean isNotKwdOrSymb() {
        return !isKeyword() && !isSymbol() && !value.isBlank() && !value.equals("<EOF>");
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public  Language getLanguage() {
        return language;
    }

    public Interval getSourceInterval() {
        return sourceInterval;
    }

    public int start(){
        return sourceInterval.a;
    }

    public int end(){
        return sourceInterval.b;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object n){
        return n instanceof Node && ((Node) n).text.equals(this.text);
    }

}
