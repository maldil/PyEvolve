package org.inferrules;

import com.google.common.collect.Streams;
import com.google.gson.Gson;
import com.inferrules.comby.jsonResponse.CombyMatch;
import com.inferrules.utils.Utilities;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Try;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

public class Utils {
    private static final String matchCommand = "echo \"{0}\" | comby \"{1}\" -stdin -json-lines -match-newline-at-toplevel -match-only  -matcher {2} \"foo\"";
    private static final String defaultLanguage = ".java";

    public static CombyMatch getMatch(String template, String value, String language, boolean isPerfect) {
        Object[] arguments = {value, template, language == null ? defaultLanguage : language};
        return Utilities.runBashCommand(MessageFormat.format(matchCommand, arguments))
                .map(x -> new Gson().fromJson(x, CombyMatch.class))
                .onFailure(x -> System.out.println(x.toString()))
                .filter(x -> !isPerfect || x.isPerfect(value)).getOrNull();

    }

    public static List<String> tokenizeTemplate(String tempalte){
        CombyMatch m = getMatch(":[a.]", tempalte,".java", false);
        return m.getMatches().stream().map(x->x.getMatched()).collect(Collectors.toList());

    }

    public static boolean areAlphaEquivalent(String template1, String template2){
        List<String> tokenizedTemplate1 = tokenizeTemplate(template1);
        List<String> tokenizedTemplate2 = tokenizeTemplate(template2);
        if(tokenizedTemplate1.size()!=tokenizedTemplate2.size())
            return false;
        return Streams.zip(tokenizedTemplate1.stream(), tokenizedTemplate2.stream(), Tuple::of)
                .collect(groupingBy(x->x._1(), collectingAndThen(toList(), xs -> xs.stream().map(x -> x._2()).distinct().count())))
                .entrySet().stream().allMatch(x->x.getValue() == 1);
    }

}
