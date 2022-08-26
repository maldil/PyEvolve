package com.inferrules.comby.operations;

import com.inferrules.comby.jsonResponse.CombyRewrite;
import io.vavr.control.Try;
import org.inferrules.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.python.antlr.ast.Module;

import static org.junit.jupiter.api.Assertions.*;

class BasicCombyOperationsTest {

    @Test
    void rewrite() {
        BasicCombyOperations op = new BasicCombyOperations();
        Module code = Utils.getPythonModule("author/project/test22.py");
        String strCode = code.getInternalBody().get(1).toString();
        String matcher = "def function1(sentence, intArray):\n" +
                "    :[l1]\n" +
                "    :[[l6]] = :[[l8]]\n" +
                "    :[l3]\n" +
                "    for :[[l4]] in :[[l5]]:\n" +
                "        :[[l6]] = :[[l6]] + :[[l4]]\n" +
                "return :[[l7]]";
        String rewrite = "def function1(sentence, intArray):\n" +
                "    :[l1]\n" +
                "    :[[l6]] = np.sum(:[[l5]])\n" +
                "    :[l3]\n" +
                "return :[[l7]]";

        Try<CombyRewrite> changedCode = op.rewrite(matcher, rewrite, strCode, ".python");
        Assertions.assertEquals("def function1(sentence, intArray):\n" +
                "    hhh = 0\n" +
                "    number = np.sum(intArray)\n" +
                "    print(ff)\n" +
                "return hhh\n",changedCode.get().getRewrittenSource());
    }
}