package com.inferrules.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

import io.vavr.control.Try;
import org.antlr.v4.runtime.misc.Interval;

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


}
