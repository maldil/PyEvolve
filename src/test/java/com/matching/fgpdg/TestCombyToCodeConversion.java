package com.matching.fgpdg;

import com.matching.ConcreatePythonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.python.antlr.ast.Module;

public class TestCombyToCodeConversion {
    @Test
    void testTemplate1() throws Exception {
        String code = "for i in range(1, :[[l1]]):\n" +
                "   print(num, 'x', i, '=', :[[l2]]*:[[l3]])";
        ConcreatePythonParser parser = new ConcreatePythonParser();
        String s = parser.convertComByTemplateToParsableCode(code);
        Assertions.assertEquals("for i in range(1, [$1]):\n   print(num, 'x', i, '=', [$2]*[$3])",s);
        System.out.println(s);
    }

    @Test
    void testTemplate2() throws Exception {
        String code = "\"for :[[l2]] in :[[l6]]:\\n\" +\n" +
                "                \"    boo()\\n\" +\n" +
                "                \"    :[l11]\\n\" +\n" +
                "                \"    if (:[[l7]]):\\n\" +\n" +
                "                \"        xx.ccc.fff()\\n\" +\n" +
                "                \"        :[[l8]].:[[l7]].:[[l6]](2*:[[l9]])";
        ConcreatePythonParser parser = new ConcreatePythonParser();
        String s = parser.convertComByTemplateToParsableCode(code);
        Assertions.assertEquals("\"for [$2] in [$6]:\\n\" +\n" +
                "                \"    boo()\\n\" +\n" +
                "                \"    [%11]\\n\" +\n" +
                "                \"    if ([$7]):\\n\" +\n" +
                "                \"        xx.ccc.fff()\\n\" +\n" +
                "                \"        [$8].[$7].[$6](2*[$9])",s);
        System.out.println(s);
    }

    @Test
    void testTemplate3() throws Exception {
        String code = "[:[[l51]],:[[l52]],:[[l53]],4,5,:[l14]]\n" +
                "{'one':3,:[[l56]]::[[l57]],:[[l58]]::[[l59]]}\n" +
                "\n" +
                "{ 3,:[[l56]],:[[l57]],:[[l58]],:[[l59]]}";
        ConcreatePythonParser parser = new ConcreatePythonParser();
        String s = parser.convertComByTemplateToParsableCode(code);
        Assertions.assertEquals("[[$51],[$52],[$53],4,5,[%14]]\n" +
                "{'one':3,[$56]:[$57],[$58]:[$59]}\n" +
                "\n" +
                "{ 3,[$56],[$57],[$58],[$59]}",s);
        System.out.println(s);
    }

}
