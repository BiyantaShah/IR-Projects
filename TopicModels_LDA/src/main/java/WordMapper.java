import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Biyanta on 10/08/17.
 */
public class WordMapper {

    Set<String> stopWordsSet = new HashSet<String>();
    Map<String, AP89> documentMap = new HashMap<String, AP89>();

    Map<String, Set<String>> queryDocumentMap = new HashMap<String, Set<String>>();
    Map<String, Set<String>> queryTokensMap = new HashMap<String, Set<String>>();
    Map<String,Integer> tfCount  = new HashMap<String, Integer>();
    Map<String,Map<String,Double>> queryScoreMapping = new HashMap<String, Map<String,Double>>();

    public void loadFiles(File[] listOfFiles) throws IOException {

        // load stop words
        BufferedReader reader = new BufferedReader(new FileReader("Mapping/stoplist.txt"));

        String line = new String();
        while ((line = reader.readLine()) != null) {

            stopWordsSet.add(line.trim());
        }
        reader.close();

        // load ap89_collection
        for (File doc: listOfFiles) {

            if (validFile(doc)) {
                List<AP89> listOfDocs = parseFile(doc);

                for (AP89 document : listOfDocs) {
                    documentMap.put(document.getDOCNO(), document);
                }
            }

        }

        // load the queries with its document
        BufferedReader reader1 = new BufferedReader(new FileReader("Mapping/okapiBM25.txt"));

        String bm25 = new String();

        while((bm25 = reader1.readLine()) != null) {

            String [] values = bm25.split(" ");
            String query = values[0].trim();
            String doc = values[1].trim();

            if (queryDocumentMap.containsKey(query)) {
                Set<String> documents = queryDocumentMap.get(query);
                documents.add(doc);
                queryDocumentMap.put(query, documents);
            }
            else {
                Set<String> documents = new HashSet<String>();
                documents.add(doc);
                queryDocumentMap.put(query, documents);
            }
        }

        BufferedReader tfReader  = new BufferedReader(new FileReader("Mapping/TFMapping.txt"));

        while((line = tfReader.readLine())!=null){
            String[] data = line.split(" ");
            tfCount.put(data[0].trim(), Integer.valueOf(data[1].trim()));
        }
        tfReader.close();
    }

    private List<AP89> parseFile(File doc) throws IOException {

        Document document = Jsoup.parse(doc, "UTF-8");
        Elements docs = document.getElementsByTag("DOC");

        List<AP89> listOfDocs = new ArrayList<AP89>();

        Iterator<Element> docsIterator = docs.iterator();

        while(docsIterator.hasNext()){
            Element element = docsIterator.next();
            AP89 createdDoc = createDocument(element);

            if(createdDoc!= null){
                listOfDocs.add(createdDoc);
            }
        }

        return listOfDocs;
    }

    private AP89 createDocument(Element element) {

        AP89 doc = new AP89();
        String[] tags = {"DOCNO","TEXT"};

        for(String tag : tags){
            Elements eleTag = element.getElementsByTag(tag);
            Iterator<Element> eleIter = eleTag.iterator();

            while(eleIter.hasNext()){

                String textValue = eleIter.next().toString();

                if(tag.equals("DOCNO"))
                    doc.setDOCNO(textValue);

                else if(tag.equals("TEXT")) {
                    textValue = textValue.replace("<text>", "");
                    textValue = textValue.replace("</text>", "");
                    doc.setTEXT(textValue);
                }

            }
        }

        return doc;
    }

    private boolean validFile(File doc) {

        Pattern p = Pattern.compile("^ap");
        return p.matcher(doc.getName()).find();
    }

    public void generateWordMapper() throws IOException {

        System.out.println("Total Queries: " + queryDocumentMap.size());
        
        for (String query : queryDocumentMap.keySet()) {
            updateTokens(query, queryDocumentMap.get(query));
            System.out.println("mapping for "+ query);
        }

        for (String query : queryDocumentMap.keySet()) {
            generateMatrix(query);
            System.out.println("Matrix for "+ query);
        }
        for (String query: queryDocumentMap.keySet()) {
            System.out.println(query + " "+ queryDocumentMap.get(query).size());
        }
    }

