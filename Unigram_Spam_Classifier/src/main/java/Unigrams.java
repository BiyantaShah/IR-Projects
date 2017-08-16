import org.elasticsearch.action.get.GetResponse;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Biyanta on 08/08/17.
 */
public class Unigrams {

    private ElasticSearchConnection esClient;
    Map<String, Integer> unigrams = new LinkedHashMap<String, Integer>();
    private Map<String, String> spamHamMap = new LinkedHashMap<String, String>();


    public Unigrams(ElasticSearchConnection elasticSearchConnection) throws IOException {
        esClient = elasticSearchConnection;
        loadIndexFile();
    }

    private void loadIndexFile() throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader("index"));
        String line = new String();

        while ((line = reader.readLine()) != null) {
            String [] spamHamData = line.split(" ");
            String fileName = spamHamData[1].trim().substring(8);
            spamHamMap.put(fileName, spamHamData[0].trim());
        }
        reader.close();
    }

    public void computeUnigrams() throws IOException {

        BufferedWriter unigramTFWriter = new BufferedWriter(new FileWriter("Unigrams/UnigramsTF.txt"));
        for (int i = 1; i <= 75419; i++) {
            getIndexedDoc(i + "", unigramTFWriter);
            System.out.println("Completed "+ i);
        }
        unigramTFWriter.flush();
        unigramTFWriter.close();

    }

    private void getIndexedDoc(String id, BufferedWriter unigramTFWriter) throws IOException {

        GetResponse response = esClient.transportClient.prepareGet(esClient.INDEX_NAME, esClient.INDEX_TYPE, id)
                .execute()
                .actionGet();

        if (response.isExists()) {
            String fileName = response.getSourceAsMap().get("fileName").toString();
            String split = response.getSourceAsMap().get("split").toString();
            String text = response.getSourceAsMap().get("text").toString();

            extractUnigram(fileName, split, text, unigramTFWriter);
        }
    }

    private void extractUnigram(String fileName, String split, String text, BufferedWriter unigramTFWriter) throws IOException {

        unigramTFWriter.write(fileName +" "+split+ " ");
        Map<String, Integer> document = new LinkedHashMap<String, Integer>();

        String[] values = text.split(" ");

        for (String value : values) {
            value = cleanToken(value);

            if (value.length() > 20 && !Pattern.matches("[0-9]+", value))
                continue;

            if(!value.trim().isEmpty()) {
                int size = unigrams.size();
                if (!unigrams.containsKey(value.trim()))
                    unigrams.put(value.trim(), size+1);

                if(document.containsKey(value)){
                    document.put(value, (1+document.get(value)));
                }else {
                    document.put(value, 1);
                }

            }

        }

        for (String term : document.keySet()) {
            unigramTFWriter.write(term + " "+ document.get(term) +" ");
        }
        unigramTFWriter.write("\n");

    }

    private String cleanToken(String s) {
        s = s.replace(".", "");
        s = s.replace("\t", "");
        s = s.replaceAll("\"", "");
        s = s.replaceAll("\\*", "");
        s = s.replace("(", "");
        s = s.replace("{", "");
        s = s.replace("}", "");
        s = s.replace(")", "");
        s = s.replace("[", "");
        s = s.replace(" ", "");
        s = s.replace("]", "");
        s = s.replace(";", "");
        s = s.replace("\n", "");
        s = s.replace("-", "");
        s = s.replace("/", "");
        s = s.replace("#", "");
        s = s.replace("?", "");
        s = s.replace(",", "");
        s = s.replace("+", "");
        s = s.replace("=", "");
        s = s.replace("$", "");
        s = s.replace("%", "");
        s = s.replace("_", "");
        s = s.trim().replaceAll(" +", " ");

        return s;
    }

    public void computeMatrix() throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader("Unigrams/UnigramsTF.txt"));

        BufferedWriter writer1 = new BufferedWriter(new FileWriter("Unigrams/trainUnigrams.txt"));
        BufferedWriter writer2 = new BufferedWriter(new FileWriter("Unigrams/testUnigrams.txt"));
        String line = new String();
        int count = 1;
        while ((line = reader.readLine()) != null) {

            System.out.println("line " + count++);
            String[] value = line.split(" ");
            String fileName = value[0];
            String split = value[1];

            if (split.equals("train")) {

                writer1.write(fileName + ":");
                String [] termUnit = new String[value.length - 2];
                System.arraycopy(value, 2, termUnit, 0, value.length-2);
                for (int  i = 0; i < termUnit.length; i+=2) {
                    writer1.write(termUnit[i]+ " " + termUnit[i+1] + " ");
                }
                writer1.write("\n");

            }
            else if (split.equals("test")) {

                writer2.write(fileName + ":");
                String [] termUnit = new String[value.length - 2];
                System.arraycopy(value, 2, termUnit, 0, value.length-2);
                for (int  i = 0; i < termUnit.length; i+=2) {
                    writer2.write(termUnit[i]+ " " + termUnit[i+1] + " ");
                }
                writer2.write("\n");
            }

        }
        reader.close();

        writer1.flush();
        writer1.close();

        writer2.flush();
        writer2.close();

    }

    public void writeUnigrams() {

        StringBuilder dataString = new StringBuilder();
        String delimiter = "";
        System.out.println("Size of unigrams" +unigrams.size());

        for (Map.Entry<String, Integer> uEntry : unigrams.entrySet()) {
            dataString.append(delimiter);
            delimiter = "\n";
            dataString.append(uEntry.getKey());
            dataString.append(" ");
            dataString.append(uEntry.getValue());
        }
        writeToFile("Unigrams/Unigrams.txt", dataString.toString());
    }

    private void writeToFile(String fileName, String data) {

        try {
            BufferedWriter writer =  new BufferedWriter(new FileWriter(fileName));
            writer.write(data);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeMatrices() throws IOException {

        System.out.println("Writing");
        writeMatrix("Unigrams/trainUnigrams.txt", "Matrices/trainUnigram.txt");
        writeMatrix("Unigrams/testUnigrams.txt", "Matrices/testUnigram.txt");
    }

    private void writeMatrix(String srcFile, String destFile) throws IOException {

        loadUnigrams();

        StringBuilder data  =  new StringBuilder();
        String regex = "";

        BufferedReader reader = new BufferedReader(new FileReader(srcFile));
        String line = new String();
        int count = 1;
        while ((line = reader.readLine()) != null) {

            System.out.println("line "+ count++);
            String [] values = line.split(":");
            String fileName = values[0];
            String [] termunits = values[1].split(" ");

            data.append(regex);
            regex = "\n";

            data.append(spamHamMap.get(fileName).equals("spam")? 1: 0);
            data.append(" ");

            List<TermUnit> sortedTerms = new LinkedList<TermUnit>();
            for (int  i = 0; i < termunits.length; i+=2) {
               sortedTerms.add(new TermUnit(termunits[i], Integer.valueOf(termunits[i+1])));
            }

            Collections.sort(sortedTerms, new Comparator<TermUnit>() {

                @Override
                public int compare(TermUnit o1, TermUnit o2) {
                    if(unigrams.get(o1.getTerm()) <= unigrams.get(o2.getTerm()))
                        return -1;
                    else
                        return 1;
                }
            });

            String separator ="";

            for (TermUnit tu : sortedTerms) {
                Integer termId = unigrams.get(tu.getTerm());
                String tf = String.valueOf(tu.getTermFreq());
                data.append(separator);
                separator =" ";
                data.append(termId);
                data.append(":");
                data.append(tf);
            }
        }

        reader.close();

        writeToFile(destFile, data.toString());
    }


    private void loadUnigrams() throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader("Unigrams/Unigrams.txt"));

        String line = new String();
        unigrams.clear();

        while ((line = reader.readLine()) != null) {

            String[] values = line.split(" ");

            unigrams.put(values[0], Integer.valueOf(values[1]));

        }

        System.out.println("loaded");

    }

}
