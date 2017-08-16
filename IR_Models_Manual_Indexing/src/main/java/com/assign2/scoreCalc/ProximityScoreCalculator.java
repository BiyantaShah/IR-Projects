package com.assign2.scoreCalc;

import com.assign2.indexing.Indexing;
import com.assign2.indexing.TokenInfo;
import com.assign2.models.ProximitySearch;
import com.assign2.processing.OutputFile;
import com.assign2.processing.Sorting;

import java.io.*;
import java.util.*;

/**
 * Created by Biyanta on 09/06/17.
 */
public class ProximityScoreCalculator {

    private RandomAccessFile randomAccessFile = null;

    public static Properties docsLengthValue = new Properties();
    public static Map<String, Long> termOffset = new HashMap<String, Long>();

    public void readFiles() {

        // read the catalog files to find the offset
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File("Indexed/FinalCatalogFile.txt")));
            String line = null;
            while((line=  reader.readLine())!= null){
                String[] temp = line.split(" ");
                termOffset.put(temp[0], Long.valueOf(temp[1]));
            }
            reader.close();

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        try {
            docsLengthValue.load(ProximityScoreCalculator.class.getClassLoader().
                    getResourceAsStream("docLength.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // read the inverted index

        try {
            randomAccessFile = new RandomAccessFile(new File("Indexed/FinalInvertedFile.txt"), "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void calculateScore(String[] queryTerms) {

        List<TokenInfo> proximityDocs = new ArrayList<TokenInfo>();

        for (int i = 0; i < queryTerms.length; i++) {

            String term = Indexing.getStemOfWord(queryTerms[i].trim().toLowerCase());

            if (!termOffset.containsKey(term)) {
                continue;
            }

            List<TokenInfo> docs = getDocumentsForTerm(term);
            proximityDocs.addAll(docs);

        }

        Map<String, List<String>> tempDocsList = new HashMap<String, List<String>>();

        for (TokenInfo tokenInfo : proximityDocs) {

            if (tempDocsList.containsKey(tokenInfo.getDocId())) {

                tempDocsList.get(tokenInfo.getDocId()).add(tokenInfo.getTermPos());
            }
            else  {
                List<String> posList = new ArrayList<String>();
                posList.add(tokenInfo.getTermPos());
                tempDocsList.put(tokenInfo.getDocId(), posList);
            }
        }

        Map<String,Double> proximityScoreDocList= new HashMap<String, Double>();
        Iterator<Map.Entry<String,List<String>>> tempIter = tempDocsList.entrySet().iterator();

        while (tempIter.hasNext()) {
            Map.Entry<String,List<String>> entry = tempIter.next();
            List<String> matchingTerm = entry.getValue();
            List<List<Integer>> posList = new ArrayList<List<Integer>>();

            for (String match : matchingTerm) {

                List<Integer> positionList = new ArrayList<Integer>();
                String[] pos = match.split("-");
                positionList.add(Integer.valueOf(pos[0]));

                int positionCount = Integer.valueOf(pos[0]);

                for (int i = 1 ; i < pos.length;i++) {

                    positionCount = positionCount+ Integer.valueOf(pos[i]);
                    positionList.add(positionCount);
                }

                posList.add(positionList);
            }

            int minimumSpan;
            if (posList.size() == 1) {
                minimumSpan = 1;
            }
            else {
                try {
                    minimumSpan = ProximitySearch.scoreForDoc(posList);
                } catch (Exception e) {
                    minimumSpan = 1;
                }
            }

            double score = proximityScore(minimumSpan, posList.size(), entry.getKey());
            proximityScoreDocList.put(entry.getKey(), score);

        }

        Map<String,Double> sortedScore = Sorting.sortByComparator(proximityScoreDocList);

        try {
            OutputFile.outputResultsToFile(sortedScore, queryTerms[0], "Output/proximity_results.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Proximity "+queryTerms[0]);
    }

    private double proximityScore(int minimumSpan, int size, String docId) {

        return (1500.0 - minimumSpan) * size /
                (Long.valueOf(String.valueOf(docsLengthValue.get(docId))) + 174662);
    }

    private List<TokenInfo> getDocumentsForTerm(String term) {

        List<TokenInfo> tokens = new ArrayList<TokenInfo>();
        Long offset = termOffset.get(term);

        try {
            randomAccessFile.seek(offset);

            StringBuilder sb = new StringBuilder(randomAccessFile.readLine());
            String[] documents = sb.toString().split("=")[1].split(";");

            for (String document : documents) {

                if (document.contains("!")) {
                    String[] tokenData = document.split("%");
                    String[] docData = tokenData[1].split("#");
                    String[] tfData = docData[1].split("~");

                    tokens.add(new TokenInfo(docData[0], term, Integer.parseInt(tfData[0]), tfData[1]));
                }
                else {
                    String[] docData = document.split("#");
                    String[] tfData = docData[1].split("~");

                    tokens.add(new TokenInfo(docData[0], term, Integer.parseInt(tfData[0]), tfData[1]));
                }
            }
            return tokens;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void close() {
        try {
            randomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
