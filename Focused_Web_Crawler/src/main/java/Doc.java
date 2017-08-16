import com.google.gson.annotations.SerializedName;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Biyanta on 14/06/17.
 */
public class Doc {

    @SerializedName("docno")
    private String DOCNO;

    @SerializedName("text")
    private String TEXT;

    @SerializedName("title")
    private String HEAD;

    @SerializedName("out_links")
    private Set<String> outlinks = new HashSet<String>();

    @SerializedName("in_links")
    private Set<String> inlinks = new HashSet<String>();

    @SerializedName("depth")
    private int DEPTH;

    @SerializedName("author")
    private Set<String> authors = new HashSet<String>();

    @SerializedName("HTTPheader")
    private String httpHeader;

    @SerializedName("html_Source")
    private String htmlSource;

    @SerializedName("url")
    private String url;

    public String getDOCNO() {
        return DOCNO;
    }

    public void setDOCNO(String DOCNO) {
        this.DOCNO = DOCNO;
    }

    public String getTEXT() {
        return TEXT;
    }

    public void setTEXT(String TEXT) {
        this.TEXT = TEXT;
    }

    public String getHEAD() {
        return HEAD;
    }

    public void setHEAD(String HEAD) {
        this.HEAD = HEAD;
    }

    public Set<String> getOutlinks() {
        return outlinks;
    }

    public void setOutlinks(Set<String> outlinks) {
        this.outlinks = outlinks;
    }

    public Set<String> getInlinks() {
        return inlinks;
    }

    public void setInlinks(Set<String> inlinks) {
        this.inlinks = inlinks;
    }

    public int getDEPTH() {
        return DEPTH;
    }

    public void setDEPTH(int DEPTH) {
        this.DEPTH = DEPTH;
    }


    public Set<String> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<String> authors) {
        this.authors = authors;
    }

    public String getHttpHeader() {
        return httpHeader;
    }

    public void setHttpHeader(String httpHeader) {
        this.httpHeader = httpHeader;
    }

    public String getHtmlSource() {
        return htmlSource;
    }

    public void setHtmlSource(String htmlSource) {
        this.htmlSource = htmlSource;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }




}
