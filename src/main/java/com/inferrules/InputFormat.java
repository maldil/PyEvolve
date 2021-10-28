package com.inferrules;

public class InputFormat {

    private final String Before;
    private final String After;
    private final String Language;

    public InputFormat(String before, String after, String language) {
        Before = before;
        After = after;
        Language = language;
    }

    public String getBefore() {
        return Before;
    }

    public String getAfter() {
        return After;
    }

    public String getLanguage() {
        return Language;
    }
}
