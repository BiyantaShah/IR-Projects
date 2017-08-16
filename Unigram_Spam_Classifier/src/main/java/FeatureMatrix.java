import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.io.*;
import java.util.*;

/**
 * Created by Biyanta on 07/08/17.
 */
public class FeatureMatrix {
    private ElasticSearchConnection esClient;
    private Map<String, String> spamHamMap = new LinkedHashMap<String, String>();
    private Map<String, Integer> featureMap = new LinkedHashMap<String, Integer>();

    private static Map<String, List<TermUnit>> trainMap = new LinkedHashMap<String, List<TermUnit>>();
    private static Map<String, List<TermUnit>> testMap = new LinkedHashMap<String, List<TermUnit>>();

//    String spamList = "mySpamList.txt";
    String spamList = "givenSpamList.txt";


    public FeatureMatrix(ElasticSearchConnection elasticSearchConnection) throws IOException {

        loadIndexFeatureFile();
        esClient = elasticSearchConnection;
        
    }
    

    private void loadIndexFeatureFile() throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader("index"));
        String line = new String();

        while ((line = reader.readLine()) != null) {
            String [] spamHamData = line.split(" ");
            String fileName = spamHamData[1].trim().substring(8);
            spamHamMap.put(fileName, spamHamData[0].trim());
        }
        reader.close();

        BufferedReader reader1 = new BufferedReader(new FileReader(spamList));
        String line1 = new String();
        int count = 1;

        while ((line1 = reader1.readLine()) != null) {
            featureMap.put(line1, count);
            count ++;
        }

        reader1.close();

    }

    public void computeFeatureMatrix() {

        for (String term : featureMap.keySet()) {
            processEntry(term);
        }
        System.out.println("done computing");
    }

    public void processEntry(String term) {

        System.out.println(term);
        SearchResponse searchResponse = esClient.transportClient.prepareSearch(esClient.INDEX_NAME).
                setTypes(esClient.INDEX_TYPE)
                .setScroll(new TimeValue(60000)).setQuery(QueryBuilders.matchQuery("text", term))
                .setSize(1000).setExplain(true).execute().actionGet();

        while (true) {

            for (SearchHit searchHit : searchResponse.getHits().getHits()) {
                String fileName = (String) searchHit.getSource().get("fileName");
                String split = (String) searchHit.getSource().get("split");

                int termFreq = (int) Double.parseDouble((searchHit
                        .getExplanation()
                        .toString()
                        .split("termFreq=")[1])
                        .split("\n")[0]);

                updateMatrix(fileName, new TermUnit(term, termFreq), split );
            }

            searchResponse = esClient.transportClient.
                    prepareSearchScroll(searchResponse.getScrollId()).
                    setScroll(new TimeValue(60000))
                    .execute()
                    .actionGet();

            // Break condition: No hits are returned
            if (searchResponse.getHits().getHits().length == 0) {
                break;
            }
        }
    }

    public void updateMatrix(String fileName, TermUnit termUnit, String split) {

        if (split.equalsIgnoreCase("train")) {

            if (trainMap.containsKey(fileName)) {
                List<TermUnit> existingTerms = trainMap.get(fileName);
                existingTerms.add(termUnit);
                trainMap.put(fileName, existingTerms);
            }
            else {
                List<TermUnit> newTerms = new LinkedList<TermUnit>();
                newTerms.add(termUnit);
                trainMap.put(fileName, newTerms);
            }

        }
        else if(split.equalsIgnoreCase("test")){
            if (testMap.containsKey(fileName)) {
                List<TermUnit> existingTerms = testMap.get(fileName);
                existingTerms.add(termUnit);
                testMap.put(fileName, existingTerms);
            }
            else {
                List<TermUnit> newTerms = new LinkedList<TermUnit>();
                newTerms.add(termUnit);
                testMap.put(fileName, newTerms);
            }
        }
    }

    public void writeMatrices() throws IOException {

        System.out.println("Writing");
        writeMatrix(trainMap, "Matrices/trainGivenMatrix.txt");
        writeMatrix(testMap, "Matrices/testGivenMatrix.txt");
    }

    int count = 1;
    private void writeMatrix(Map<String, List<TermUnit>> Map, String fileName) throws IOException {

        StringBuilder data  =  new StringBuilder();
        String regex = "";
        BufferedWriter writer  = new BufferedWriter(new FileWriter("catalog"+count+".txt"));
        count++;
        System.out.println(Map.size());
        for (String name : Map.keySet()) {
            data.append(regex);
            regex = "\n";

            writer.write(name + " " + (spamHamMap.get(name).equals("spam")? 1: 0) + "\n");
            data.append(spamHamMap.get(name).equals("spam")? 1: 0);

            for (TermUnit tu : Map.get(name)) {
                Integer termId = featureMap.get(tu.getTerm());
                String tf = String.valueOf(tu.getTermFreq());
                data.append(" ");
                data.append(termId);
                data.append(":");
                data.append(tf);
            }
        }
        writer.flush();
        writer.close();

        writeToFile(data, fileName);
    }

    private void writeToFile(StringBuilder data, String fileName) {

        try {
            BufferedWriter writer =  new BufferedWriter(new FileWriter(fileName));
            writer.write(data.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
