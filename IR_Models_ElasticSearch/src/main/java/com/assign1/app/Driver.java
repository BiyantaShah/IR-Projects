package com.assign1.app;

import com.assign1.indexing.Indexing;
import com.assign1.processing.CleanQuery;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Created by Biyanta on 26/05/17.
 */
public class Driver {

    public static File folder_path = new File("ap89_collection");
//    public static String query_path = "Search";
    public static String query_path = "query_desc.51-100.short.txt";

    public static File[] listOfFiles = folder_path.listFiles();
    public static ElasticSearchConnection elasticSearchConnection = new ElasticSearchConnection();
    public static ScoreCalculator scoreCalculator = new ScoreCalculator(elasticSearchConnection);

    static int TOTAL_DOCUMENTS = 84678;

    public static void main (String[] args) throws IOException {

        elasticSearchConnection.connect();

//        Indexing index = new Indexing();
//        index.indexFile(listOfFiles);

        System.out.println("Documents Indexed");

        CleanQuery cq = new CleanQuery();
        List<String> queryList = cq.readQueries();

        System.out.println("Queries Cleaned");

//        elasticSearchConnection.printIds();

        scoreCalculator.readDocProps();

        ListIterator<String> queryIter = queryList.listIterator();
        while(queryIter.hasNext()){
            StringBuilder query = new StringBuilder(queryIter.next());

            //remove all the punctuations
            String[] queryTerms = cq.queryListWithoutStopwords(query.toString());
            scoreCalculator.fetchDocuments(queryTerms);
        }

        System.out.println("Documents fetched");

        // OkapiTF and tf-idf
//        int i = 0;
//        queryIter = queryList.listIterator();
//
//        while(queryIter.hasNext()){
//            StringBuilder query = new StringBuilder(queryIter.next());
//
//            //remove all the punctuations
//            String[] queryTerms = cq.queryListWithoutStopwords(query.toString());
//            scoreCalculator.generateOkapiScore(queryTerms);
//            System.out.println("Okapi TF and TFIDF completed " + (++i));
//        }
//
//
//        // BM25
//        int i = 0;
//        queryIter = queryList.listIterator();
//		while(queryIter.hasNext()){
//			StringBuilder query = new StringBuilder(queryIter.next());
//
//			//remove all the punctuations
//			String[] queryTerms = cq.queryListWithoutStopwords(query.toString());
//            scoreCalculator.generateBM25Score(queryTerms);
//            System.out.println("Okapi BM25 completed " + (++i));
//		}
//
//        // Unigram LM with Laplace smoothing
//        int i = 0;
//        queryIter = queryList.listIterator();
//		while(queryIter.hasNext()){
//			StringBuilder query = new StringBuilder(queryIter.next());
//
//			//remove all the punctuations
//			String[] queryTerms = cq.queryListWithoutStopwords(query.toString());
//            scoreCalculator.generateLaplaceScore(queryTerms);
//            System.out.println("Unigram LM with Laplace Smoothing completed " + (++i));
//		}
//
//		// Unigram LM with Jelinek Mercer
        int i = 0;
        queryIter = queryList.listIterator();
		while(queryIter.hasNext()){
			StringBuilder query = new StringBuilder(queryIter.next());

			//remove all the punctuations
			String[] queryTerms = cq.queryListWithoutStopwords(query.toString());
            scoreCalculator.generateJelinekScore(queryTerms);
            System.out.println("Unigram LM with Jelinek Mercer Smoothing completed " + (++i));
		}


    }
}