    private void generateMatrix(String query) throws IOException {

        BufferedWriter queryMatrixWriter = new BufferedWriter
                (new FileWriter("Matrices/Matrix_"+query+".txt"));

        BufferedWriter docwriter = new BufferedWriter
                (new FileWriter("FileDocID/MatrixDocMapping_"+query+".txt"));

        Map<String,String> tokenMapping = getTokenMapping(query);

        Set<String> docIDS = queryDocumentMap.get(query);
        int count = 1;
        for (String docID: docIDS) {
            queryMatrixWriter.write("|"+" ");
            AP89 document = documentMap.get(docID);

            String[] cleanedTokens = getTokensForText(document).split(" ");

            for (String token : cleanedTokens) {

                if(tokenMapping.containsKey(token)) {
                    if(token.equals("null"))
                        continue;
                    queryMatrixWriter.write(tokenMapping.get(token)+" ");
                }
            }
            queryMatrixWriter.write("\n");
            docwriter.write(count+" "+docID+"\n");
            ++count;
        }
        queryMatrixWriter.flush();
        queryMatrixWriter.close();

        docwriter.flush();
        docwriter.close();
    }

    private Map<String,String> getTokenMapping(String query) throws IOException {

        Map<String,String> mapping = new HashMap<String, String>();
        BufferedReader reader = new BufferedReader
                (new FileReader("MappedFiles/TokenMapping_"+query+".txt"));

        String line = new String();

        while ((line = reader.readLine())!= null) {
            String[] values = line.split(" ");
            mapping.put(values[0].trim(), values[1].trim());
        }

        reader.close();
        return mapping;
    }

    private void updateTokens(String query, Set<String> documentIDS) throws IOException {

        BufferedWriter writer = new BufferedWriter
                (new FileWriter("MappedFiles/TokenMapping_"+query+".txt"));

        Set<String> tokens = new HashSet<String>();

        for (String docID: documentIDS) {

            AP89 document = documentMap.get(docID);
            String[] cleanedTokens = getTokensForText(document).split(" ");

            for (String token : cleanedTokens) {
                if (! (stopWordsSet.contains(token.toLowerCase().trim())) && (token.length() > 3)) {
                    tokens.add(token.toLowerCase().trim());
                }
            }
        }

        long count = 0;
        for (String token : tokens) {
            writer.write(token +" "+count+"\n");
            ++count;
        }

        writer.flush();
        writer.close();

        queryTokensMap.put(query, tokens);
    }

    private String getTokensForText(AP89 document) {
        return cleanText(new StringBuilder().append(document.getTEXT()).toString());
    }

    private String cleanText(String str) {

        return str.replaceAll("[^a-zA-Z]+", " ");
    }

    public void generateTopics() throws IOException {

        for (String query : queryDocumentMap.keySet()) {
            writeToTopicFile(query);
            System.out.println("Topic file for "+query);
        }

    }

