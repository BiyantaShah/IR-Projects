import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.index.mapper.SourceToParse;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;

/**
 * Created by Biyanta on 14/08/17.
 */
public class DriverB {

    public static File folder_path = new File("ap89_collection");
    public static File[] listOfFiles = folder_path.listFiles();

    public static void main (String[] args) throws Exception {

//        WordMapper mapper = new WordMapper();
//        mapper.loadFiles(listOfFiles);
//        mapper.generateWordMapper();
//        mapper.generateMatrix();
//        mapper.generateTopics();
//        mapper.generateTopicTokenMap();
//        mapper.generateTopicDistribution();

        KmeansClustering();
    }

    private static void KmeansClustering() throws Exception {
        BufferedReader docReader = new BufferedReader(new FileReader("DOCIDwithWekaInput.txt"));
        Map<Integer, String> docMap = new HashMap<Integer, String>();
        String line = new String();
        int count = 0;

        while ((line = docReader.readLine()) != null) {
            count = count + 1;
            docMap.put(count, line.trim());
        }
        System.out.println(count);

        SimpleKMeans kmeans = new SimpleKMeans();

        kmeans.setSeed(10);
        kmeans.setPreserveInstancesOrder(true);
        kmeans.setNumClusters(25);

        BufferedReader reader = new BufferedReader(new FileReader("Input.arff"));
        BufferedWriter writer = new BufferedWriter(new FileWriter("Output"));
        Instances data = new Instances(reader);


        kmeans.buildClusterer(data);
        int[] assignments = kmeans.getAssignments();

        int i = 0;
        for(int clusterNum : assignments) {
            i++;
            String docID = docMap.get(i);
            writer.write(docID +" " +clusterNum+"\n");
        }
        writer.flush();
        writer.close();

        System.out.println(i);
    }
}
