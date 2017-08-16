import com.google.gson.annotations.SerializedName;

/**
 * Created by Biyanta on 14/08/17.
 */
public class AP89 {

    @SerializedName("docno")
    private String DOCNO;

    @SerializedName("text")
    private String TEXT;

    public String getDOCNO() {
        return DOCNO;
    }

    public void setDOCNO(String DOCNO) {
        this.DOCNO = DOCNO.substring(10,23);
    }

    public String getTEXT() {
        return TEXT;
    }

    public void setTEXT(String TEXT) {
        this.TEXT = updateText(this.TEXT, TEXT);
    }

    private String updateText(String existing, String newText) {

        if(null == existing) {
            return newText;
        }
        return existing + " " + newText;
    }


}
