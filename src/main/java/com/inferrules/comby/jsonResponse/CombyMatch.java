
package com.inferrules.comby.jsonResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


import java.util.List;


public class CombyMatch {

    @SerializedName("uri")
    @Expose
    private Object uri;
    @SerializedName("matches")
    @Expose
    private List<com.inferrules.comby.jsonResponse.Match> matches = null;

    public Object getUri() {
        return uri;
    }

    public void setUri(Object uri) {
        this.uri = uri;
    }

    public List<com.inferrules.comby.jsonResponse.Match> getMatches() {
        return matches;
    }

    public void setMatches(List<com.inferrules.comby.jsonResponse.Match> matches) {
        this.matches = matches;
    }

    public boolean isPerfect(String source) {
        return getMatches().size() == 1
                && source.trim().equals(getMatches().get(0).getMatched().replace("\\n","\n").trim());
    }
}
