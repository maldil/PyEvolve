package com.inferrules.core;

import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class RewriteRule {

    private final Template Before;
    private final Template After;

    public RewriteRule(Template before, Template after) {
        this.Before = before;
        this.After = after;
    }


//    public Map<String, ValueDifference<TemplateVariable>> intersection(){
//        return Maps.difference(Before.getCodeToTemplateVars(), After.getCodeToTemplateVars())
//                .entriesDiffering();
//    }

}
