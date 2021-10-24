package com.inferrules.core.languageAdapters;

import com.inferrules.core.Node;
import com.inferrules.parsers.Java8Lexer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;
import java.util.stream.Collectors;

public class JavaAdapter implements Code2Node{

    @Override
    public Node parse(String codeSnippet) {
        var tokens = new CommonTokenStream(new com.inferrules.parsers.Java8Lexer(CharStreams.fromString(codeSnippet)));
        com.inferrules.parsers.Java8Parser parser = new com.inferrules.parsers.Java8Parser(tokens);
        ParseTree parseTree = parser.blockStatement();
        List<String> tokenList = tokens.getTokens().stream().map(Token::getText).collect(Collectors.toList());
        return parseTree2Node(parseTree, LanguageSpecificInfo.Language.JAVA, tokenList);
    }
    @Override
    public List<String> tokenize(String codeSnippet) {
        CommonTokenStream x = new CommonTokenStream(new Java8Lexer(CharStreams.fromString(codeSnippet)));
        x.getNumberOfOnChannelTokens();
        return x.getTokens().stream().map(Token::getText).collect(Collectors.toList());
    }
}
