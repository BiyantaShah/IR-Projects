import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Biyanta on 15/08/17.
 */
public class ConfusionMatrix {

    public static void main (String[] args) throws IOException {

        Map<String,String> docClusterMapping = new HashMap<String,String>();

        // read the output file of WEKA
        BufferedReader opReader = new BufferedReader(new FileReader("Output"));
        String line = new String();

        while ((line = opReader.readLine()) != null) {

            String[] values = line.split(" ");
            docClusterMapping.put(values[0].trim(), values[1].trim());
        }
        opReader.close();
        System.out.println("Output file read");
        // construct the confusion matrix

        int[][] confusionMatrix = new int[2][2];

        BufferedReader reader = new BufferedReader(new FileReader("QueryCombinations.txt"));

        while((line = reader.readLine()) != null) {

            String[] values = line.split("&&");
            String query1 = values[0].split(":")[0];
            String docID1 = values[0].split(":")[1];
            String query2 = values[1].split(":")[0];
            String docID2 = values[1].split(":")[1];

            if (query1.equals(query2)) {

                if (docClusterMapping.get(docID1).equals(docClusterMapping.get(docID2))) {
                    // same query same cluster
                    confusionMatrix[0][0] = confusionMatrix[0][0] + 1;
                }
                else {
                    // same query different clusters
                    confusionMatrix[0][1] = confusionMatrix[0][1] + 1;
                }
            }
            else {
                if (docClusterMapping.get(docID1).equals(docClusterMapping.get(docID2))) {
                    // different query same cluster
                    confusionMatrix[1][0] = confusionMatrix[1][0] + 1;
                }
                else {
                    // different query different cluster
                    confusionMatrix[1][1] = confusionMatrix[1][1] + 1;
                }
            }

        }

        reader.close();

        for(int i = 0; i < 2; i++) {
            for(int j = 0;j < 2; j++) {
                System.out.print(confusionMatrix[i][j]+" ");
            }
            System.out.println();
        }

        double numerator = (confusionMatrix[0][0] + confusionMatrix[1][1]);
        double denominator = (confusionMatrix[0][0] + confusionMatrix[0][1] + confusionMatrix[1][0]
                + confusionMatrix[1][1]);

        double accuracy = numerator/denominator;

        System.out.println("Accuracy "+ (accuracy*100));

    }

}
