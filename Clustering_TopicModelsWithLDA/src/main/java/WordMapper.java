import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Biyanta on 14/08/17.
 */
public class WordMapper {

    Set<String> stopWordsSet = new HashSet<String>();
    Map<String, AP89> documentMap = new HashMap<String, AP89>();
    Set<String> tokenSet = new HashSet<String>();

    Map<String,String> tokenMapping = new HashMap<String, String>();

    Map<String, Map<String,Double>> topicToTokenMapping = new HashMap<String, Map<String, Double>>();
    Map<String,Map<String,Double>> docToTopicMapping = new HashMap<String, Map<String,Double>>();

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

        System.out.println("Number of docs "+ documentMap.size());
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

        for (AP89 doc : documentMap.values()) {

            String[] tokens = getTokensForText(doc).split(" ");

            for (String token : tokens) {
                tokenSet.add(token.trim().toLowerCase());
            }
        }
        
//        generateTokenMapping();
        System.out.println("Generated token Mapping");
    }

    private void generateTokenMapping() throws IOException {

        BufferedWriter writer = new BufferedWriter(new FileWriter("TokensMapping.txt"));
        long count = 0;
        for (String token : tokenSet) {

            if(validToken(token) && !token.equals("null")) {
                writer.write(token +" "+count+"\n");
                tokenMapping.put(token, String.valueOf(count));
                ++count;

            }
        }
        writer.flush();
        writer.close();
    }

    private boolean validToken(String token) {

        return !stopWordsSet.contains(token.toLowerCase().trim()) && (token.length() > 3);
    }

    private String getTokensForText(AP89 doc) {

        return cleanText(new StringBuilder().append(doc.getTEXT()).toString());
    }

    private String cleanText(String str) {

        return str.replaceAll("[^a-zA-Z]+", " ");
    }

    public void generateMatrix() throws IOException {

        BufferedWriter matrixWriter = new BufferedWriter(new FileWriter("Matrix.txt"));
        BufferedWriter docIdWriter = new BufferedWriter(new FileWriter("DocIdMapping.txt"));

        for (String docID : documentMap.keySet()) {
            docIdWriter.write(docID +" \n");
            matrixWriter.write("|");

            AP89 doc = documentMap.get(docID);
            String [] tokens = getTokensForText(doc).split(" ");

            for (String token : tokens) {

                if (tokenMapping.containsKey(token.trim().toLowerCase())) {

                    matrixWriter.write(" "+ tokenMapping.get(token.trim().toLowerCase()));
                }
            }
            matrixWriter.write("\n");
        }

        matrixWriter.flush();
        matrixWriter.close();

        docIdWriter.flush();
        docIdWriter.close();

        System.out.println("Matrix generated");
    }

    public void generateTopics() throws IOException {

        int numberOfTopics = 200;
        long count = 1;

        BufferedReader reader = new BufferedReader(new FileReader("MatrixModel.txt"));

        for (int i = 0; i < 10; i++) {
            reader.readLine();
        }

        List<Map<String,Double>> topics = new ArrayList<Map<String,Double>>();

        for(int i = 0; i < numberOfTopics;i++){
            topics.add(new HashMap<String, Double>());
        }

        for(int word = 0; word <= tokenMapping.size(); word++){

            String[] values = reader.readLine().trim().split(" ");
            String token = new String();

            for (Map.Entry<String, String> map : tokenMapping.entrySet()) {
                if (map.getValue().equals(values[0].trim())) {
                    token = map.getKey();
                }
            }

            for(int topicIndex = 0; topicIndex < numberOfTopics; topicIndex++) {

                double value = Double.valueOf(values[topicIndex+1]);
                
                topics.get(topicIndex).put(token, value);
            }
        }

        printTopics(topics);

        reader.close();
    }

    private void printTopics(List<Map<String, Double>> topics) throws IOException {

        int wordsPerTopic = 20;

        BufferedWriter writer = new BufferedWriter(new FileWriter("Topics.txt"));
        for (int i = 0 ; i < topics.size() ;i++) {

            writer.write("TOPIC-"+i+"\n");

            Map<String, Double> sortedMap = sortByComparator(topics.get(i));

            Iterator<Map.Entry<String, Double>> iter = sortedMap.entrySet().iterator();

            for (int j = 0 ; j < wordsPerTopic; j++) {

                Map.Entry<String,Double> entry = iter.next();
                writer.write(entry.getKey()+" "+ entry.getValue()+"\n");
            }
        }

        writer.flush();
        writer.close();
        System.out.println("topics printed");
    }

    private Map<String,Double> sortByComparator(Map<String, Double> map) {
        
        // Convert Map to List
        List<Map.Entry<String, Double>> list =
                new LinkedList<Map.Entry<String, Double>>(map.entrySet());

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

    public void generateTopicTokenMap() throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader("Topics.txt"));
        String line = new String();


        String key = new String();
//        int count = 0;

        while ((line = reader.readLine()) != null) {

            if (line.contains("TOPIC")) {
                key = line.trim();
//                count ++;
            }
            else {
                String[] values = line.split(" ");

                if (topicToTokenMapping.containsKey(key)) {
                    Map<String,Double> map = topicToTokenMapping.get(key);
                    map.put(values[0].trim(), Double.valueOf(values[1].trim()));
                    topicToTokenMapping.put(key, map);
                }
                else {
                    Map<String,Double> tempMap = new HashMap<String, Double>();
                    tempMap.put(values[0].trim(), Double.valueOf(values[1].trim()));
                    topicToTokenMapping.put(key, tempMap);
                }

            }
        }
//        topicToTokenMapping.put(key, tempMap);
        System.out.println(topicToTokenMapping.size());

    }

    public void generateTopicDistribution() throws IOException {

        BufferedWriter writer = new BufferedWriter(new FileWriter("Input.arff"));
        BufferedWriter writerDocID = new BufferedWriter(new FileWriter("DOCIDwithWekaInput.txt"));
        int count = 0;

        writer.write("@relation topicDistribution" +"\n");
        writer.write("\n");
        for(int i = 0; i < 200;i++){
            writer.write("@attribute TOPIC-"+i+" numeric"+"\n");
        }
        writer.write("\n"+"@data"+"\n");

        for (String docID : documentMap.keySet()) {
            Map<String, Integer> docTF = getDocTF(documentMap.get(docID));

            Map<String, Double> tempMap = new HashMap<String, Double>();

            for (int  i = 0; i< 200; i++) {
                Map<String,Double> listOfWordsForTopic = topicToTokenMapping.get("TOPIC-"+i);

                double value = 0.0;
                for (String word : listOfWordsForTopic.keySet()) {
                    if(docTF.containsKey(word)){
                        value = value + docTF.get(word) * listOfWordsForTopic.get(word);
                    }
                }
                tempMap.put("TOPIC-"+i, value);
            }
//            docToTopicMapping.put(docID, tempMap);

            // write topic distribution

            writerDocID.write(docID+"\n");

            for(int i = 0; i < 200;i++){

                if(i == 199) {
                    writer.write(tempMap.get("TOPIC-"+i)+"\n");
                }
                else {
                    writer.write(tempMap.get("TOPIC-"+i)+",");
                }
            }
            System.out.println("Topic doc for "+count ++ + " created");
        }

        writerDocID.flush();
        writer.flush();

        writerDocID.close();
        writer.close();
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
}
