package com.inferrules.core.languageAdapters;

import com.google.gson.Gson;
import io.vavr.control.Try;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class LanguageSpecificInfo {

    public enum Language {JAVA, PYTHON3}

    private static final Path JavaConfigFile = Paths.get("/Users/ameya/Research/InferRules/src/main/resources/LanguageConfigs/java.json");
    private static final Path PythonConfigFile = Paths.get("/Users/ameya/Research/InferRules/src/main/resources/LanguageConfigs/python.json");
    private static final LanguageConfig JavaLangConfig = Try.of(() -> new Gson().fromJson(Files.readString(JavaConfigFile), LanguageConfig.class))
            .getOrElse(new LanguageConfig());
    private static final LanguageConfig PythonLangConfig = Try.of(() -> new Gson().fromJson(Files.readString(PythonConfigFile), LanguageConfig.class))
            .getOrElse(new LanguageConfig());

    private static final Map<Language, LanguageConfig> configs = Map.of(Language.JAVA, JavaLangConfig, Language.PYTHON3, PythonLangConfig);

    public static List<String> getKeywords(Language language) {
        return configs.get(language).getKeywords();
    }

    public static List<String> getSymbols(Language language) {
        return configs.get(language).getSymbols();
    }

    public static ILanguageAdapter getAdapter(Language l){
        switch (l){
            case JAVA:return new JavaAdapter();
            case PYTHON3:return new PythonAdapter();
        }
        return new JavaAdapter();
    }
}
