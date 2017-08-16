import java.io.*;
import java.util.*;

/**
 * Created by Biyanta on 10/08/17.
 */
public class GeneratingUnionFiles {

    static Map<String, Set<String>> qrelMap = new HashMap<String, Set<String>>();
    static Map<String, Set<String>> bm25Score = new HashMap<String, Set<String>>();

    public static void main (String[] args) throws IOException {

        readQrel();
        readOkapiBM25(bm25Score);
    }

    private static void readOkapiBM25(Map<String, Set<String>> scoreMap) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader("okapiBM25_results.txt"));

        String line = new String();

        while ((line = reader.readLine())!= null) {
            String[] feature = line.split(" ");

            if (line.trim().length() == 0)
                continue;

            if (scoreMap.containsKey(feature[0].trim())) { // contains the query ID
                scoreMap.get(feature[0].trim()).add(feature[2].trim());
            } else {
                Set<String> tempSet = new HashSet<String>();
                tempSet.add(feature[2].trim());
                scoreMap.put(feature[0].trim(), tempSet);
            }
        }

        reader.close();

        writeToFile(scoreMap, qrelMap);


    }

    private static void writeToFile(Map<String, Set<String>> scoreMap, Map<String, Set<String>> qrelMap) throws IOException {

        BufferedWriter writer = new BufferedWriter(new FileWriter("Mapping/okapiBM25.txt"));
        Map<String, Set<String>> finalMap = new HashMap<String, Set<String>>();

        for (String query : qrelMap.keySet()) {

            if (scoreMap.containsKey(query)) {
                Set<String> tempMapQrel = qrelMap.get(query);
                Set<String> tempMapBm25 = scoreMap.get(query);
                tempMapBm25.addAll(tempMapQrel);
                finalMap.put(query, tempMapBm25);
            }
        }

        for (String query : finalMap.keySet()) {

            for (String doc : finalMap.get(query))
            writer.write(query+" "+doc+"\n");
        }

        writer.flush();
        writer.close();
    }

    private static void readQrel() throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader("qrels.adhoc.51-100.AP89.txt"));

        String line = new String();
        
        while ((line = reader.readLine())!= null) {
            String[] values = line.split(" ");
            String query = values[0].trim();
            String doc = values[2].trim();

            if (qrelMap.containsKey(query)) {
                Set<String> docIDS = qrelMap.get(query);
                docIDS.add(doc);
                qrelMap.put(query, docIDS);
            }
            else {
                Set<String> docIDS = new HashSet<String>();
                docIDS.add(doc);
                qrelMap.put(query, docIDS);
            }
        }

        reader.close();
        System.out.println(qrelMap.size());
    }
}
