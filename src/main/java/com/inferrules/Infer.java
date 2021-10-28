package com.inferrules;

import com.inferrules.core.RewriteRule;
import com.inferrules.core.languageAdapters.LanguageSpecificInfo;
import org.apache.commons.cli.*;

import java.util.Map;

public class Infer {

    public static void main(String[] args) {
        var m = parseCommandLineArgs(args);
        InputFormat input = new InputFormat(m.get("Before"), m.get("After"), m.get("Language"));

        RewriteRule rw = new RewriteRule(input.getBefore(),
                input.getAfter(),
                LanguageSpecificInfo.Language.valueOf(input.getLanguage()));
        System.out.println(rw.getMatch().getTemplate());
        System.out.println("---------------");
        System.out.println(rw.getReplace().getTemplate());
    }


    public static Map<String, String> parseCommandLineArgs(String[] args){
        Options options = new Options();

        Option input = new Option("b", "before", true, "Before Code Snippet");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option("a", "after", true, "After Code Snippet");
        output.setRequired(true);
        options.addOption(output);

        Option lang = new Option("l", "language", true, "Language");
        lang.setRequired(true);
        options.addOption(lang);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;//not a good practice, it serves it purpose
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }
        return Map.of("Before", cmd.getOptionValue("before").replace("\\n","\n"), "After",cmd.getOptionValue("after").replace("\\n","\n"),
                "Language",cmd.getOptionValue("language"));
    }
}
