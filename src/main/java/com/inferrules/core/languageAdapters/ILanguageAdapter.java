package com.inferrules.core.languageAdapters;

import com.inferrules.core.Node;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;

import static com.inferrules.utils.Utilities.joinTokens;

public interface ILanguageAdapter {

    Node parse(String codeSnippet);

    List<String> tokenize(String codeSnippet);

    String removeComments(String removeComments);

    default Node parseTree2Node(ParseTree parseTree,  Language language, List<String> tokens) {
        Interval sourceInterval = parseTree.getSourceInterval();
        switch (parseTree.getChildCount()) {
            case 0: {
                return new Node(joinTokens(tokens, sourceInterval), language, sourceInterval, parseTree.getText());
            }
            // do not compress the pattern NT-> T
            case 1:
                if (parseTree.getChild(0).getChildCount() == 0) {
                    List<Node> children = List.of(parseTree2Node(parseTree.getChild(0), language, tokens));
                    return new Node(joinTokens(tokens, sourceInterval), children, language, sourceInterval, parseTree.getText());
                }
                return parseTree2Node(parseTree.getChild(0), language, tokens);
            default:
                List<Node> children = new ArrayList<>();
                int bound = parseTree.getChildCount();
                for (int x = 0; x < bound; x++) {
                    if (parseTree.getChild(x).getSourceInterval().a != -1) {
                        Node node = parseTree2Node(parseTree.getChild(x), language, tokens);
                        children.add(node);
                    }
                }
                return new Node(joinTokens(tokens, parseTree.getSourceInterval()), children, language, sourceInterval, parseTree.getText());
        }
    }


}
