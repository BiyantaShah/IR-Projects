/**
 * Created by Biyanta on 25/07/17.
 */
public class Score implements Comparable<Score> {

    private String docID;
    private String queryID;
    private double score;

    public Score(String docId, String queryId, double score) {
        this.docID = docId;
        this.queryID = queryId;
        this.score = score;
    }


    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public String getQueryID() {
        return queryID;
    }

    public void setQueryID(String queryID) {
        this.queryID = queryID;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public int compareTo(Score o) {
        return Double.compare(o.getScore(), this.getScore());
    }
}
