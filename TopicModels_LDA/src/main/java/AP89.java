import com.google.gson.annotations.SerializedName;
import org.apache.poi.hssf.record.formula.functions.T;

/**
 * Created by Biyanta on 10/08/17.
 */
public class AP89 {

    @SerializedName("docno")
    private String DOCNO;

    private String FILEID;
    private String FIRST;
    private String SECOND;
    private String HEAD;
    private String BYLINE;
    private String DATELINE;

    @SerializedName("text")
    private String TEXT;

    public String getDOCNO() {
        return DOCNO;
    }

    public void setDOCNO(String DOCNO) {
        this.DOCNO = DOCNO.substring(10,23);
    }

    private String updateText(String existing, String newText) {

        if(null == existing) {
            return newText;
        }
        return existing + " " + newText;
    }

    public String getFILEID() {
        return FILEID;
    }

    public void setFILEID(String FILEID) {
        this.FILEID = updateText(this.FILEID, FILEID);
    }

    public String getFIRST() {
        return FIRST;
    }

    public void setFIRST(String FIRST) {
        this.FIRST = updateText(this.FIRST, FIRST);
    }

    public String getSECOND() {
        return SECOND;
    }

    public void setSECOND(String SECOND) {
        this.SECOND = updateText(this.SECOND, SECOND);
    }

    public String getHEAD() {
        return HEAD;
    }

    public void setHEAD(String HEAD) {
        this.HEAD = updateText(this.HEAD, HEAD);
    }

    public String getBYLINE() {
        return BYLINE;
    }

    public void setBYLINE(String BYLINE) {
        this.BYLINE = updateText(this.BYLINE, BYLINE);
    }

    public String getDATELINE() {
        return DATELINE;
    }

    public void setDATELINE(String DATELINE) {
        this.DATELINE = updateText(this.DATELINE, DATELINE);
    }

    public String getTEXT() {
        return TEXT;
    }

    public void setTEXT(String TEXT) {
        this.TEXT = updateText(this.TEXT, TEXT);
    }



}
