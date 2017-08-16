package com.assign2.scoreCalc;

import com.assign2.app.Driver;
import com.assign2.indexing.Indexing;
import com.assign2.indexing.TokenInfo;
import com.assign2.models.UnigramJelinekMercer;
import com.assign2.processing.OutputFile;
import com.assign2.processing.Sorting;

import java.io.*;
import java.util.*;

/**
 * Created by Biyanta on 08/06/17.
 */
public class JMScoreCalculator {
    private RandomAccessFile randomAccessFile = null;

    public static Properties docsLengthValue = new Properties();
    public static Map<String, Long> termLookUpMap = new HashMap<String, Long>();


    public void readFiles(){

        // for lookup
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("Indexed/FinalCatalogFile.txt")));
            String brStr = null;
            while((brStr=  br.readLine())!= null){
                String[] temp = brStr.split(" ");
                termLookUpMap.put(temp[0], Long.valueOf(temp[1]));
            }
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // For length
        try {
            docsLengthValue.load(JMScoreCalculator.
                    class.getClassLoader().getResourceAsStream("docLength.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            randomAccessFile = new RandomAccessFile(new File("Indexed/FinalInvertedFile.txt"), "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void calculateScore(String[] queryTerms) {
        Map<String,Double> docScores = new HashMap<String,Double>();
        List<TokenInfo> tempQueryTermList = null;

        for(int i = 1; i < queryTerms.length; i++){
            //String term = queryTerms[i].trim().toLowerCase();
            String term = Indexing.getStemOfWord(queryTerms[i].trim().toLowerCase());

            if(termLookUpMap.containsKey(term)){
                tempQueryTermList = null;
                tempQueryTermList = new ArrayList<TokenInfo>(getDocumentsForTerm(term));

                //compute total freq of all this term;
                long TOTAL_TERM_COUNT = 0;
                Iterator<TokenInfo> countDocIter = tempQueryTermList.iterator();
                while(countDocIter.hasNext()){
                    TOTAL_TERM_COUNT= TOTAL_TERM_COUNT + countDocIter.next().getCount();
                }

                if(TOTAL_TERM_COUNT == 0){
                    continue;
                }

                final List<String> docIdsOfExistingQueryTerms = new ArrayList<String>();
                Iterator<TokenInfo> docIter = tempQueryTermList.iterator();
                while(docIter.hasNext()){
                    docIdsOfExistingQueryTerms.add(docIter.next().getDocId());
                }

                Iterator<String>  allDocIds = Driver.docIds.iterator();
                while(allDocIds.hasNext()){
                    String docId = allDocIds.next();
                    if(docIdsOfExistingQueryTerms.contains(docId)){
                        continue;
                    }else{
                        if(Long.valueOf(docsLengthValue.get(docId).toString()) == 0){
                            continue;
                        }
                        tempQueryTermList.add(new TokenInfo(docId, 0, term));
                    }
                }
                // for each of the
                Iterator<TokenInfo> docsIter = tempQueryTermList.iterator();
                while(docsIter.hasNext()){
                    TokenInfo doc = docsIter.next();
                    double jScore = UnigramJelinekMercer.score(doc, Long.valueOf(docsLengthValue.get(doc.getDocId()).toString()),TOTAL_TERM_COUNT);
                    if(docScores.containsKey(doc.getDocId())){
                        double score = jScore+ docScores.get(doc.getDocId());
                        docScores.put(doc.getDocId(), score);
                    }else{
                        docScores.put(doc.getDocId(), jScore);
                    }
                }
            }
        }


        Map<String,Double> sortedScore = Sorting.sortByComparator(docScores);

        //removed
        try {
            OutputFile.outputResultsToFile(sortedScore,queryTerms[0], "Output/jelinekMercer_results.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("jm scored "+queryTerms[0]);


    }

    private List<TokenInfo> getDocumentsForTerm(String term) {
        List<TokenInfo> tokens = new ArrayList<TokenInfo>();
        Long termOffset = termLookUpMap.get(term);
        try {
            randomAccessFile.seek(termOffset);
            StringBuilder docsDescAsStr = new StringBuilder(randomAccessFile.readLine());

            String[] documents = docsDescAsStr.toString().split("=")[1].split(";");

            for (String document : documents) {

                if (document.contains("!")) {
                    String[] tokenData = document.split("%");
                    String[] docData = tokenData[1].split("#");
                    String[] tfData = docData[1].split("~");

                    tokens.add(new TokenInfo(docData[0], Integer.parseInt(tfData[0]), term));

                }
                else {
                    String[] docData = document.split("#");
                    String[] tfData = docData[1].split("~");

                    tokens.add(new TokenInfo(docData[0], Integer.parseInt(tfData[0]), term));
                }


            }
            return tokens;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void close(){
        try {
            randomAccessFile.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
