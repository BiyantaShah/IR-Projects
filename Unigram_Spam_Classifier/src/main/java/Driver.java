import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Biyanta on 02/08/17.
 */
public class Driver {

    public static File folderPath = new File("/Users/Biyanta/Downloads/trec07p/data");
    public static File[] listOfFiles = folderPath.listFiles();

    public static ElasticSearchConnection elasticSearchConnection = new ElasticSearchConnection();

    public static void main (String[] args) throws IOException {

        elasticSearchConnection.connect();

        elasticSearchConnection.mapSpamHam();

//        IndexFiles index = new IndexFiles();
//        index.indexFiles(listOfFiles);

        elasticSearchConnection.loadTestTrainData();

//        Unigrams unigrams = new Unigrams(elasticSearchConnection);
//        unigrams.computeUnigrams();
//        unigrams.writeUnigrams();
//        unigrams.computeMatrix();
//        unigrams.writeMatrices();

        FeatureMatrix matrix  = new FeatureMatrix(elasticSearchConnection);
        matrix.computeFeatureMatrix();
        matrix.writeMatrices();

        elasticSearchConnection.close();

    }
}
