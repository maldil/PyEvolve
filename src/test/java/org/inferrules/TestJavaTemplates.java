package org.inferrules;

import com.google.gson.Gson;
import com.inferrules.core.Template;
import com.inferrules.core.languageAdapters.JavaAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class TestJavaTemplates {


    @Test
    void testTemplates() throws IOException, URISyntaxException {
        Map<String, String> scenarios = Map.of("Utils.transform(x);", "java/snippet1.json",
                "x.map(Utils::transform);", "java/snippet2.json");
        for(var scenario : scenarios.entrySet()){
            Template.TemplateNode t = new Template(scenario.getKey(), new JavaAdapter(), true).getTemplateNode();
            Template.TemplateNode expectedTemplateNode = readTemplateNodeFromResource(scenario.getValue());
            Assertions.assertEquals(t.toJson(),expectedTemplateNode.toJson());
        }
    }

    private Template.TemplateNode readTemplateNodeFromResource(String fileName) throws URISyntaxException, IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return new Gson().fromJson(Files.readString(Paths.get(resource.toURI())), Template.TemplateNode.class);

        }
    }

}
