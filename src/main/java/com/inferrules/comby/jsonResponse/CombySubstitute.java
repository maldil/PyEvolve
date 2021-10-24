package com.inferrules.comby.jsonResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


import java.util.List;


public class CombySubstitute {

    @SerializedName("range")
    @Expose
    private Range range;
    @SerializedName("replacement_content")
    @Expose
    private String replacementContent;
    @SerializedName("environment")
    @Expose
    private List<Object> environment = null;

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public String getReplacementContent() {
        return replacementContent;
    }

    public void setReplacementContent(String replacementContent) {
        this.replacementContent = replacementContent;
    }

    public List<Object> getEnvironment() {
        return environment;
    }

    public void setEnvironment(List<Object> environment) {
        this.environment = environment;
    }

}