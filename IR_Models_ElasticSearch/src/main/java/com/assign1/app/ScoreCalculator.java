package com.assign1.app;

import com.assign1.models.*;
import com.assign1.processing.Documents;
import com.assign1.processing.Sorting;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.termvectors.TermVectorsResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.json.JSONObject;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.IOException;
import java.util.*;

/**
 * Created by Biyanta on 26/05/17.
 */
public class ScoreCalculator {

    ElasticSearchConnection elasticSearchConnection;

    private final Map<String, List<Documents>> queryTermList = new HashMap();
    private static final List<String> docIds = new ArrayList<String>();
    private final String INDEX_NAME = "biyanta";

    public static Properties docsLengthValue = new Properties();
    public static Properties docProperties = new Properties();

    public ScoreCalculator(ElasticSearchConnection elasticSearchConnection) {
        this.elasticSearchConnection = elasticSearchConnection;
    }

    private String getStemOfWord(String word) {
        PorterStemmer stemValue = new PorterStemmer();
        stemValue.setCurrent(word);
        stemValue.stem();
        return stemValue.getCurrent();
    }

    // calculating the TF for each term
    public void fetchDocuments(String[] queryTerms) {

        for (String queryTerm : queryTerms) {
            String word = getStemOfWord(queryTerm.toLowerCase());

            if (queryTermList.containsKey(word)) {
                continue;
            }

            List<Documents> docList = new ArrayList<Documents>();
            QueryBuilder qb = QueryBuilders.matchQuery("text", word);
            SearchResponse searchRes = elasticSearchConnection
                    .getClient()
                    .prepareSearch(INDEX_NAME)
                    .setScroll(new TimeValue(600000))
                    .setQuery(qb)
                    .setExplain(true)
                    .execute()
                    .actionGet();

            outerLoop:
            while (true) {
                for (SearchHit searchHit : searchRes.getHits().getHits()) {
                    if (searchRes.getHits().getTotalHits() > 10000) {

                        // do not consider terms occurring frequently
                        break outerLoop;
                    }
//                    justPrint(searchHit);

                    Documents document = new Documents();
                    document.setDocumentId(searchHit.getId());
                    document.setDocumentFrequency(searchRes.getHits().getTotalHits());
                    document.setTerm(word);

                    try {

                        long val = (long) Double.parseDouble((searchHit
                                .getExplanation()
                                .toString()
                                .split("termFreq=")[1])
                                .split("\n")[0]);
                        
                        document.setTermFrequency(val);
//                        System.out.println ("DF " + document.getDocumentFrequency());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    docList.add(document);
                }

                searchRes = elasticSearchConnection.
                        getClient()
                        .prepareSearchScroll(searchRes.getScrollId())
                        .setScroll(new TimeValue(60000))
                        .execute()
                        .actionGet();

                // No hits are returned
                if (searchRes.getHits().getHits().length == 0) {
                    break;
                }
            }
            queryTermList.put(word, docList);
        }

    }

    private void justPrint(SearchHit searchHit) {

        TermVectorsResponse response = elasticSearchConnection.transportClient.prepareTermVectors()
                .setIndex(INDEX_NAME)
                .setType("document")
                .setId(searchHit.getId())
                .setSelectedFields("text")
                .execute().actionGet();

        XContentBuilder builder;
        try {

            builder = XContentFactory.jsonBuilder().startObject();
            response.toXContent(builder, ToXContent.EMPTY_PARAMS);

            builder.endObject();

            JSONObject obj = new JSONObject(builder.string());
            JSONObject obj1 = obj.getJSONObject("term_vectors").
                    getJSONObject("text").
                    getJSONObject("terms");

            System.out.println(obj1.toString());

        }catch(Exception e){
            e.printStackTrace();
        }
        System.exit(0);
    }


    public void generateOkapiScore(String[] queryTerms) {

        List<Documents> okapiDocument = new ArrayList<Documents>();

        for(String query :  queryTerms){
            String term = getStemOfWord(query.toLowerCase());

            if(queryTermList.containsKey(term)){
                List<Documents> okapiList = queryTermList.get(term);
                okapiDocument.addAll(okapiList);
            }
        }
        Map<String,Double> okapiScoreDocList= new HashMap<String, Double>();

        Iterator<Documents> docIter = okapiDocument.iterator();
        while(docIter.hasNext()){
            Documents okapiVal = docIter.next();

            double okapiScore = OkapiTF.okapiScore
                    (okapiVal.getTermFrequency(),
                            Long.valueOf((String) docsLengthValue.get(okapiVal.getDocumentId())));

            if(okapiScoreDocList.containsKey(okapiVal.getDocumentId())) {

                double okScore = okapiScore + okapiScoreDocList.get(okapiVal.getDocumentId());
                okapiScoreDocList.put(okapiVal.getDocumentId(), okScore);

            }
            else{
                okapiScoreDocList.put(okapiVal.getDocumentId(), okapiScore);
            }
        }

        Map<String,Double> sorted = Sorting.sortByComparator(okapiScoreDocList);

        try {
            OutputFile.outputResultsToFile(sorted,queryTerms[0], "okapiTF_results.txt");

        } catch (IOException e) {
            e.printStackTrace();
        }

        tfIDFScoring(okapiDocument,okapiScoreDocList,queryTerms[0]);
    }

    private void tfIDFScoring(List<Documents> okapiDocument, Map<String, Double> okapiScoreDocList, String queryTerm) {

        Map<String,Double> tfIdfScoreDocumentList= new HashMap<String, Double>();

        Iterator<Documents> queryIter = okapiDocument.iterator();

        while (queryIter.hasNext()) {

            Documents doc = queryIter.next();

            double tfidfScore = TFIDF.tfidfScore(okapiScoreDocList.get(doc.getDocumentId()),
                    Driver.TOTAL_DOCUMENTS, doc.getDocumentFrequency());

            if (tfIdfScoreDocumentList.containsKey(doc.getDocumentId())) {

                double tfScore = tfidfScore + tfIdfScoreDocumentList.get(doc.getDocumentId());
                tfIdfScoreDocumentList.put(doc.getDocumentId(), tfScore);
            }
            else {
                tfIdfScoreDocumentList.put(doc.getDocumentId(), tfidfScore);
            }
        }

        Map<String,Double> sortedScore = Sorting.sortByComparator(tfIdfScoreDocumentList);

        try {
            OutputFile.outputResultsToFile(sortedScore, queryTerm, "okapiTFIDF_results.txt");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void generateBM25Score(String[] queryTerms) {
        List<Documents> bm25Document = new ArrayList<Documents>();

        for(String query: queryTerms){
            String term = getStemOfWord(query.trim().toLowerCase());
            if(queryTermList.containsKey(term)){
                List<Documents> bm25List = queryTermList.get(term);
                bm25Document.addAll(bm25List);
            }
        }

        Map<String,Double> bm25ScoreDocList= new HashMap<String, Double>();

        Iterator<Documents> docIter = bm25Document.iterator();
        while(docIter.hasNext()){
            Documents doc = docIter.next();

            double bm25Score = OkapiBM25.okapiBM25Score(Driver.TOTAL_DOCUMENTS,
                    doc.getDocumentFrequency(), doc.getTermFrequency(),
                    Long.valueOf(docsLengthValue.get(doc.getDocumentId()).toString()));

            if(bm25ScoreDocList.containsKey(doc.getDocumentId())){

                double bmScore = bm25Score + bm25ScoreDocList.get(doc.getDocumentId());
                bm25ScoreDocList.put(doc.getDocumentId(), bmScore);

            }
            else{
                bm25ScoreDocList.put(doc.getDocumentId(), bm25Score);
            }
        }

        Map<String,Double> sortedScore = Sorting.sortByComparator(bm25ScoreDocList);

        try {
            OutputFile.outputResultsToFile(sortedScore,queryTerms[0], "okapiBM25_results.txt");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void generateLaplaceScore(String[] queryTerms) {

        List<Documents> queryDocument = new ArrayList<Documents>();

        for(int i = 1 ; i < queryTerms.length;i++){
            String term = getStemOfWord(queryTerms[i].trim().toLowerCase());
            if(queryTermList.containsKey(term)){
                List<Documents> docsList = queryTermList.get(term);
                queryDocument.addAll(docsList);
            }
        }


        Map<String,Integer> penalizedDocs = new HashMap<String,Integer>();

        Iterator<Documents> docIter = queryDocument.iterator();

        while(docIter.hasNext()){
            Documents document = docIter.next();

            for (String term : queryTerms) {
                String qterm = getStemOfWord(term.trim().toLowerCase());

                if(document.getTerm().equals(qterm)){
                    if(penalizedDocs.containsKey(document.getDocumentId())){
                        penalizedDocs.put(document.getDocumentId(), penalizedDocs.get(document.getDocumentId())+1);
                    }
                    else{
                        penalizedDocs.put(document.getDocumentId(),1);
                    }
                }
            }
        }

        // Obtain scores for those corresponding docs
        Map<String,Double> laplaceScoreDocList= new HashMap<String, Double>();

		docIter = queryDocument.iterator();

        while(docIter.hasNext()){
            Documents doc = docIter.next();

            double uniLaplaceScore = UnigramLaplace.laplaceSmoothingScore(doc.getTermFrequency(),
                    Long.valueOf(docsLengthValue.get(doc.getDocumentId()).toString()));

            if(laplaceScoreDocList.containsKey(doc.getDocumentId())){
                double uScore = uniLaplaceScore + laplaceScoreDocList.get(doc.getDocumentId());
                laplaceScoreDocList.put(doc.getDocumentId(), uScore);
            }
            else{
                laplaceScoreDocList.put(doc.getDocumentId(), uniLaplaceScore);
            }
        }

        // Add the penalized score
        docIter = queryDocument.iterator();
        while(docIter.hasNext()){
            Documents doc = docIter.next();

            double penalizedScore = UnigramLaplace.penalizedScore(Long.valueOf(docsLengthValue.get(doc.getDocumentId()).toString()),
                    queryTerms.length - penalizedDocs.get(doc.getDocumentId()));

            double lscore = penalizedScore + laplaceScoreDocList.get(doc.getDocumentId());

            laplaceScoreDocList.put(doc.getDocumentId(), lscore);
        }

        // update the scores for all other documents documents
        Iterator<Map.Entry<Object, Object>> lengthIter = docsLengthValue.entrySet().iterator();

        while(lengthIter.hasNext()){

            Map.Entry<Object, Object> entry = lengthIter.next();

            if(laplaceScoreDocList.containsKey(entry.getKey().toString().trim())){
                continue;
            }
            else{
                // Calculate score differently for language models
                // calculate the penalized score instead of adding it like VS models
                double penalizedScore = UnigramLaplace.penalizedScore(Long.valueOf(entry.getValue().toString()),
                        queryTerms.length);
                laplaceScoreDocList.put(entry.getKey().toString().trim(), penalizedScore);
            }
        }

        Iterator<Map.Entry<String,Double>> logIter = laplaceScoreDocList.entrySet().iterator();

        while(logIter.hasNext()){
            Map.Entry<String,Double> entry = logIter.next();
            entry.setValue(Math.log10(entry.getValue()));
        }


        Map<String,Double> sorted = Sorting.sortByComparator(laplaceScoreDocList);

        try {
            OutputFile.outputResultsToFile(sorted, queryTerms[0], "laplace_results.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void generateJelinekScore(String[] queryTerms) {

        Map<String,Double> docScores = new HashMap<String,Double>();

        List<Documents> tempQueryTermList = null;

        for(int i = 1; i < queryTerms.length; i++){

            String term = getStemOfWord(queryTerms[i].trim().toLowerCase());

            if(queryTermList.containsKey(term)){
//                tempQueryTermList = null;
                tempQueryTermList = new ArrayList<Documents>(queryTermList.get(term));

                //compute total frequency of this term;
                long totalTermCount = 0;
                Iterator<Documents> countDocIter = tempQueryTermList.iterator();

                while(countDocIter.hasNext()){
                    totalTermCount = totalTermCount + countDocIter.next().getTermFrequency();
                }

                if(totalTermCount == 0){
                    continue;
                }

                final List<String> existingDocIds = new ArrayList<String>();
                Iterator<Documents> docIter = tempQueryTermList.iterator();
                while(docIter.hasNext()){
                    existingDocIds.add(docIter.next().getDocumentId());
                }

                Iterator<String>  docIdsIter = docIds.iterator();
                while(docIdsIter.hasNext()){
                    String docId = docIdsIter.next();

                    if(existingDocIds.contains(docId)){
                        continue;
                    }
                    else{

                        if(Long.valueOf(docsLengthValue.get(docId).toString()) == 0){
                            continue;
                        }

                        Documents document = new Documents();
                        document.setDocumentId(docId);
                        document.setTerm(term);
                        tempQueryTermList.add(document);
                    }
                }

                Iterator<Documents> docsIter = tempQueryTermList.iterator();
                while(docsIter.hasNext()){
                    Documents doc = docsIter.next();
                    double jScore = UnigramJelinekMercer.score(doc,
                            Long.valueOf(docsLengthValue.get(doc.getDocumentId()).toString()), totalTermCount);

                    if(docScores.containsKey(doc.getDocumentId())){
                        double score = jScore+ docScores.get(doc.getDocumentId());
                        docScores.put(doc.getDocumentId(), score);
                    }
                    else{
                        docScores.put(doc.getDocumentId(), jScore);
                    }
                }
            }
        }

        Map<String,Double> sorted = Sorting.sortByComparator(docScores);


        try {
            OutputFile.outputResultsToFile(sorted,queryTerms[0], "jelinekMercer_results.txt");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Loading the parsed properties into data structures
    public void readDocProps() {

        // map of document ID's and their length
        try {
            docsLengthValue.load(ScoreCalculator.class.getClassLoader().getResourceAsStream("docLength.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            docProperties.load(ScoreCalculator.class.getClassLoader().getResourceAsStream("docIds"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // adding the document ID's into the list

        Iterator<Map.Entry<Object, Object>> propIter = docProperties.entrySet().iterator();
        while(propIter.hasNext()){
            docIds.add(propIter.next().getKey().toString());
        }
    }
}
