package com.inferrules.comby.operations;

import com.google.gson.Gson;
import com.inferrules.comby.jsonResponse.CombyMatch;
import com.inferrules.comby.jsonResponse.CombyRewrite;
import com.inferrules.comby.jsonResponse.Match;
import com.inferrules.core.Node;
import com.inferrules.core.languageAdapters.Language;
import com.inferrules.utils.Utilities;
import io.vavr.control.Try;

import java.text.MessageFormat;

import static java.util.stream.Collectors.joining;

public class BasicCombyOperations {

    private static final String matchCommand = "echo \"{0}\" | comby \"{1}\" -stdin -json-lines -match-newline-at-toplevel -match-only  -matcher {2} \"foo\"";
    private static final String rewriteCommand = "echo \"{0}\" | comby \"{1}\" \"{2}\" -stdin -json-lines \"{3}\"";
    private static final String substituteCommand = "comby '' '{0}' -substitute '[{1}]'";
    private static final String defaultLanguage = ".java";

//    public static Try<Match> getPerfectMatch(String template, Node source, String language) {
//        return getMatch(template, source, language, true).map(x -> x.getMatches().get(0));
//    }


    public static Try<CombyMatch> getMatch(String template, Node source, Language language, boolean isPerfect) {
        Object[] arguments = { source.getValue(), template, language.getExtension()};
        return Utilities.runBashCommand(MessageFormat.format(matchCommand, arguments))
                .map(x -> new Gson().fromJson(x, CombyMatch.class))
                .onFailure(x -> System.out.println(x.toString()))
                .filter(x -> !isPerfect || x.isPerfect(source.getValue()));
    }

    public static Try<CombyRewrite> rewrite(String matcher, String rewrite, String source, String language) {
        return Utilities.runBashCommand(MessageFormat.format(rewriteCommand, source, matcher, rewrite,
                language == null ? defaultLanguage : language))
                .map(x -> new Gson().fromJson(x, CombyRewrite.class));
    }

    public static Try<String> substitute(String template, String... substitutions) {
        String subs = Utilities.split(2, substitutions).stream()
                .map(x -> MessageFormat.format("{\"variable\":\"{0}\",\"value\":\"{1}\"}", x[0], x[1]))
                .collect(joining(","));
        return Utilities.runBashCommand(MessageFormat.format(substituteCommand, template, subs));
    }
}
