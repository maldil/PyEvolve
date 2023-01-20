package com.utils;

import io.vavr.control.Try;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FileIO {
    public static void writeStringToFile(String string, String outputFile) {
        File f = new File(outputFile);
        if (!f.getParentFile().exists()){
            f.getParentFile().mkdirs();
            System.err.println("Path does not exist, creating the path " + f.getParentFile().getPath());
        }
        if (f.exists()){
            System.err.println("File exists, overriding the file " + f.getParentFile().getPath());
        }
        Try.of(()-> Files.writeString(Paths.get(outputFile),string)).onFailure(System.err::println).
                onSuccess(x->System.out.println("File successfully wrote to " +outputFile));

    }

    private static void writeString(String string, String outputFile) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        writer.write(string);
        writer.flush();
        writer.close();
    }

    public static String readFile(String path){
        return Try.of(() -> Files.readString(Paths.get(path))).onFailure(System.err::println).get();
    }

    public static List<File> readAllFiles(String extension, String path){
        File folder = new File(path);
        if (folder.exists()){
            return Arrays.stream(Objects.requireNonNull(folder.listFiles())).filter(x->x.getName().endsWith(".py")).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private String getPathToResources(String name){
        File f = new File(name);
        if (f.exists()) {
            return f.getAbsolutePath();
        }
        return getClass().getClassLoader().getResource(name).getPath();

    }

    public static String readStringFromFile(String inputFile) {
        try {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(inputFile));
            byte[] bytes = new byte[(int) new File(inputFile).length()];
            in.read(bytes);
            in.close();
            return new String(bytes);
        }
        catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }
}
