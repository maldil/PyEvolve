package com.inferrules.comby.jsonResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


import java.util.List;


public class CombyRewrite {

    @SerializedName("uri")
    @Expose
    private Object uri;
    @SerializedName("rewritten_source")
    @Expose
    private String rewrittenSource;
    @SerializedName("in_place_substitutions")
    @Expose
    private List<CombySubstitute> combySubstitutes = null;
    @SerializedName("diff")
    @Expose
    private String diff;

    public Object getUri() {
        return uri;
    }

    public void setUri(Object uri) {
        this.uri = uri;
    }

    public String getRewrittenSource() {
        return rewrittenSource;
    }

    public void setRewrittenSource(String rewrittenSource) {
        this.rewrittenSource = rewrittenSource;
    }

    public List<CombySubstitute> getInPlaceSubstitutions() {
        return combySubstitutes;
    }

    public void setInPlaceSubstitutions(List<CombySubstitute> combySubstitutes) {
        this.combySubstitutes = combySubstitutes;
    }

    public String getDiff() {
        return diff;
    }

    public void setDiff(String diff) {
       this.diff = diff;
    }

}