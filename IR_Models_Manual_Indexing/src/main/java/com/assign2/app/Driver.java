package com.assign2.app;

import com.assign2.indexing.Indexing;
import com.assign2.processing.CleanQuery;
import com.assign2.processing.DocumentIdMapping;
import com.assign2.scoreCalc.JMScoreCalculator;
import com.assign2.scoreCalc.OkapiBM25ScoreCalculator;
import com.assign2.scoreCalc.OkapiScoreCalculator;
import com.assign2.scoreCalc.ProximityScoreCalculator;

import java.io.*;
import java.util.*;

/**
 * Created by Biyanta on 03/06/17.
 */
public class Driver {

    public static File folder_path = new File("ap89_collection");
//    public static String query_path = "query_proximitySearch.txt";
    public static String query_path = "query_desc.51-100.short.txt";

    public static File submission_file = new File("in.0.50.txt");

    public static File[] listOfFiles = folder_path.listFiles();

    public static final Set<String> stopWords = new HashSet<String>();

    public static Set<String> docIds = new HashSet<String>();

    public static int TOTAL_DOCUMENTS = 84678;
    public static Long count = 1L;

    public static Map<String, Long> docToIntMap = new HashMap<String, Long>();

    public static Properties intToDocIdsMapping = new Properties();


    public static void main (String[] args) throws IOException {

//        File opFolder = new File("Output");
//        opFolder.mkdir();

//        File finalIndex = new File("Indexed");
//        finalIndex.mkdir();
//
//        Indexing indexing = new Indexing();
//        indexing.processStopWords();
//        indexing.indexDocs(listOfFiles);

//        DocumentIdMapping.docToIntMapping(docToIntMap);

        intToDocIdsMapping.load(new FileInputStream(new File("intToDocIdsMapping.txt")));

        CleanQuery cq = new CleanQuery();
        List<String> queryList = cq.readQueries();

        System.out.println("Queries Cleaned");

        loadDocIds();


//         For OkapiTF
        ListIterator<String> queryIter = queryList.listIterator();
//        while(queryIter.hasNext()){
//            StringBuilder query = new StringBuilder(queryIter.next());
//
//            //remove all the punctuations from the query
//            String[] queryTerms = cq.queryListWithoutStopwords(query.toString().toLowerCase());
//            OkapiScoreCalculator okapiScoreCalculator = new OkapiScoreCalculator();
//            okapiScoreCalculator.readFiles();
//            okapiScoreCalculator.calculateScore(queryTerms);
//            okapiScoreCalculator.close();
//
//        }

//         for BM25
        queryIter = queryList.listIterator();
        while (queryIter.hasNext()) {
            StringBuilder query = new StringBuilder(queryIter.next());

            // remove all the punctuations
            String[] queryTerms = cq.queryListWithoutStopwords(query.toString().toLowerCase());
            OkapiBM25ScoreCalculator okapibm25ScoreCalculator = new OkapiBM25ScoreCalculator();
            okapibm25ScoreCalculator.readFiles();
            okapibm25ScoreCalculator.calculateScore(queryTerms);
            okapibm25ScoreCalculator.close();
        }

//         For Jelinek Mercer
//        queryIter = queryList.listIterator();
//        while (queryIter.hasNext()) {
//            StringBuilder query = new StringBuilder(queryIter.next());
//
//            // remove all punctuations from the query
//            String[] queryTerms = cq.queryListWithoutStopwords(query.toString().toLowerCase());
//
//            JMScoreCalculator jmScoreCalculator = new JMScoreCalculator();
//            jmScoreCalculator.readFiles();
//            jmScoreCalculator.calculateScore(queryTerms);
//            jmScoreCalculator.close();
//        }

        // for proximity
        // uncomment proximity query list for this part and then run.
//        queryIter = queryList.listIterator();
//        while (queryIter.hasNext()) {
//            StringBuilder query = new StringBuilder(queryIter.next());
//
//            // remove all punctuations from the query
//            String[] queryTerms = cq.queryListWithoutStopwords(query.toString().toLowerCase());
//
//            ProximityScoreCalculator pScoreCalculator = new ProximityScoreCalculator();
//            pScoreCalculator.readFiles();
//            pScoreCalculator.calculateScore(queryTerms);
//            pScoreCalculator.close();
//
//        }

//        finalSubmission(submission_file);

        System.out.println("Done");


    }


    private static void finalSubmission(File submission_file) throws IOException {

        System.out.println("Submission file");

        RandomAccessFile randomAccessFile =
                new RandomAccessFile(new File("Indexed/FinalInvertedFile.txt"), "r");

        File outputFile = new File("Output/out.stop.stem.txt");

        Map<String, Long> termOffset = new HashMap<String, Long>();

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

        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        try {
            BufferedReader reader1 = new BufferedReader(new FileReader(submission_file));
            String term = null;

            while ((term = reader1.readLine()) != null) {

                String stemmed = Indexing.getStemOfWord(term).toLowerCase();
//                String stemmed = term;

                Long offset = termOffset.get(stemmed);
                randomAccessFile.seek(offset);

                StringBuilder sb = new StringBuilder(randomAccessFile.readLine());
                String[] documents = sb.toString().split("=")[1].split(";");

                int docFreq = documents.length;

                int totalTF = Integer.parseInt(documents[0].split("!")[1].split("%")[0]);

                writer.write(term+" "+docFreq+" "+totalTF+"\n");

            }
            writer.flush();
            writer.close();
            randomAccessFile.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadDocIds() {

        Properties docLengthMap = new Properties();

        try {
            docLengthMap.load(Driver.class
                    .getClassLoader().getResourceAsStream("docLength.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Object key : docLengthMap.keySet()) {
            docIds.add(key.toString());
        }
    }
}
