package org.inferrules;
import com.inferrules.comby.jsonResponse.CombyMatch;
import com.inferrules.core.RewriteRule;
import com.inferrules.core.languageAdapters.LanguageSpecificInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.inferrules.Utils.areAlphaEquivalent;


public class TestRewriteRules {


    @Test
    void testJavaRewriteRule1() {
        String before = "Utils.transform(x);";
        String after = "x.map(Utils::transform);";
        String expectedMatch = ":[[l1]].:[[l2]](:[[l3]]);";
        String expectedReplace = ":[[l3]].map(:[[l1]]:::[[l2]]);";
        RewriteRule rw = new RewriteRule(before, after, LanguageSpecificInfo.Language.Java);
        Assertions.assertTrue(areAlphaEquivalent(expectedMatch,rw.getMatch().getTemplate()));
        Assertions.assertTrue(areAlphaEquivalent(expectedReplace,rw.getReplace().getTemplate()));
    }



    @Test
    void testJavaRewriteRule2() {
        String before = "fn.apply(x.y());";
        String after = "fn.applyAsInt(x.y());";
        String expectedMatch = ":[[l1]].apply(:[l3]);";
        String expectedReplace = ":[[l1]].applyAsInt(:[l3]);";
        RewriteRule rw = new RewriteRule(before, after, LanguageSpecificInfo.Language.Java);
        Assertions.assertTrue(areAlphaEquivalent(expectedMatch,rw.getMatch().getTemplate()));
        Assertions.assertTrue(areAlphaEquivalent(expectedReplace,rw.getReplace().getTemplate()));
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
        RewriteRule rw = new RewriteRule(before, after, LanguageSpecificInfo.Language.Java);
        Assertions.assertTrue(areAlphaEquivalent(expectedMatch,rw.getMatch().getTemplate()));
        Assertions.assertTrue(areAlphaEquivalent(expectedReplace,rw.getReplace().getTemplate()));
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
                :[[l4]]<:[[l7]], :[[l8]]> :[[l10]] = new HashMap<>();
                for(:[[l7]] :[[l15]] : :[[l16]]){
                 if(!:[[l10]].containsKey(:[[l15]])){
                    :[[l10]].:[[l26]](:[[l15]], :[[l28]]);
                 }
                 :[[l10]].:[[l26]](:[[l15]], :[[l10]].get(:[[l15]])+:[[l28]]);
                }""";
        String expectedReplace = ":[[l4]]<:[[l7]], :[[l8]]> collect = :[[l16]].stream().collect(groupingby(:[[l15]]->:[[l15]], counting()));";
        RewriteRule rw = new RewriteRule(before, after, LanguageSpecificInfo.Language.Java);
        Assertions.assertTrue(areAlphaEquivalent(expectedMatch,rw.getMatch().getTemplate()));
        Assertions.assertTrue(areAlphaEquivalent(expectedReplace,rw.getReplace().getTemplate()));
    }

    @Test
    void testPythonRewriteRule1() {
        String before = """
                count = 0
                for e in es:
                    count += e
                print(count)""";
        String after = "count = np.sum(es)";
        String expectedMatch = """
                :[[l1]] = 0
                for :[[l5]] in :[[l6]]:
                    :[[l1]] += :[[l5]]
                print(:[[l1]])""";
        String expectedReplace = ":[[l1]] = np.sum(:[[l6]])";
        RewriteRule rw = new RewriteRule(before, after, LanguageSpecificInfo.Language.Python);
        Assertions.assertTrue(areAlphaEquivalent(expectedMatch,rw.getMatch().getTemplate()));
        Assertions.assertTrue(areAlphaEquivalent(expectedReplace,rw.getReplace().getTemplate()));
    }


}
