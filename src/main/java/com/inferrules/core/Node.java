package com.inferrules.core;

import com.inferrules.core.languageAdapters.LanguageSpecificInfo;
import org.antlr.v4.runtime.misc.Interval;

import java.util.ArrayList;
import java.util.List;

public class Node {

    private final String value; // space and new line are included
    private final LanguageSpecificInfo.Language language;
    private final boolean isKeyword;
    private final boolean isSymbol;
    private final List<Node> children;
    private final Interval sourceInterval;
    private final String text; // spaces and new lines are NOT included

    public Node(String value, LanguageSpecificInfo.Language language, Interval sourceInterval, String text) {
        this.text = text;
        this.value = value;
        this.language = language;
        this.sourceInterval = sourceInterval;
        this.isKeyword = LanguageSpecificInfo.getKeywords(language).contains(value);
        this.isSymbol = !isKeyword && LanguageSpecificInfo.getSymbols(language).contains(value);
        this.children = new ArrayList<>();
    }

    public Node(String value, List<Node> children, LanguageSpecificInfo.Language language, Interval sourceInterval, String text) {
        this.text = text;
        this.value = value;
        this.language = language;
        this.sourceInterval = sourceInterval;
        this.isKeyword = LanguageSpecificInfo.getKeywords(language).contains(value);
        this.isSymbol = !isKeyword && LanguageSpecificInfo.getSymbols(language).contains(value);
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

    public LanguageSpecificInfo.Language getLanguage() {
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

}
