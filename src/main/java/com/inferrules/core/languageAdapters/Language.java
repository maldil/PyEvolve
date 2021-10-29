package com.inferrules.core.languageAdapters;

import com.google.gson.Gson;
import io.vavr.control.Try;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public enum Language {
    Python {
        @Override
        public List<String> getKwdSymbols() {
            return List.of("+", "-", "*", "**", "/", "//", "%", "<<", ">>", "&", "|", "^", "~", "<", ">", "<=", ">=", "==", "!=", "<>", "+=", "-=", "*=", "/=", "//=", "%=", "**=", "&=", "|=", "^=", ">>=", "<<=", "#", "@", "\\", "(", ")", "[", "]", "{", "}", "<", ">", ",", ":", ".", "=", ";");
        }

        @Override
        public List<String> getKwds() {
            return List.of("+", "-", "*", "**", "/", "//", "%", "<<", ">>", "&", "|", "^", "~", "<", ">", "<=", ">=", "==", "!=", "<>", "+=", "-=", "*=", "/=", "//=", "%=", "**=", "&=", "|=", "^=", ">>=", "<<=", "#", "@", "\\", "(", ")", "[", "]", "{", "}", "<", ">", ",", ":", ".", "=", ";");
        }

        @Override
        public ILanguageAdapter getAdapter() {
            return new PythonAdapter();
        }
    },
    Java {
        @Override
        public List<String> getKwdSymbols() {
            return List.of("=", "+=", "-=", "*=", "/=", "%=", "&=", "^=", "|=", "<<=", ">>=", ">>>=", "?", ":", "||", "&&", "|", "^", "&", "==", "!=", "<", ">", "<=", ">=", "<<", ">>", ">>>", "+", "-", "*", "/", "%", "++", "==", "!", "~", "(", ")", "[", "]", "{", "}", "<", ">", ",", ":", ".", "=", ";", "::");
        }

        @Override
        public List<String> getKwds() {
            return List.of("abstract", "continue", "for", "new", "switch", "assert", "default", "goto", "package", "synchronized", "boolean", "do", "if", "private", "this", "break", "double", "implements", "protected", "throw", "byte", "else", "import", "public", "throws", "case", "enum", "instanceof", "return", "transient", "catch", "extends", "int", "short", "try", "char", "final", "interface", "static", "void", "class", "finally", "long", "strictfp", "volatile", "const", "float", "native", "super", "while");
        }

        @Override
        public ILanguageAdapter getAdapter() {
            return new JavaAdapter();
        }
    };

    public List<String> getKwdSymbols() {
        return new ArrayList<>();
    }

    public List<String> getKwds() {
        return new ArrayList<>();
    }

    public ILanguageAdapter getAdapter() {
        return null;
    }
}

