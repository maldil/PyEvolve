
package com.inferrules.comby.jsonResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.inferrules.core.Node;


import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Environment {

    @SerializedName("variable")
    @Expose
    private String variable;
    @SerializedName("value")
    @Expose
    private String value;
    @SerializedName("range")
    @Expose
    private Range range;

    private Node node;

    public String getVariable() {
        Pattern p = Pattern.compile("(.*)_(.*)_equal");
        Matcher mt = p.matcher(this.variable);
        if(mt.matches()){
            this.variable= mt.group(2);
        }else {
            this.variable = variable;
        }
        return variable;
    }

    public void setVariable(String variable) {
        Pattern p = Pattern.compile("(.*)_(.*)_equal");
        Matcher mt = p.matcher(variable);
        if(mt.matches()){
            this.variable= mt.group(2);
        }else {
            this.variable = variable;
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }


    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }
}