    private void writeToTopicFile(String query) throws IOException {

        int numberOfTopics = 20;
        Map<String, Double> topicsMap = new HashMap<String, Double>();

        BufferedWriter writer = new BufferedWriter(new FileWriter("Topics/Topics_"+query+".txt"));
        BufferedReader reader  = new BufferedReader(new FileReader("Model/Matrix_"+query+"_model.txt"));

        String line = new String();

        // skip the first 10 lines of metadata
        for (int i = 0; i < 10 ; i++) {
            reader.readLine();
        }

        List<Map<String,Double>> topicsList = new ArrayList<Map<String,Double>>(10);

        for(int i = 0 ; i < numberOfTopics;i++){
            topicsList.add(new HashMap<String, Double>());
        }

        double[] weightedScore = new double[numberOfTopics];

        Map<String, String> tokenMapping = new HashMap<String, String>();
        Map<String, String> reverseTokenMapping = new HashMap<String, String>();

        uploadTokenMappings(query, tokenMapping, reverseTokenMapping);

        for (int i = 0; i < queryTokensMap.get(query).size(); i++) {

            line = reader.readLine();

            String[] values = line.split(" ");

            for(int topicIndex = 1; topicIndex <= numberOfTopics; topicIndex++) {
                topicsList.get(topicIndex-1)
                        .put(reverseTokenMapping.get(values[0].trim()), Double.valueOf(values[topicIndex]));
            }
        }

        int count = 1;
        for (Map <String, Double> topicList: topicsList) {

            Map<String, Double> sortedMap = sortByComparator(topicList);
            writer.write("TOPIC-"+count+"\n");
//            System.out.println(query +" " +count);

            Iterator<Map.Entry<String, Double>> iter = sortedMap.entrySet().iterator();

            for(int k = 0 ; k < 30; k++){
                Map.Entry<String,Double> entry = iter.next();
                writer.write(entry.getKey()+":"+entry.getValue()+"\n");
                weightedScore[count-1] = weightedScore[count-1]+
                        entry.getValue() * tfCount.get(entry.getKey());
            }

            topicsMap.put("TOPIC-"+count, weightedScore[count-1]);
            ++count;
        }

        writer.flush();
        writer.close();

        queryScoreMapping.put(query, topicsMap);
        reader.close();
        
//        generateTopicDistribution(query, topicsList);
    }

    private void generateTopicDistribution(String query, List<Map<String, Double>> topicsList) throws IOException {

        BufferedWriter writer = new BufferedWriter
                (new FileWriter("TopicsDist/TopicDist"+query+".txt"));

        for (String docID : queryDocumentMap.get(query)) {
            AP89 doc = documentMap.get(docID);
            Map<String,Integer> docTF = getDocTF(doc);
            writer.write(doc.getDOCNO());

            for(int i = 0; i < 20; i++){
                Map<String,Double> sortedMap = sortByComparator(topicsList.get(i));
                double value = 0.0;

                Iterator<Map.Entry<String, Double>> iter = sortedMap.entrySet().iterator();

                for(int k = 0 ; k < 30; k++){
                    Map.Entry<String,Double> entry = iter.next();
                    if(docTF.containsKey(entry.getKey())){
                        value = value + docTF.get(entry.getKey()) * entry.getValue();
                    }
                }
                if(value != 0.0)
                    writer.write(" TOPIC-"+i+":"+value);
            }
            writer.write("\n");
        }

        writer.flush();
        writer.close();
        System.out.println("Topic Distribution "+ query);

    }

    private Map<String,Integer> getDocTF(AP89 doc) {

        Map<String,Integer> tfMap = new HashMap<String, Integer>();
        String[] words = getTokensForText(doc).split(" ");

        for (String word : words) {
            word = word.toLowerCase().trim();
            if(tfMap.containsKey(word)){
                tfMap.put(word, (tfMap.get(word) + 1));
            }
            else {
                tfMap.put(word, 1);
            }
        }
        return tfMap;
    }

    private Map<String,Double> sortByComparator(Map<String, Double> topicList) {

        // Convert Map to List
        List<Map.Entry<String, Double>> list =
                new LinkedList<Map.Entry<String, Double>>(topicList.entrySet());

        // Sort list with comparator, to compare the Map values
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // Convert sorted map back to a Map
        Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext();) {
            Map.Entry<String, Double> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    private void uploadTokenMappings(String query,
                                     Map<String, String> tokenMapping,
                                     Map<String, String> reverseTokenMapping) throws IOException {

        BufferedReader reader  = new BufferedReader
                (new FileReader("MappedFiles/TokenMapping_"+query+".txt"));

        String line = new String();
        while ((line = reader.readLine()) != null) {

            String [] values = line.split(" ");
            tokenMapping.put(values[0].trim(), values[1].trim());
            reverseTokenMapping.put(values[1].trim(), values[0].trim());
        }

        System.out.println("Token mappings uploaded");
        reader.close();
    }
}
