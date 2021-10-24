package com.inferrules.core.languageAdapters;

import java.util.ArrayList;
import java.util.List;


public class LanguageConfig {

    private List<String> keywords;
    private List<String> symbols;

    public LanguageConfig(){
        keywords=new ArrayList<>();
        symbols=new ArrayList<>();
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getSymbols() {
        return symbols;
    }

    public void setSymbols(List<String> symbols) {
        this.symbols = symbols;
    }
}
