/**
 * Created by Biyanta on 05/07/17.
 */
public class HitsCrawl {

    static ElasticSearchConnection elasticSearchConnection = new ElasticSearchConnection();

    public static void main (String[] args) {

        elasticSearchConnection.connect();
        elasticSearchConnection.fetchAllDocuments();
    }
}
