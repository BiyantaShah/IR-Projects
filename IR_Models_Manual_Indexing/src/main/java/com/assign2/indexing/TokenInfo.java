package com.assign2.indexing;

/**
 * Created by Biyanta on 03/06/17.
 */
public class TokenInfo implements Comparable<TokenInfo> {

    private String docId;
    private String term;
    private int count;
    private String termPos;
    private int docFreq;

    public TokenInfo(String docId, String term, int count, String position) {

        super();
        this.docId = docId;
        this.term = term;
        this.count = count;
        this.termPos = position;
    }

    public TokenInfo(String docId, int count, String term) {

        super();
        this.docId = docId;
        this.count = count;
        this.term = term;
    }


    public int getDocFreq() {
        return docFreq;
    }

    public void setDocFreq(int docFreq) {
        this.docFreq = docFreq;
    }

    public TokenInfo(String docId, int count, String term, int docFrequency) {

        super();
        this.docId = docId;

        this.count = count;
        this.term = term;
        this.docFreq = docFrequency;
    }


    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getTermPos() {
        return termPos;
    }

    public void setTermPos(String termPos) {
        this.termPos = termPos;
    }




    public int compareTo(TokenInfo o) {
        return o.count - this.count;
    }

    public String toString() {
        return "[DOCNO=" + docId + "count "+ count + ", term=" + term +  ", Pos=" + termPos +"]";
    }
}
