package com.assign1.processing;

/**
 * Created by Biyanta on 26/05/17.
 */
public class Documents {

    String term;
    long termFrequency;
    String documentId;
    long documentFrequency;
    long length;

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public long getTermFrequency() {
        return termFrequency;
    }

    public void setTermFrequency(long termFrequency) {
        this.termFrequency = termFrequency;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public long getDocumentFrequency() {
        return documentFrequency;
    }

    public void setDocumentFrequency(long documentFrequency) {
        this.documentFrequency = documentFrequency;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }
}
