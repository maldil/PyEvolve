package org.inferrules;
import com.inferrules.core.RewriteRule;
import com.inferrules.core.languageAdapters.Language;
import org.junit.jupiter.api.Test;

import static org.inferrules.Utils.areAlphaEquivalent;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class TestRewriteRules {


    @Test
    void testJavaRewriteRule1() {
        String before = "Utils.transform(x);";
        String after = "x.map(Utils::transform);";
        String expectedMatch = ":[[l1]].:[[l2]](:[[l3]]);";
        String expectedReplace = ":[[l3]].map(:[[l1]]:::[[l2]]);";
        RewriteRule rw = new RewriteRule(before, after,  Language.Java);
        assertTrue(areAlphaEquivalent(expectedMatch,rw.getMatch().getTemplate()));
        assertTrue(areAlphaEquivalent(expectedReplace,rw.getReplace().getTemplate()));
    }



    @Test
    void testJavaRewriteRule2() {
        String before = "fn.apply(x.y());";
        String after = "fn.applyAsInt(x.y());";
        String expectedMatch = ":[[l1]].apply(:[l3]);";
        String expectedReplace = ":[[l1]].applyAsInt(:[l3]);";
        RewriteRule rw = new RewriteRule(before, after,  Language.Java);
        assertTrue(areAlphaEquivalent(expectedMatch,rw.getMatch().getTemplate()));
        assertTrue(areAlphaEquivalent(expectedReplace,rw.getReplace().getTemplate()));
    }


    @Test
    void testJavaRewriteRule3() {
        String before = "List<String> collect = new ArrayList<>();\nfor(String e : elements){\n collect.add(e.substring(5));\n}";
        String after = "List<String> collect = elements.stream().map(e -> e.substring(5)).collect(toList());";
        String expectedMatch = """
                :[[l4]]<:[[l6]]> :[[l8]] = new ArrayList<>();
                for(:[[l6]] :[[l13]] : :[[l14]]){
                 :[[l8]].add(:[[l13]].:[[l20]](:[[l21]]));
                }""";
        String expectedReplace = ":[[l4]]<:[[l6]]> :[[l8]] = :[[l14]].stream().map(:[[l13]] -> :[[l13]].:[[l20]](:[[l21]])).:[[l8]](toList());";
        RewriteRule rw = new RewriteRule(before, after,  Language.Java);
        assertTrue(areAlphaEquivalent(expectedMatch,rw.getMatch().getTemplate()));
        assertTrue(areAlphaEquivalent(expectedReplace,rw.getReplace().getTemplate()));
    }

    @Test
    void testJavaRewriteRule4() {
        String before = """
                Map<String, Long> m = new HashMap<>();
                for(String e : elements){
                 if(!m.containsKey(e)){
                    m.put(e, 1);
                 }
                 m.put(e, m.get(e)+1);
                }""";
        String after = "Map<String, Long> collect = elements.stream().collect(groupingby(e->e, counting()));";
        String expectedMatch = """
                :[[l4]]<:[[l6]], :[[b]]> :[[l10]] = new HashMap<>();
                for(:[[l6]] :[[l15]] : :[[l16]]){
                 if(!:[[l10]].containsKey(:[[l15]])){
                    :[[l10]].:[[l26]](:[[l15]], :[[l28]]);
                 }
                 :[[l10]].:[[l26]](:[[l15]], :[[l10]].get(:[[l15]])+:[[l28]]);
                }""";
        String expectedReplace = ":[[l4]]<:[[l6]],:[[b]]> collect = :[[l16]].stream().collect(groupingby(:[[l15]]->:[[l15]], counting()));";
        RewriteRule rw = new RewriteRule(before, after,  Language.Java);
        assertTrue(areAlphaEquivalent(expectedMatch,rw.getMatch().getTemplate()));
        assertTrue(areAlphaEquivalent(expectedReplace,rw.getReplace().getTemplate()));
    }


    @Test
    void testJavaRewriteRule4_WithSingleLineComment() {
        String before = """
                Map<String, Long> m = new HashMap<>();
                // This is an example of a counter
                for(String e : elements){
                 if(!m.containsKey(e)){
                    m.put(e, 1);
                 }
                 m.put(e, m.get(e)+1);
                }""";
        String after = "Map<String, Long> collect = elements.stream().collect(groupingby(e->e, counting()));";
        String expectedMatch = """
                :[[l4]]<:[[l6]], :[[b]]> :[[l10]] = new HashMap<>();
                for(:[[l6]] :[[l15]] : :[[l16]]){
                 if(!:[[l10]].containsKey(:[[l15]])){
                    :[[l10]].:[[l26]](:[[l15]], :[[l28]]);
                 }
                 :[[l10]].:[[l26]](:[[l15]], :[[l10]].get(:[[l15]])+:[[l28]]);
                }""";
        String expectedReplace = ":[[l4]]<:[[l6]],:[[b]]> collect = :[[l16]].stream().collect(groupingby(:[[l15]]->:[[l15]], counting()));";
        RewriteRule rw = new RewriteRule(before, after,  Language.Java);
        assertTrue(areAlphaEquivalent(expectedMatch,rw.getMatch().getTemplate()));
        assertTrue(areAlphaEquivalent(expectedReplace,rw.getReplace().getTemplate()));
    }


    @Test
    void testJavaRewriteRule4_WithMultiLineComment() {
        String before = """
                Map<String, Long> m = new HashMap<>();
                /* This is an example of a counter
                This is a block
                comment
                */
                for(String e : elements){
                 if(!m.containsKey(e)){
                    m.put(e, 1);
                 }
                 m.put(e, m.get(e)+1);
                }""";
        String after = "Map<String, Long> collect = elements.stream().collect(groupingby(e->e, counting()));";
        String expectedMatch = """
                :[[l4]]<:[[l6]], :[[b]]> :[[l10]] = new HashMap<>();
                for(:[[l6]] :[[l15]] : :[[l16]]){
                 if(!:[[l10]].containsKey(:[[l15]])){
                    :[[l10]].:[[l26]](:[[l15]], :[[l28]]);
                 }
                 :[[l10]].:[[l26]](:[[l15]], :[[l10]].get(:[[l15]])+:[[l28]]);
                }""";
        String expectedReplace = ":[[l4]]<:[[l6]],:[[b]]> collect = :[[l16]].stream().collect(groupingby(:[[l15]]->:[[l15]], counting()));";
        RewriteRule rw = new RewriteRule(before, after,  Language.Java);
        assertTrue(areAlphaEquivalent(expectedMatch,rw.getMatch().getTemplate()));
        assertTrue(areAlphaEquivalent(expectedReplace,rw.getReplace().getTemplate()));

    }


//    @Test //https://twitter.com/NikosTsantalis/status/1453298762558889986/photo/1
//    public void javaForToStream_nikosTweet(){
//        String before  = """
//                for (ExceptionInfo exceptionInfo : throwsList) {
//                     if (!exceptionInfo.isFound()) {
//                         final Token token = exceptionInfo.getName();
//                         log(token.getLineNo(), token.getColumnNo(),
//                                 MSG_EXPECTED_TAG,
//                                 JavadocTagInfo.THROWS.getText(), token.getText());
//                     }
//                 }""";
//        String after = """
//                throwsList.stream().filter(exceptionInfo -> !exceptionInfo.isFound())
//                                 .forEach(exceptionInfo -> {
//                                     final Token token = exceptionInfo.getName();
//                                     log(token.getLineNo(), token.getColumnNo(),
//                                         MSG_EXPECTED_TAG,
//                                         JavadocTagInfo.THROWS.getText(), token.getText());
//                                 });""";
//        String expectedMatch = "";
//        String expectedReplace = ":[[l1]] = np.sum(:[[l6]])";
//        RewriteRule rw = new RewriteRule(before, after, LanguageSpecificInfo.Language.Java);
//        assertTrue(areAlphaEquivalent(expectedMatch,rw.getMatch().getTemplate()));
//        assertTrue(areAlphaEquivalent(expectedReplace,rw.getReplace().getTemplate()));
//
//    }

    @Test
    void testPythonRewriteRule1() {
        String before = """
                olderr = np.seterr(divide='ignore')
                try:
                  actual=logit(a)
                finally:
                  np.seterr(olderr)
                """;
        String after = "with np.errstate(divide='ignore'):\n" +
                "  actual = logit(a)";
        String expectedMatch = """
                :[[l1]] = 0
                for :[[l5]] in :[[l6]]:
                    :[[l1]] += :[[l5]]
                print(:[[l1]])""";
        String expectedReplace = ":[[l1]] = np.sum(:[[l6]])";
        RewriteRule rw = new RewriteRule(before, after,  Language.Python);
        assertTrue(areAlphaEquivalent(expectedMatch,rw.getMatch().getTemplate()));
        assertTrue(areAlphaEquivalent(expectedReplace,rw.getReplace().getTemplate()));
    }


    @Test
    void testPythonRewriteRule_Listing1() {
        String before = """
                result = 0
                for elem in elements:
                    result += elem""";
        String after = "result = np.sum(elements)";
        String expectedMatch = """
                for :[[l0]] in :[[l1]]:
                    :[[l3]] += :[[l0]]""";
        String expectedReplace = ":[[l3]] = np.sum(:[[l1]])";
        RewriteRule rw = new RewriteRule(before, after,  Language.Python);
        assertTrue(areAlphaEquivalent(expectedMatch,rw.getMatch().getTemplate()));
        assertTrue(areAlphaEquivalent(expectedReplace,rw.getReplace().getTemplate()));
    }

    @Test
    void testPythonRewriteRule_Listing2() {
        String before = """
                with tf.device('/cpu:0'):
                    B, H, T, _ = q.getshape().as_list()""";
        String after = "B, H, T, _ = q.getshape().as_list()";
        String expectedMatch = """
                with tf.device('/cpu:0'):
                    :[l7] = :[[l14]].:[[l16]]:[l17].:[[l19]]:[l17]""";
        String expectedReplace = ":[l7] = :[[l14]].:[[l16]]:[l17].:[[l19]]:[l17]";
        RewriteRule rw = new RewriteRule(before, after,  Language.Python);
        assertTrue(areAlphaEquivalent(expectedMatch,rw.getMatch().getTemplate()));
        assertTrue(areAlphaEquivalent(expectedReplace,rw.getReplace().getTemplate()));
    }

    @Test
    void testPythonRewriteRule_Listing4() {
        String before = """
                input.grad.data.zero()""";
        String after = """
                with torch.no_grad():
                    input.grad.zero()""";
        String expectedMatch = ":[[l1]]:[l2].data.:[l6]";
        String expectedReplace = """
                with torch.no_grad():
                    :[[l1]]:[l2]:[l6]""";
        RewriteRule rw = new RewriteRule(before, after,  Language.Python);
        assertTrue(areAlphaEquivalent(expectedMatch,rw.getMatch().getTemplate()));
        assertTrue(areAlphaEquivalent(expectedReplace,rw.getReplace().getTemplate()));
    }

    @Test
    void testPythonRewriteRule_Listing5() {
        String before = """
                file = tf.gfile.GFile(label_path)
                dataset = csv.DictReader(file, delimiter="")""";
        String after = """
                with tf.gfile.GFile(label_path) as file:
                    dataset = csv.DictReader(file, delimiter="")""";
        String expectedMatch = """
                :[[l1]] = :[l3]
                :[[l13]] = :[[l16]].:[[l18]](:[[l1]], :[l21])""";
        String expectedReplace = """
                with :[l3] as :[[l1]]:
                    :[[l13]] = :[[l16]].:[[l18]](:[[l1]], :[l21])""";
        RewriteRule rw = new RewriteRule(before, after,  Language.Python);
        assertTrue(areAlphaEquivalent(expectedMatch,rw.getMatch().getTemplate()));
        assertTrue(areAlphaEquivalent(expectedReplace,rw.getReplace().getTemplate()));
    }

    @Test
    void testPythonRewriteRule_Listing6() {
        String before = """
                return sum(ls) / len(ls)""";
        String after = """
                return np.mean(ls)""";
        String expectedMatch = "return sum(:[[l5]]) / len(:[[l5]])";
        String expectedReplace = """
                return np.mean(:[[l5]])""";
        RewriteRule rw = new RewriteRule(before, after,  Language.Python);
        assertTrue(areAlphaEquivalent(expectedMatch,rw.getMatch().getTemplate()));
        assertTrue(areAlphaEquivalent(expectedReplace,rw.getReplace().getTemplate()));
    }

    @Test
    void testPythonRewriteRule_Listing7() {
        String before = """
                denominator = np.dot(np.dot(W.T, W), H)""";
        String after = """
                denominator = np.linalg.multi_dot(W.T, W, H)""";
        String expectedMatch = """
                :[[l1]] = :[[l4]].:[[l6]](:[[l4]].:[[l6]](:[[l14]]:[l15], :[[l14]]), :[[l17]])""";
        String expectedReplace = ":[[l1]] = :[[l4]].linalg.multi_dot(:[[l14]]:[l15], :[[l14]], :[[l17]])";
        RewriteRule rw = new RewriteRule(before, after,  Language.Python);
        assertTrue(areAlphaEquivalent(expectedMatch,rw.getMatch().getTemplate()));
        assertTrue(areAlphaEquivalent(expectedReplace,rw.getReplace().getTemplate()));
    }

    @Test
    void testPythonRewriteRule_Listing8() {
        String before = """
                noise = self._diag_tensor[0, 0]
                sub_eigs = []
                for lazy_tensor in self._lazy_tensor.lazy_tensors:
                    sub_eigs.append(lazy_tensor.evaluate().symeig(eigenvectors=True)[0][:, 0].unsqueeze(-1))     
                    eigs = sub_eigs[0].matmul(sub_eigs[1].t())
                torch.log(eigs + noise).sum()""";
        String after = """
                noise = self._diag_tensor[0, 0]
                sub_eigs = [DiagLazyTensor(svd_decomp.S) for svd_decomp in self._kron_svd]
                sub_eigs_kronecker = KroneckerProductLazyTensor(*sub_eigs).diag()
                torch.log(sub_eigs_kronecker + noise).sum()
                """;
        RewriteRule rw = new RewriteRule(before, after,  Language.Python);

        String expectedMatch = """
                :[[l1]] = :[[l4]].:[[l6]][:[[l9]], :[[l9]]]
                :[[l11]] = []
                for :[[l15]] in :[[l4]]._lazy_tensor.lazy_tensors:
                    :[[l11]].append(:[[l15]].evaluate:[l29].symeig(eigenvectors=True):[l35][:, :[[l9]]].unsqueeze(-:[[l42]]))
                    :[[l44]] = :[[l11]]:[l35].matmul(:[[l11]][:[[l42]]].t:[l29])
                :[[l56]].:[[l58]](:[[l44]] + :[[l1]]).:[[l62]]:[l29]""";
        String expectedReplace = """
                :[[l1]] = :[[l4]].:[[l6]][:[[l9]], :[[l9]]]
                :[[l11]] = [DiagLazyTensor(svd_decomp.S) for svd_decomp in :[[l4]]._kron_svd]
                sub_eigs_kronecker = KroneckerProductLazyTensor(*:[[l11]]).diag:[l29]
                :[[l56]].:[[l58]](sub_eigs_kronecker + :[[l1]]).:[[l62]]:[l29]
                """;
        assertTrue(areAlphaEquivalent(expectedMatch,rw.getMatch().getTemplate()));
        assertTrue(areAlphaEquivalent(expectedReplace,rw.getReplace().getTemplate()));
    }

    @Test
    void testPythonRewriteRule_Listing9_comments_singleline() {
        String before = """
                first_occ = [self._get_first_term_occurrence(term) for term in terms]
                # TODO maybe a better function would do here
                sum(first_occ) / len(first_occ)""";
        String after = """
                np.mean([self._get_first_term_occurrence(term)
                                         for term in keyphrase])
                """;
        RewriteRule rw = new RewriteRule(before, after,  Language.Python);

        String expectedMatch = """
                :[[l1]] = [:[[l6]].:[[l8]](:[[l10]]) for :[[l10]] in terms]
                sum:[l17] / len:[l17]
                """;
        String expectedReplace = """
                np.mean([:[[l6]].:[[l8]](:[[l10]])
                                         for :[[l10]] in keyphrase])
                """;
        assertTrue(areAlphaEquivalent(expectedMatch,rw.getMatch().getTemplate()));
        assertTrue(areAlphaEquivalent(expectedReplace,rw.getReplace().getTemplate()));
    }

    @Test
    void testPythonRewriteRule_Listing9_comments_multiline() {
        String before = """
                first_occ = [self._get_first_term_occurrence(term) for term in terms]
                ""\"
                TODO maybe a better function would do here
                Block
                of
                comment
                ""\"
                sum(first_occ) / len(first_occ)""";

        String after = """
                np.mean([self._get_first_term_occurrence(term)
                                         for term in keyphrase])
                """;
        RewriteRule rw = new RewriteRule(before, after,  Language.Python);

        String expectedMatch = """
                :[[l1]] = [:[[l6]].:[[l8]](:[[l10]]) for :[[l10]] in terms]
                sum:[l17] / len:[l17]
                """;
        String expectedReplace = """
                np.mean([:[[l6]].:[[l8]](:[[l10]])
                                         for :[[l10]] in keyphrase])
                """;
        assertTrue(areAlphaEquivalent(expectedMatch,rw.getMatch().getTemplate()));
        assertTrue(areAlphaEquivalent(expectedReplace,rw.getReplace().getTemplate()));
    }

    @Test
    void testPythonRewriteRule_open_to_with() {
        String before = "dataset = dataset.apply(\n" +
                "       tf.contrib.data.batch_and_drop_remainder(FLAGS.batch_size)\n" +
                "   )";

        String after = "dataset = dataset.batch(FLAGS.batch_size, drop_remainder=True)";
        RewriteRule rw = new RewriteRule(before, after,  Language.Python);
        System.out.println(rw);

    }


    @Test
    void testPythonRewriteRule_try_to_with() {
        String before = """
                olderr = np.seterr(divide='ignore')
                try:
                   actual = logit(a)
                finally:
                   np.seterr(olderr)
                """;

        String after = "with np.errstate(divide='ignore'):\n" +
                "  actual = logit(a)";
        RewriteRule rw = new RewriteRule(before, after,  Language.Python);
        System.out.println(rw);

    }

    @Test
    void testPythonRewriteRule_If() {
        String before = """
                title = ""
                if "title" in article:
                  title = article["title"].strip()
                else:
                  title = "\"""";
        String after = "title = article.get(\"title\", \"\").strip()";
        String expectedMatch = """
                for :[[l0]] in :[[l1]]:
                    :[[l3]] += :[[l0]]""";
        String expectedReplace = ":[[l3]] = np.sum(:[[l1]])";
        RewriteRule rw = new RewriteRule(before, after,  Language.Python);
        assertTrue(areAlphaEquivalent(expectedMatch,rw.getMatch().getTemplate()));
        assertTrue(areAlphaEquivalent(expectedReplace,rw.getReplace().getTemplate()));
    }



}
