import java.io.*;
import java.util.*;

/**
 * Created by Biyanta on 20/07/17.
 */
public class LinearRegression {

    private Set<String> training = new HashSet<String>();
    private Set<String> testing = new HashSet<String>();

    // coefficients
    private double okapiCoefficient = 0.0,
            tfidfCoefficient = 0.0,
            bm25Coefficient = 0.0,
            jelinekCoefficient = 0.0,
            laplaceCoefficient = 0.0;

    // min score for each query
    private Map<String, Double> okapiMinScore = new HashMap<String, Double>();
    private Map<String, Double> tfidfMinScore = new HashMap<String, Double>();
    private Map<String, Double> bm25MinScore = new HashMap<String, Double>();
    private Map<String, Double> jelinekMinScore = new HashMap<String, Double>();
    private Map<String, Double> laplaceMinScore = new HashMap<String, Double>();

    // load score file into map
    private Map<String, Map<String, Double>> okapiScore = new HashMap<String, Map<String, Double>>();
    private Map<String, Map<String, Double>> tfidfScore = new HashMap<String, Map<String, Double>>();
    private Map<String, Map<String, Double>> bm25Score = new HashMap<String, Map<String, Double>>();
    private Map<String, Map<String, Double>> jelinekScore = new HashMap<String, Map<String, Double>>();
    private Map<String, Map<String, Double>> laplaceScore = new HashMap<String, Map<String, Double>>();



    public void loadDataForFeatures() throws IOException {

        loadTrainingTestingQueries();

        loadFeatureData(okapiScore, okapiMinScore, "Scores/okapiTF_results.txt");
//        System.out.println("Okapi");
//        for (String key : okapiScore.keySet()) {
//            System.out.println(key + " " + okapiScore.get(key).size());
//        }
        loadFeatureData(tfidfScore, tfidfMinScore, "Scores/okapiTFIDF_results.txt");
        loadFeatureData(bm25Score, bm25MinScore, "Scores/okapiBM25_results.txt");
        loadFeatureData(jelinekScore, jelinekMinScore, "Scores/jelinekMercer_results.txt");
        loadFeatureData(laplaceScore, laplaceMinScore, "Scores/laplace_results.txt");
    }

    private void loadFeatureData(Map<String, Map<String, Double>> scoreMap,
                                 Map<String, Double> minScoreMap,
                                 String fileName) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line = new String();

        while ((line = reader.readLine())!= null) {
            String[] feature = line.split(" ");

            if (line.trim().length() == 0)
                continue;

            if (scoreMap.containsKey(feature[0].trim())) { // contains the query ID
                scoreMap.get(feature[0].trim()).put(feature[2].trim(), Double.valueOf(feature[4].trim()));
            }
            else  {
                Map <String, Double> tempFeatureMap = new HashMap<String, Double>();
                tempFeatureMap.put(feature[2].trim(), Double.valueOf(feature[4].trim()));
                scoreMap.put(feature[0].trim(), tempFeatureMap);
            }

            if (minScoreMap.containsKey(feature[0].trim())) {
                double minScore = minScoreMap.get(feature[0].trim());

                if (Double.valueOf(feature[4].trim()) < minScore) {
                    minScoreMap.put(feature[0].trim(), Double.valueOf(feature[4].trim()));
                }
            }
            else {
                minScoreMap.put(feature[0].trim(), Double.valueOf(feature[4].trim()));
            }
        }

