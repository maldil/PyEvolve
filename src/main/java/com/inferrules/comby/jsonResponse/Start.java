
package com.inferrules.comby.jsonResponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;




public class Start {

    @SerializedName("offset")
    @Expose
    private Integer offset;
    @SerializedName("line")
    @Expose
    private Integer line;
    @SerializedName("column")
    @Expose
    private Integer column;

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getLine() {
        return line;
    }

    public void setLine(Integer line) {
        this.line = line;
    }

    public Integer getColumn() {
        return column;
    }

    public void setColumn(Integer column) {
        this.column = column;
    }

}
