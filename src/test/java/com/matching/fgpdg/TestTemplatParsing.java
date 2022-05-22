package com.matching.fgpdg;

import com.matching.ConcreatePythonParser;
import com.matching.fgpdg.nodes.ast.AlphanumericHole;
import com.matching.fgpdg.nodes.ast.LazyHole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.python.antlr.Visitor;
import org.python.antlr.ast.AlphHole;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.Hole;
import org.python.antlr.ast.Module;

public class TestTemplatParsing {

    @Test
    void testTemplate1() throws Exception {
        String code = "for :[[l2]] in :[[l6]]:\n" +
                "    boo()\n" +
                "    :[l11]\n" +
                "    if (:[[l7]]):\n" +
                "        xx.ccc.fff()\n" +
                "        :[[l8]].:[[l7]].:[[l6]](2*:[[l9]])";
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(8,count.alpHole+count.lazyHole);
    }

    @Test
    void testTemplate2() throws Exception {
        String code = "[:[[l5]] for  :[[l11]] in :[[l12]] if :[[l13]] ]\n" +
                "\n" +
                "while (:[[l17]]):\n" +
                "    :[[l19]]";
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(6,count.alpHole+count.lazyHole);
    }

    @Test
    void testTemplate3() throws Exception {
        String code = "with :[[l19]] as :[[l20]]:\n" +
                "    :[[l21]]\n" +
                "\n" +
                "try:\n" +
                "    :[[l23]]\n" +
                "except :[[l23]] as xx:\n" +
                "    print(\"division by zero!\")\n" +
                "except ZeroDivisionError as yy:\n" +
                "    print(\"division by zero!\")\n" +
                "else:\n" +
                "    print(\"result is\")\n" +
                "finally:\n" +
                "    print(\"executing finally clause\")";
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(5,count.alpHole+count.lazyHole);
    }

    @Test
    void testTemplate4() throws Exception {
        String code = "[:[[l51]],:[[l52]],:[[l53]],4,5,:[[l14]]]\n" +
                "{'one':3,:[[l56]]::[[l57]],:[[l58]]::[[l59]]}\n" +
                "\n" +
                "{ 3,:[[l56]],:[[l57]],:[[l58]],:[[l59]]}";
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(12,count.alpHole+count.lazyHole);
    }

    @Test
    void testTemplate5() throws Exception {
        String code = "{:[[l1]]::[[l2]] for :[[l3]] in iterable if :[[l5]]}";
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(4,count.alpHole+count.lazyHole);
    }

    @Test
    void testTemplate6() throws Exception {
        String code = "{:[[l1]] for :[[l3]] in iterable if :[[l5]]}";
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(3,count.alpHole+count.lazyHole);
    }

    @Test
    void testTemplate7() throws Exception {
        String code = "(:[[l1]] for :[[l3]] in iterable if :[[l5]])";
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(3,count.alpHole+count.lazyHole);
    }

    @Test
    void testTemplate8() throws Exception {
        String code = ":[[l1]] = 3\n" +
                "choice = input(:[[l2]])\n" +
                "word = input(\"Please enter text\")\n" +
                "letters = :[[l3]] + string.punctuation + string.digits\n" +
                "encoded = ''\n" +
                "if :[[l4]] == \"encode\":\n" +
                "    for :[[l5]] in word:\n" +
                "        if letter == ' ':\n" +
                "            encoded = encoded + ' '\n" +
                "        else:\n" +
                "            x = letters.:[[l6]](letter) + shift\n" +
                "            encoded = encoded + :[[l7]][x]";
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(7,count.alpHole+count.lazyHole);
    }

    @Test
    void testTemplate9() throws Exception {
        String code = "if :[[l1]] == \"decode\":\n" +
                "    for letter in :[[l2]]:\n" +
                "        if letter == ' ':\n" +
                "            encoded = :[[l3]] + ' '\n" +
                "        else:\n" +
                "            x = letters.index(:[[l4]]) - shift\n" +
                "            encoded = :[[l5]] + letters[x]";
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(5,count.alpHole+count.lazyHole);
    }

