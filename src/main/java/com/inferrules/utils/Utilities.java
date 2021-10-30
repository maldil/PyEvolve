package com.inferrules.utils;

import com.inferrules.core.TemplateNode;
import com.inferrules.core.TemplateVariable;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import org.antlr.v4.runtime.misc.Interval;
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.inferrules.core.Template.TreeTraverser;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

public class Utilities {

    public static Try<String> runBashCommand(String command){
        String[] cmd = {"/bin/sh", "-c", command };
        return Try.of(() -> Runtime.getRuntime().exec(cmd))
                .map(p -> new BufferedReader(new InputStreamReader(p.getInputStream()))
                        .lines().collect(joining("\n")));
    }


    public static <T> List<T[]> split(int chunkSize, T... inputArray){
        return IntStream.iterate(0, i -> i + chunkSize)
                .limit((int) Math.ceil((double) inputArray.length / chunkSize))
                .mapToObj(j -> Arrays.<T>copyOfRange(inputArray, j, Math.min(inputArray.length, j + chunkSize)))
                .collect(Collectors.toList());
    }


    public static String joinTokens(List<String> tokens, Interval sourceInterval) {
        return IntStream.range(sourceInterval.a, sourceInterval.b + 1)
                .mapToObj(tokens::get).collect(joining());
    }

    public static <A,B,X,Y> Map<X,Y> applyFnOnMap(Map<A,B> map, Function<Map.Entry<A,B>, Map.Entry<X,Y>> func){
        return map.entrySet().stream().map(func)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static <X,Y> AbstractMap.SimpleImmutableEntry<X,Y> asEntry(X x, Y y){
        return new AbstractMap.SimpleImmutableEntry<>(x,y);
    }

    public static <X,Y> Map<X,Y> mergeMap(Map<X,Y>... maps){
        return Arrays.stream(maps).flatMap(x->x.entrySet().stream())
                .collect(toMap(x->x.getKey(),x->x.getValue()));
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


    public static <T> Stream<T> stream(Iterable<T> it){
        return StreamSupport.stream(it.spliterator(), false);
    }


    /**
     * Assumes that the node always exists!
     * @param beforeNode
     * @param x
     * @return
     */
    public static Tuple2<TemplateVariable, TemplateNode> findInTree(Tuple2<TemplateVariable, TemplateNode> beforeNode, TemplateVariable x) {
        return traverseTree(beforeNode).filter(z -> z._1().equals(x)).findFirst().orElse(null);
    }

    public static Stream<Tuple2<TemplateVariable, TemplateNode>> traverseTree(Tuple2<TemplateVariable, TemplateNode> beforeNode) {
        return stream(TreeTraverser.breadthFirst(beforeNode));
    }
}
