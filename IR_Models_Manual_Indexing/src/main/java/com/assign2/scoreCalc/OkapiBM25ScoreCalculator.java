package com.assign2.scoreCalc;

import com.assign2.app.Driver;
import com.assign2.indexing.Indexing;
import com.assign2.indexing.TokenInfo;
import com.assign2.models.OkapiBM25;
import com.assign2.processing.OutputFile;
import com.assign2.processing.Sorting;

import java.io.*;
import java.util.*;

/**
 * Created by Biyanta on 08/06/17.
 */
public class OkapiBM25ScoreCalculator {

    private RandomAccessFile randomAccessFile = null;

    public static Properties docsLengthValue = new Properties();
    public static Map<String, Long> termOffset = new HashMap<String, Long>();

    public void readFiles() {

        // read the final catalog file
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File("Indexed/FinalCatalogFile.txt")));

            String line = null;
            while((line = reader.readLine())!= null){

                String[] offset = line.split(" ");
                termOffset.put(offset[0], Long.valueOf(offset[1]));
            }
            reader.close();

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // get the document and its length
        try {
            docsLengthValue.load(JMScoreCalculator.class
                    .getClassLoader().getResourceAsStream("docLength.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get the final inverted index file
        try {
            randomAccessFile = new RandomAccessFile(new File("Indexed/FinalInvertedFile.txt"), "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void calculateScore(String[] queryTerms) {

        List<TokenInfo> bm25Document = new ArrayList<TokenInfo>();

        for(int i = 1 ; i < queryTerms.length;i++){

            String term = Indexing.getStemOfWord(queryTerms[i].trim().toLowerCase());

            if(!termOffset.containsKey(term)){
                continue;
            }
            List<TokenInfo> docs = getDocumentsForTerm(term);
            bm25Document.addAll(docs);
        }

        Map <String, Double> bm25ScoreDocList = new HashMap<String, Double>();

        Iterator<TokenInfo> docIter = bm25Document.iterator();

        while (docIter.hasNext()) {
            TokenInfo tokenDoc = docIter.next();

            double bm25Score = OkapiBM25.okapiBM25Score(Driver.TOTAL_DOCUMENTS,
                    tokenDoc.getDocFreq(), tokenDoc.getCount(),
                    Long.valueOf(docsLengthValue.get(tokenDoc.getDocId()).toString()));

            if(bm25ScoreDocList.containsKey(tokenDoc.getDocId())){

                double bmScore = bm25Score + bm25ScoreDocList.get(tokenDoc.getDocId());
                bm25ScoreDocList.put(tokenDoc.getDocId(), bmScore);

            }
            else{
                bm25ScoreDocList.put(tokenDoc.getDocId(), bm25Score);
            }

        }

        Map<String,Double> sortedScore = Sorting.sortByComparator(bm25ScoreDocList);

        try {
            OutputFile.outputResultsToFile(sortedScore,queryTerms[0], "Output/okapiBM25_results.txt");

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("bm25 scored "+queryTerms[0]);
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

                    tokens.add(new TokenInfo(docData[0], Integer.parseInt(tfData[0]), term, documents.length));

                }
                else {
                    String[] docData = document.split("#");
                    String[] tfData = docData[1].split("~");

                    tokens.add(new TokenInfo(docData[0], Integer.parseInt(tfData[0]), term, documents.length));
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
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