    @Test
    void testTemplate10() throws Exception {
        String code = "num = int(input(\"Enter a number: \"))\n" +
                "if (:[[l1]] % :[[l2]]) == 0:\n" +
                "   print(\"{0} is Even\".format(num))\n" +
                "else:\n" +
                "   print(\"{0} is Odd\".format(num))";
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(2,count.alpHole+count.lazyHole);
    }

    @Test
    void testTemplate11() throws Exception {
        String code = "for i in range(1, :[[l3]]):\n" +
                "   print(num, 'x', i, '=', :[[l1]]*:[[l2]])";
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(3,count.alpHole+count.lazyHole);
    }

    @Test
    void testTemplate12() throws Exception {
        String code = "def generate_benchmark_params_cpu_gpu(*params_list):\n" +
                "  \"\"\"Extend the benchmark names with CPU and GPU suffix.\n" +
                "  Args:\n" +
                "    *params_list: A list of tuples represents the benchmark parameters.\n" +
                "  Returns:\n" +
                "    A list of strings with the benchmark name extended with CPU and GPU suffix.\n" +
                "  \"\"\"\n" +
                "  benchmark_params = []\n" +
                "  for params in params_list:\n" +
                "    benchmark_params.extend([\n" +
                "        ((:[[l1]][0] + '_CPU',) + param[1:]) for param in params\n" +
                "    ])\n" +
                "    benchmark_params.extend([\n" +
                "        ((param[0] + :[[l2]],) + param[1:]) for param in params\n" +
                "    ])\n" +
                "  return benchmark_params";
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(2,count.alpHole+count.lazyHole);
    }

    @Test
    void testTemplate13() throws Exception {
        String code = "if :[[l1]] is :[[l2]]:\n" +
                "    raise ValueError('Input data is required.')\n" +
                "if 'optimizer' is None:\n" +
                "    raise ValueError('Optimizer is required.')\n" +
                "if 'loss' is None:\n" +
                "    raise ValueError('Loss function is required.')\n" +
                "if :[[l3]] < :[[l4]]:\n" +
                "    raise ValueError('`num_gpus` cannot be negative')\n";
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(4,count.alpHole+count.lazyHole);
    }

    @Test
    void testTemplate14() throws Exception {
        String code = "app = tf.keras.applications.Xception\n" +
                ":[[l1]], :[[l2]] = (\n" +
                "        saved_model_benchmark_util.save_and_load_benchmark(app))\n" +
                "\n" +
                "self.report_benchmark(\n" +
                "        iters=save_result['iters'],\n" +
                "        wall_time=save_result['wall_time'],\n" +
                "        name=save_result['name'])\n" +
                "\n" +
                "self.report_benchmark(\n" +
                "        iters=load_result['iters'],\n" +
                "        wall_time=:[[l3]],\n" +
                "        name=load_result['name'])";
        ConcreatePythonParser parser = new ConcreatePythonParser();
        Module module = parser.parseTemplates(code);
        HoleCounter count = new HoleCounter();
        count.visit(module);
        Assertions.assertEquals(3,count.alpHole+count.lazyHole);
    }
    private int getCharacterCount(String code,char charc){
        int count=0;
        for (int i = 0; i < code.length(); i++) {
            if (code.charAt(i) == charc) {
                count++;
            }
        }
        return count;
    }

    class HoleCounter extends Visitor {
        int alpHole=0;
        int lazyHole=0;
        public Object visitHole(Hole node) throws Exception {
            if (node instanceof AlphanumericHole){
                alpHole=alpHole+1;
            }
            else if (node instanceof LazyHole)
            {
                lazyHole=lazyHole+1;
            }
            return super.visitHole(node);
        }

        public Object visitAlphHole(AlphHole node) throws Exception {

            return super.visitAlphHole(node);
        }

        @Override
        public Object visitAttribute(Attribute node) throws Exception {
            return super.visitAttribute(node);
        }

    }
}
