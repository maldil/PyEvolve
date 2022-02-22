package com.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class FileIO {
    public static void writeStringToFile(String string, String outputFile) {
        try {
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
}
