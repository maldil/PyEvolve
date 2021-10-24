package org.inferrules;

import com.google.gson.Gson;
import com.inferrules.core.Template;
import com.inferrules.core.languageAdapters.JavaAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestJavaTemplates {

    @Test
    void testTemplates() throws IOException, URISyntaxException {
        String snippet1 = "Utils.transform(x);";
//        String snippet2 = "x.map(Utils::transform);";

        Template t = new Template(snippet1, new JavaAdapter(), true);

        Template.TemplateNode expectedTemplateNode = new Gson().fromJson(Files.readString(getFileFromResource("snippet1.json").toPath()), Template.TemplateNode.class);
        Assertions.assertEquals(new Gson().toJson(t.getTemplateNode()),new Gson().toJson(expectedTemplateNode));
        System.out.println();

    }

    private File getFileFromResource(String fileName) throws URISyntaxException {

        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {

            // failed if files have whitespaces or special characters
            //return new File(resource.getFile());

            return new File(resource.toURI());
        }

    }

}
