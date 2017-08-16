package com.assign1.models;

import java.io.*;
import java.util.*;

/**
 * Created by Biyanta on 26/05/17.
 */
public class OutputFile {

    static Map<String, Map< String, Double>> superMap = new LinkedHashMap<String, Map<String, Double>>();

    public static void outputResultsToFile(Map<String, Double> sortedScore, String queryTerm, String filePath) throws IOException {

        forAssignment6(sortedScore, queryTerm, filePath);
//        File okapiTFIDFFile = new File(filePath);
//
//        if(!okapiTFIDFFile.exists())
//            okapiTFIDFFile.createNewFile();
//
//        FileWriter fw = new FileWriter(okapiTFIDFFile,true);
//        Iterator<Map.Entry<String, Double>> qsIter = sortedScore.entrySet().iterator();
//
//        int count = 1;
//        while(qsIter.hasNext()){
//            if(count == 1001){ // only 1000 documents per query
//                break;
//            }
//
//            Map.Entry<String, Double> tempEntry = qsIter.next();
//            fw.write(queryTerm+" "+ "Q0" + " " + tempEntry.getKey()+" "+ count + " " +
//                    tempEntry.getValue()+" " +"IR");
//            ++count;
//            fw.append(System.getProperty("line.separator"));
//        }
//
//        fw.flush();
//        fw.close();
    }

    private static void forAssignment6(Map<String, Double> sortedScore, String queryTerm, String filePath) throws IOException {

        System.out.println(filePath);
        Map<String, Double> finalMap = new LinkedHashMap<String, Double>();
        BufferedReader reader = new BufferedReader(new FileReader("qrels.adhoc.51-100.AP89.txt"));

        BufferedReader reader1 = new BufferedReader(new FileReader("okapiBM25_results.txt"));

        Map< String, List<String>> bm25Map = new LinkedHashMap<String, List<String>>();

        String line1 = new String();

        while ((line1 = reader1.readLine()) != null) {
            String[] bm25 =  line1.split(" ");

            String key = bm25[0].trim();
            String value = bm25[2].trim() + " "+ bm25[4].trim();

            if (bm25Map.containsKey(key)) {
                bm25Map.get(key).add(value);
            }
            else {
                List<String> tempList = new ArrayList<String>();
                tempList.add(value);
                bm25Map.put(key, tempList);
            }
        }

        Map<String, List<String>> qrelMap = new LinkedHashMap<String, List<String>>();

        String line = new String();

        while ((line = reader.readLine()) != null) {
            String[] qrel = line.split(" ");

            if (qrelMap.containsKey(qrel[0].trim())) {
                qrelMap.get(qrel[0].trim()).add(qrel[2].trim());
            }
            else  {
                List<String> tempList = new ArrayList<String>();
                tempList.add(qrel[2].trim());
                qrelMap.put(qrel[0].trim(), tempList);
            }
        }


        List<String> listOfDocs = qrelMap.get(queryTerm);
        System.out.println(queryTerm + " " + listOfDocs.size());


        for (String doc : listOfDocs) {
            if (sortedScore.containsKey(doc))
                finalMap.put(doc, sortedScore.get(doc));
//            else
//                System.out.println(doc);
        }
        System.out.println("finalMap size "+ finalMap.size());


        int sublistSize = 0;
           sublistSize = 1000 -  finalMap.size();
        int x = finalMap.size();
        System.out.println("Sublist size" + sublistSize);

        int i = 1;
        listOfDocs = bm25Map.get(queryTerm);
        System.out.println("lits of docs "+ listOfDocs.size());
        int count = 0;
        for (String doc: listOfDocs) {
            String[] split = doc.split(" ");
            String document = split[0].trim();
            double score = Double.valueOf(split[1].trim());

            count ++;
            if (finalMap.size() ==  1000) {
                System.out.println("ever comes here ");
                break;
            }
//            else if (count <= x) {
//                System.out.println(i++);
//                continue;
//            }
            else {
                if (!finalMap.containsKey(document))
                    finalMap.put(document, score);
            }

        }
        System.out.println("Size " +finalMap.size());

        superMap.put(queryTerm, finalMap);
        System.out.println("Super size"+ superMap.size());

        String fileName = filePath.substring(0, filePath.length()-4);
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName+"1.txt"));

        Iterator<Map.Entry<String, Map<String, Double>>> qsIter = superMap.entrySet().iterator();


        while(qsIter.hasNext()){
            int y = 1;
            Map.Entry<String, Map<String, Double>> tempEntry = qsIter.next();

            Iterator <Map.Entry<String, Double>> iter = tempEntry.getValue().entrySet().iterator();

            while (iter.hasNext()) {
                Map.Entry<String, Double> entry = iter.next();
                writer.write(tempEntry.getKey()+" "+ "Q0" + " "+ entry.getKey()+" "+ y + " " +
                        entry.getValue()+" " +"IR");
                ++y;
                writer.append(System.getProperty("line.separator"));
            }
        }

        writer.flush();
        writer.close();

    }
}
