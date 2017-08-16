/**
 * Created by Biyanta on 02/08/17.
 */
public class InMail {

    private String id;
    private String fileName;
    private String text;
    private String label;
    private String split;

    public InMail(String id, String fileName, String text, String label, String split) {
        this.id = id;
        this.fileName = fileName;
        this.text = text;
        this.label = label;
        this.split = split;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getSplit() {
        return split;
    }

    public void setSplit(String split) {
        this.split = split;
    }
}
