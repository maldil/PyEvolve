package com.inferrules.core;

import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class RewriteRule {

    private final String Match;
    private final String Replace;

    public RewriteRule(Template before, Template after) {
        this.Replace = null;
        this.Match = null;
    }


    public Map<String, ValueDifference<TemplateVariable>> intersection(Template t1, Template t2){
        return Maps.difference(t1.getCodeToTemplateVars(), t2.getCodeToTemplateVars())
                .entriesDiffering();
    }

}
