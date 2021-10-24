
package com.inferrules.comby.jsonResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;




public class Range {

    @SerializedName("start")
    @Expose
    private com.inferrules.comby.jsonResponse.Start start;
    @SerializedName("end")
    @Expose
    private com.inferrules.comby.jsonResponse.End end;

    public com.inferrules.comby.jsonResponse.Start getStart() {
        return start;
    }

    public void setStart(com.inferrules.comby.jsonResponse.Start start) {
        this.start = start;
    }

    public com.inferrules.comby.jsonResponse.End getEnd() {
        return end;
    }

    public void setEnd(com.inferrules.comby.jsonResponse.End end) {
        this.end = end;
    }

}
