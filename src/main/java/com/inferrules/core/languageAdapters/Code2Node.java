package com.inferrules.core.languageAdapters;

import com.inferrules.core.Node;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.inferrules.utils.Utilities.joinTokens;
import static java.util.Collections.singletonList;

public interface Code2Node {

    Node parse(String codeSnippet);

    List<String> tokenize(String codeSnippet);

    default Node parseTrees2Node(List<ParseTree> parseTrees, LanguageSpecificInfo.Language language, List<String> tokens) {
        List<Node> nodes = parseTrees.stream().map(x -> parseTree2Node(x, language, tokens)).collect(Collectors.toList());
        String text = parseTrees.stream().map(x->x.getText()).collect(Collectors.joining());
        return new Node(String.join("",tokens), nodes, language, new Interval(0, tokens.size()), text);
    }

    default Node parseTree2Node(ParseTree parseTree, LanguageSpecificInfo.Language language, List<String> tokens) {
        Interval sourceInterval = parseTree.getSourceInterval();
        switch (parseTree.getChildCount()) {
            case 0: {
                return new Node(joinTokens(tokens, parseTree.getSourceInterval()), language, sourceInterval, parseTree.getText());
            }
            // do not compress the pattern NT-> T
            case 1:
                return parseTree.getChild(0).getChildCount() == 0 ? new Node(joinTokens(tokens, parseTree.getSourceInterval())
                        , singletonList(parseTree2Node(parseTree.getChild(0), language, tokens)), language, sourceInterval, parseTree.getText())
                        : parseTree2Node(parseTree.getChild(0), language, tokens);
            default:
                return new Node(joinTokens(tokens, parseTree.getSourceInterval()), IntStream.range(0, parseTree.getChildCount())
                        .filter(x -> parseTree.getChild(x).getSourceInterval().a != -1)
                        .mapToObj(x -> parseTree2Node(parseTree.getChild(x), language, tokens))
                        .collect(Collectors.toList()), language, sourceInterval, parseTree.getText());
        }
    }

}
