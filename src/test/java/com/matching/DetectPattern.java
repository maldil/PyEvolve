package com.matching;
import com.utils.Utils;
import org.junit.jupiter.api.Test;

import static com.matching.fgpdg.Configurations.PROJECT_REPOSITORY;


public class DetectPattern {

    @Test
    void testPattern() throws Exception {
        String pattern = "# import numpy as np\n" +
                "# type :[[l2]] : Any\n" +
                "# type :[[l1]] : Any\n" +
                "# type :[l3] : Any\n" +
                "# type :[l4] : Any\n" +
                "for :[[l2]] in :[l3]:\n" +
                "    for :[[l1]] in :[l4]:\n" +
                "        break\n";
        String outPath = "./OUTPUT/"; //https://github.com/maldil/MLEditsTest.git
        String projectPath =  PROJECT_REPOSITORY +"pythonInfer/PatternTest";
        System.out.println(pattern);
        Utils.searchProjectForPatterns(projectPath,pattern,outPath);
    }
}
