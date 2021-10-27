package com.inferrules.core;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.regex.Pattern;

public class TemplateVariable {

    private final Type type;
    private final String Name;
    private final String Text;

    private TemplateVariable(Type type, String name) {
        this.type = type;
        Name = name;
        Text =  MessageFormat.format(type.getTemplate(), Name);;
    }

    public TemplateVariable(String name, String value) {
        this.type = Arrays.stream(Type.values()).filter(x -> x.matches(value))
                .findFirst().orElse(Type.ANYTHING);
        this.Name = name;
        this.Text = MessageFormat.format(type.getTemplate(), Name);
    }

    public boolean hasName(String n){
        return n.equals(Name);
    }

    public TemplateVariable rename(String after){
        return new TemplateVariable(this.type, after);
    }

    public String asText() { return this.Text; }

    public String getName() {
        return Name;
    }

    @Override
    public String toString(){
        return this.Text;
    }

    enum Type {
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
        Type(int id) {
            this.kindId = id;
        }

        public boolean matches(String value){ return false;}

        public String getTemplate(){ return "[{0}]";}
    }

}
