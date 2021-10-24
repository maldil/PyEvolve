package com.inferrules.core.languageAdapters;

import com.inferrules.core.Node;
import com.inferrules.parsers.PythonLexer;
import com.inferrules.parsers.PythonParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.inferrules.core.languageAdapters.LanguageSpecificInfo.Language.PYTHON3;

public class PythonAdapter implements ILanguageAdapter {
    @Override
    public Node parse(String codeSnippet) {
        List<String> tokenList = tokenize(codeSnippet);
        var tokens = new CommonTokenStream(new PythonLexer(CharStreams.fromString(codeSnippet)));
        return parseTrees2Node(parse(new PythonParser(tokens), tokenList.size()), PYTHON3, tokenList);
    }


    private List<ParseTree> parse(PythonParser parser, int n){
        List<ParseTree> pts = new ArrayList<>();
        int curr_end = 0;
        while (curr_end <= n){
            ParseTree pt = parser.single_input();
            pts.add(pt);
            curr_end = pt.getSourceInterval().a> pt.getSourceInterval().b ? curr_end+1 :pt.getSourceInterval().b;
        }
        return pts;
    }

    @Override
    public List<String> tokenize(String codeSnippet) {
        CommonTokenStream x = new CommonTokenStream(new PythonLexer(CharStreams.fromString(codeSnippet)));
        x.getNumberOfOnChannelTokens();
        return x.getTokens().stream().map(Token::getText).collect(Collectors.toList());
    }
}
