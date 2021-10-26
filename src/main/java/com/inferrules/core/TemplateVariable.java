package com.inferrules.core;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.regex.Pattern;

public class TemplateVariable {

    private final Kind kind;
    private final String Name;
    private final String Text;

    private TemplateVariable(Kind kind, String name) {
        this.kind = kind;
        Name = name;
        Text =  MessageFormat.format(kind.getTemplate(), Name);;
    }

    public TemplateVariable(String name, String value) {
        this.kind = Arrays.stream(Kind.values()).filter(x -> x.matches(value))
                .findFirst().orElse(Kind.ANYTHING);
        this.Name = name;
        this.Text = MessageFormat.format(kind.getTemplate(), Name);
    }

    public boolean hasName(String n){
        return n.equals(Name);
    }

    public TemplateVariable rename(String after){
        return new TemplateVariable(this.kind, after);
    }

    public String asText() { return this.Text; }

    public String getName() {
        return Name;
    }

    @Override
    public String toString(){
        return this.Text;
    }

    enum Kind{
        WORD(1){
            @Override
            public boolean matches(String value){ return Pattern.compile("\\w+").matcher(value).matches();  }
            @Override
            public String getTemplate(){ return ":[[{0}]]";}
        },
        DIGIT(2){
            @Override
            public boolean matches(String value){ return Pattern.compile("\\d+").matcher(value).matches(); }
            @Override
            public String getTemplate(){ return ":[{0}~\\d+]";}
        },
        ANYTHING(3){
            @Override
            public boolean matches(String value){ return true; }
            @Override
            public String getTemplate(){ return ":[{0}]";}
        };
        final int kindId;

        int getKindId(){
            return kindId;
        }
        Kind(int id) {
            this.kindId = id;
        }

        public boolean matches(String value){ return false;}

        public String getTemplate(){ return "[{0}]";}
    }

}
