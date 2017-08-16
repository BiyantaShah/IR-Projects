package com.assign2.scoreCalc;

import com.assign2.indexing.Indexing;
import com.assign2.indexing.TokenInfo;
import com.assign2.models.OkapiTF;
import com.assign2.processing.OutputFile;
import com.assign2.processing.Sorting;

import java.io.*;
import java.util.*;

/**
 * Created by Biyanta on 08/06/17.
 */
public class OkapiScoreCalculator {

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
            docsLengthValue.load(OkapiScoreCalculator.class
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
        
        List<TokenInfo> okapiDocument = new ArrayList<TokenInfo>();

        for(int i = 1 ; i < queryTerms.length;i++){

            String term = Indexing.getStemOfWord(queryTerms[i].trim().toLowerCase());

            if(!termOffset.containsKey(term)){
                continue;
            }
            List<TokenInfo> docs = getDocumentsForTerm(term);
            okapiDocument.addAll(docs);
        }

        Map<String,Double> okapiScoreDocList= new HashMap<String, Double>();

        Iterator<TokenInfo> docIter = okapiDocument.iterator();

        while(docIter.hasNext()){
            TokenInfo tokenDoc = docIter.next();


            double okapiScore = OkapiTF.okapiScore(Long.valueOf(tokenDoc.getCount()),
                    Long.valueOf((String) docsLengthValue.get(tokenDoc.getDocId())));



            if(okapiScoreDocList.containsKey(tokenDoc.getDocId())) {

                double okScore = okapiScore + okapiScoreDocList.get(tokenDoc.getDocId());
                okapiScoreDocList.put(tokenDoc.getDocId(), okScore);

            }
            else{
                okapiScoreDocList.put(tokenDoc.getDocId(), okapiScore);
            }
        }

        Map<String,Double> sortedScore = Sorting.sortByComparator(okapiScoreDocList);

        try {
            OutputFile.outputResultsToFile(sortedScore, queryTerms[0], "Output/okapiTF_results.txt");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println ("Okapi TF Scored "+queryTerms[0]);
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
