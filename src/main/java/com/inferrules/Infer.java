package com.inferrules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.inferrules.core.RewriteRule;
import com.inferrules.core.languageAdapters.Language;
import com.inferrules.utils.Utilities;

public class Infer {

    public static void main(String[] args) {
        var input = Utilities.parseCommandLineArgs(args);
        RewriteRule rw = new RewriteRule(input.get("Before"), input.get("After"), Language.valueOf(input.get("Language")));
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String json = gson.toJson(new InferredRewriteRule(rw), InferredRewriteRule.class);
        System.out.println(json);
    }


}
