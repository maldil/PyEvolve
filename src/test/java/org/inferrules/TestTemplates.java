package org.inferrules;

import com.inferrules.core.Template;
import com.inferrules.core.TemplateNode;
import com.inferrules.core.VariableNameGenerator;
import com.inferrules.core.languageAdapters.JavaAdapter;
import com.inferrules.core.languageAdapters.PythonAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import static org.inferrules.Utils.readTemplateFromResource;

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
                "count = 0\n" +
                "for e in es:\n" +
                "        z += y\n" +
                "        count += e\n" +
                "print(count)\n" , "python/snippet1.json",
                "count = sum([1 for y in es])\n", "python/snippet3.json",
                "count = 0\n" +
                        "for e in es:\n" +
                        "        y = sq(count)\n" +
                        "        if not y:\n" +
                        "                count += e\n" +
                        "print(count)","python/snippet2.json"
                ,"mse = 0.0\n" +
                    "for i in range(border_size, image1.shape[0] - border_size):\n" +
                    " for j in range(border_size, image1.shape[1] - border_size):\n" +
                    "   for k in range(image1.shape[2]):\n" +
                    "\terror = image1[i, j, k] - image2[i, j, k]\n" +
                    "\tmse += error * error\n" +
                    "\treturn mse / ((image1.shape[0] - 2 * border_size) * (image1.shape[1] - 2 * border_size) * image1.shape[2])","python/snippet4.json"
        );
        PythonAdapter languageAdapter = new PythonAdapter();

        for(var scenario : scenarios.entrySet()){
            Template t = new Template(scenario.getKey(), languageAdapter, new VariableNameGenerator('l'));
            Template expectedTemplateNode = readTemplateFromResource(scenario.getValue());
            Assertions.assertEquals(t.toJSON(), expectedTemplateNode.toJSON());

        }
    }


}
