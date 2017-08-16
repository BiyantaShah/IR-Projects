/**
 * Created by Biyanta on 07/08/17.
 */
public class TermUnit {

    private String term;
    private int termFreq;

    public TermUnit(String term, int termFreq) {
        super();
        this.term = term;
        this.termFreq = termFreq;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public int getTermFreq() {
        return termFreq;
    }

    public void setTermFreq(int termFreq) {
        this.termFreq = termFreq;
    }
}