        reader.close();
    }

    private void loadTrainingTestingQueries() {

        try {
            BufferedReader reader = new BufferedReader(new FileReader("TrainTest.txt"));
            String line = new String();

            while ((line = reader.readLine()) != null) {

                String [] data = line.split("=");
                if (data[0].equals("Training")) {
                    for (String queryID : data[1].split(",")) {
                        training.add(queryID.trim());
                    }
                }
                else {
                    for (String queryID : data[1].split(",")) {
                        testing.add(queryID.trim());
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buildMatrix() throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader("qrels.adhoc.51-100.AP89.txt"));
        BufferedWriter writer = new BufferedWriter(new FileWriter("Results/Matrix.txt"));

        String line = new String();

        while ((line = reader.readLine()) != null) {

            String[] qrelData = line.split(" ");

            if (training.contains(qrelData[0].trim())) {
                writeToMatrix(writer, qrelData[0], qrelData[2], qrelData[3]);
            }
        }
        reader.close();

        writer.flush();
        writer.close();

    }

    private void writeToMatrix(BufferedWriter writer,
                               String queryID,
                               String docID,
                               String relevance) throws IOException {


        Double okapi = getScore(okapiScore, okapiMinScore, docID, queryID);
        Double tfidf = getScore(tfidfScore, tfidfMinScore, docID, queryID);
        Double bm25 = getScore(bm25Score, bm25MinScore, docID, queryID);
        Double jelinekMercer = getScore(jelinekScore, jelinekMinScore, docID, queryID);
        Double laplace = getScore(laplaceScore, laplaceMinScore, docID, queryID);

        writer.write(relevance + " 1:"+ okapi + " 2:" + tfidf + " 3:"+ bm25
        + " 4:"+ jelinekMercer + " 5:" + laplace + "\n");

    }

    private Double getScore(Map<String, Map<String, Double>> scoreMap,
                            Map<String, Double> minScoreMap,
                            String docID, String queryID) {

        double score = 0.0;

        try {
            score = scoreMap.get(queryID).get(docID);
        }
        catch (Exception e) {
            score = minScoreMap.get(queryID);
        }

        return score;
    }

    public void testQueries() throws IOException {

        BufferedWriter writer = new BufferedWriter(new FileWriter("Results/TestQueries.txt"));

        for (String queryID: testing) {
            Set<String> docIDs = new HashSet<String>();
            docIDs.addAll(okapiScore.get(queryID).keySet());
            docIDs.addAll(tfidfScore.get(queryID).keySet());
            docIDs.addAll(bm25Score.get(queryID).keySet());
            docIDs.addAll(jelinekScore.get(queryID).keySet());
            docIDs.addAll(laplaceScore.get(queryID).keySet());

            List<Score> scoreList = new ArrayList<Score>();

            for (String docID: docIDs) {

                double score = testQueryResults(queryID, docID);
                Score scoreVal = new Score (docID, queryID, score);
                scoreList.add(scoreVal);
            }

            Collections.sort(scoreList);
            int rank = 1;

            for (Score score: scoreList) {
                writer.write(score.getQueryID()+" Q0 " + score.getDocID() + " "+ rank + " "
                + score.getScore() + " IR "+ "\n");
                rank ++;
            }
        }

        writer.flush();
        writer.close();

    }

    private double testQueryResults(String queryID, String docID) {
        Double okapi = getScore(okapiScore, okapiMinScore, docID, queryID);
        Double tfidf = getScore(tfidfScore, tfidfMinScore, docID, queryID);
        Double bm25 = getScore(bm25Score, bm25MinScore, docID, queryID);
        Double jelinekMercer = getScore(jelinekScore, jelinekMinScore, docID, queryID);
        Double laplace = getScore(laplaceScore, laplaceMinScore, docID, queryID);

        return calculateRegressionScore(okapi, tfidf, bm25, jelinekMercer, laplace);
    }

    private double calculateRegressionScore(Double okapi,
                                            Double tfidf,
                                            Double bm25,
                                            Double jelinekMercer,
                                            Double laplace) {

        double regressionScore = okapi * okapiCoefficient +
                tfidf * tfidfCoefficient +
                bm25 * bm25Coefficient +
                jelinekMercer * jelinekCoefficient +
                laplace * laplaceCoefficient ;

        return regressionScore;
    }

    public void testOnTrainedSet() throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader("qrels.adhoc.51-100.AP89.txt"));
        BufferedWriter writer = new BufferedWriter(new FileWriter("Results/TrainedSet.txt"));

        String line = new String();

        List<Score> scoreList = new ArrayList<Score>();
        while ((line = reader.readLine()) != null) {

            String [] data = line.split(" ");

            if (training.contains(data[0].trim())) {

                double score = testQueryResults(data[0], data[2]);
                Score scoreVal = new Score (data[2], data[0], score);
                scoreList.add(scoreVal);
            }

        }
        Collections.sort(scoreList);
        int rank = 1;

        for (Score score : scoreList) {

            writer.write(score.getQueryID() + " Q0 "+ score.getDocID() + " " + rank + " " +
                    score.getScore()
                    + " IR "+ "\n");
            rank ++;
        }
        reader.close();

        writer.flush();
        writer.close();
    }

    public void readModel() throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader("Results/Model.txt"));

        int lines = 1;
        while (lines <= 6 ) {
            reader.readLine();
            ++lines;
        }

        okapiCoefficient = Double.valueOf(reader.readLine().trim());
        tfidfCoefficient = Double.valueOf(reader.readLine().trim());
        bm25Coefficient = Double.valueOf(reader.readLine().trim());
        jelinekCoefficient = Double.valueOf(reader.readLine().trim());
        laplaceCoefficient = Double.valueOf(reader.readLine().trim());

        System.out.println(okapiCoefficient + " " + tfidfCoefficient +" " + bm25Coefficient +" "+ jelinekCoefficient
                +" "+laplaceCoefficient );
        reader.close();
    }
}
