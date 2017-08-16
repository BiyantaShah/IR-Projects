package com.assign1.indexing;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Biyanta on 26/05/17.
 */
public class Doc {

    @SerializedName("docno")
    private String DOCNO;
    @SerializedName("text")
    private String TEXT;


    public String getDOCNO() {
        return DOCNO;
    }

    public void setDOCNO(String DocNo) {
        // DOCNO = updateText(DOCNO,dOCNO);
        DOCNO = DocNo.substring(10,23);
    }

    public String getTEXT() {
        return TEXT;
    }
    public void setTEXT(String text) {
        TEXT = updateText(TEXT,text);
    }

    private String updateText(String existingText, String newText){
        if(null == existingText){
            return newText;
        }
        return existingText + " " + newText;
    }

    @Override
    public String toString() {
        return "DOC [DOCNO=" + DOCNO + ", TEXT=" + TEXT + "]";
    }
}
