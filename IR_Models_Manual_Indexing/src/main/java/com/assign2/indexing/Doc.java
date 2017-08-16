package com.assign2.indexing;

import com.assign2.app.Driver;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Biyanta on 03/06/17.
 */
public class Doc {

    @SerializedName("docno")
    private Long DOCNO;
    @SerializedName("text")
    private String TEXT;


    public Long getDOCNO() {
        return DOCNO;
    }

    public void setDOCNO(String DocNo) {
        // DOCNO = updateText(DOCNO,dOCNO);
        String docID = DocNo.substring(10,23);

        if (!Driver.docToIntMap.containsKey(docID)) {
            Driver.docToIntMap.put(docID, Driver.count);
            DOCNO = Driver.count;
            Driver.count++;
        }
        else {
            DOCNO = Driver.docToIntMap.get(docID);
        }

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
