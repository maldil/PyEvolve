package com.inferrules;

import com.inferrules.core.RewriteRule;
import com.inferrules.utils.Utilities;

import static com.inferrules.core.languageAdapters.LanguageSpecificInfo.*;

public class Infer {

    public static void main(String[] args) {
        var input = Utilities.parseCommandLineArgs(args);
        RewriteRule rw = new RewriteRule(input.get("Before"), input.get("After"), Language.valueOf(input.get("Language")));
        System.out.println(rw.getMatch().getTemplate());
        System.out.println("---------------");
        System.out.println(rw.getReplace().getTemplate());
    }
}
