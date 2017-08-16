import java.io.File;
import java.io.IOException;

/**
 * Created by Biyanta on 14/06/17.
 */
public class Driver {

    public static void main(String [] args) throws IOException {

        File opFolder = new File("Documents1");
        opFolder.mkdir();

        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

        WebCrawl webCrawl = new WebCrawl();

        webCrawl.setUp();

        try {
            webCrawl.startCrawl();

        } catch (Exception e) {
            e.printStackTrace();
        }


//        ElasticSearchConnection es = new ElasticSearchConnection();
//
//        es.connect();
//        es.indexDocs();

    }
}
