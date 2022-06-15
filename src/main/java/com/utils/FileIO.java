package com.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileIO {
    public static void writeStringToFile(String string, String outputFile) {
        try {
            File f = new File(outputFile);
            if (!f.getParentFile().exists()){
                f.getParentFile().mkdirs();
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write(string);
            writer.flush();
            writer.close();
        } catch (Exception e) {
			/*e.printStackTrace();
			System.exit(0);*/
            System.err.println(e.getMessage());
        }
    }

    public static String readFile(String path){
        try {
            return Files.readString(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
