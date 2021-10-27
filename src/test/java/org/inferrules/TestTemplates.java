package org.inferrules;

import com.google.gson.Gson;
import com.inferrules.core.Template;
import com.inferrules.core.TemplateNode;
import com.inferrules.core.VariableNameGenerator;
import com.inferrules.core.languageAdapters.JavaAdapter;
import com.inferrules.core.languageAdapters.PythonAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class TestTemplates {


    @Test
    void testJavaTemplates() throws IOException, URISyntaxException {
        Map<String, String> scenarios = Map.of(
                "Utils.transform(x);", "java/snippet1.json",
                "x.map(Utils::transform);", "java/snippet2.json");

        JavaAdapter languageAdapter = new JavaAdapter();
        for(var scenario : scenarios.entrySet()){
            Template t = new Template(scenario.getKey(), languageAdapter, new VariableNameGenerator('l'));
            Template expectedTemplateNode = readTemplateFromResource(scenario.getValue());
            Assertions.assertEquals(t.toJSON(), expectedTemplateNode.toJSON());
        }
    }

    @Test
    void testPythonTemplates() throws URISyntaxException, IOException {
        Map<String, String> scenarios = Map.of(
                """
                        count = 0
                        for e in es:
                                z += y
                                count += e
                        print(count)
                        """, "python/snippet1.json",
                "count = sum([1 for y in es])\n", "python/snippet3.json",
                """
                        count = 0
                        for e in es:
                                y = sq(count)
                                if not y:
                                        count += e
                        print(count)""","python/snippet2.json"
                , """
                        mse = 0.0
                        for i in range(border_size, image1.shape[0] - border_size):
                         for j in range(border_size, image1.shape[1] - border_size):
                           for k in range(image1.shape[2]):
                        \terror = image1[i, j, k] - image2[i, j, k]
                        \tmse += error * error
                        \treturn mse / ((image1.shape[0] - 2 * border_size) * (image1.shape[1] - 2 * border_size) * image1.shape[2])""","python/snippet4.json"
        );
        PythonAdapter languageAdapter = new PythonAdapter();

        for(var scenario : scenarios.entrySet()){
            Template t = new Template(scenario.getKey(), languageAdapter, new VariableNameGenerator('l'));
            Template expectedTemplateNode = readTemplateFromResource(scenario.getValue());
            System.out.println(t.toJSON());
            System.out.println(expectedTemplateNode.toJSON());
            Assertions.assertEquals(t.toJSON(), expectedTemplateNode.toJSON());

        }
    }

    public Template readTemplateFromResource(String fileName) throws URISyntaxException, IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return new Gson().fromJson(Files.readString(Paths.get(resource.toURI())), Template.class);

        }
    }


}
