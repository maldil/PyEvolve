package org.inferrules;

import com.google.gson.Gson;
import com.inferrules.core.Template;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Utils {

    public static Template.TemplateNode readTemplateNodeFromResource(String fileName) throws URISyntaxException, IOException {
        ClassLoader classLoader = Utils.class.getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return new Gson().fromJson(Files.readString(Paths.get(resource.toURI())), Template.TemplateNode.class);

        }
    }

}
