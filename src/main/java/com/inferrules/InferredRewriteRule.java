package com.inferrules;

import com.inferrules.core.RewriteRule;
import com.inferrules.core.TemplateVariable;
import io.vavr.Tuple2;

import java.util.List;
import java.util.stream.Collectors;

public class InferredRewriteRule {
    public String Match;
    public String Replace;
    public List<Tuple2<TemplateVariable, String>> matches;


    public InferredRewriteRule(RewriteRule r) {
        this.Match = r.getMatch().getTemplate();
        this.Replace = r.getReplace().getTemplate();
        this.matches = r.getMatch().getTemplateVarsMapping().stream().map(vm -> vm.map2(tn -> tn.getCodeSnippet()))
                .collect(Collectors.toList());

    }

}
