import org.elasticsearch.index.mapper.SourceToParse;

import java.io.*;
import java.util.*;

/**
 * Created by Biyanta on 16/07/17.
 */
public class TrecEval {

    private static int relevance = 1;

    private Map<String, Map<String,Integer>> qrelMap = new HashMap<String, Map<String,Integer>>();
    private Map<String, List<Doc>> resultsMap = new HashMap<String, List<Doc>>();
    private Map<String, Score> fScoreMap = new HashMap<String, Score>();
    private Map<String, Score> ndgcScoreMap = new HashMap<String, Score>();

    private boolean isTrecEval;

    private List<Double> avgPrecisionValues = new ArrayList<Double>();
    private List<Double> rPrecisionValues = new ArrayList<Double>();


    public TrecEval(boolean isTrecEval){
        this.isTrecEval = isTrecEval;
    }

    public void loadData(final String qrelFile, String resultsFile) throws IOException,ArrayIndexOutOfBoundsException{
        loadQrelMap(qrelFile);
        loadResultsMap(resultsFile);
    }

    private void loadQrelMap(String qrelFile) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(qrelFile));

        String line = new String();

        while ((line = reader.readLine()) != null) {
            String [] qrelData = line.split(" ");

            if (qrelMap.containsKey(qrelData[0].trim())) {

                if(isTrecEval){
                    qrelMap.get(qrelData[0]).put(qrelData[2].trim(), Integer.parseInt(qrelData[3].trim()));

                }else {
                    qrelMap.get(qrelData[0]).put(qrelData[2].trim(), (int)Math.round(Double.valueOf(qrelData[3])));
                }
            }
            else {

                Map<String,Integer> tempMap = new HashMap<String, Integer>();

                if(isTrecEval) {
                    tempMap.put(qrelData[2], Integer.parseInt(qrelData[3]));
                }
                else {
                    tempMap.put(qrelData[2], (int)Math.round(Double.valueOf(qrelData[3])));
                }
                qrelMap.put(qrelData[0], tempMap);
            }
        }

        reader.close();
        System.out.println("qrels loaded");
    }

    private void loadResultsMap(String resultFile) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(resultFile));

        String line = new String();

        while ((line = reader.readLine()) != null) {

            String[] resultsData = line.split(" ");
            Doc doc = new Doc();

            doc.setDocId(resultsData[1].trim());
            doc.setRank(Integer.parseInt(resultsData[2].trim()));

            if(qrelMap.containsKey(resultsData[0].trim())
                    && qrelMap.get(resultsData[0].trim()).containsKey(resultsData[1].trim())) {
                doc.setRelevance(qrelMap.get(resultsData[0].trim()).get(resultsData[1].trim()));
            }
            else {
                doc.setRelevance(0);
            }

            if(resultsMap.containsKey(resultsData[0].trim())) {
                resultsMap.get(resultsData[0].trim()).add(doc);
            }
            else {
                List<Doc> tempList = new ArrayList<Doc>();
                tempList.add(doc);
                resultsMap.put(resultsData[0].trim(), tempList);
            }
        }

        reader.close();
        System.out.println("result file loaded");


        for(String query: resultsMap.keySet()){
            List<Doc> tempList = resultsMap.get(query);
            Collections.sort(tempList);

//            System.out.println(query);
            List<Doc> temp = new ArrayList<Doc>();
            int i, x = 0;

            if (isTrecEval)
                x = 1000;
            else
                x = 200;

            for(i = 0; i < x; i++){
                temp.add(tempList.get(i));
            }
//            System.out.println("value of i "+ i);

            resultsMap.get(query).clear();
            resultsMap.get(query).addAll(temp);

        }

    }

    public void generatePrecisionRecall() throws IOException {

        for (String query : resultsMap.keySet()) {
            int totalRelevantDocs = getTotalOfRelevantDocs(qrelMap.get(query));
            populatePrecAndRecallValues(resultsMap.get(query),totalRelevantDocs);
        }
//        printPrecisionRecallValues();
    }

    private void populatePrecAndRecallValues(List<Doc> docs, int totalRelevantDocs) {

        int relevanceCount = 0;
        for (int i = 0; i < docs.size(); i++) {
            Doc tempDoc = docs.get(i);
            if(tempDoc.getRelevance() == relevance){
                ++relevanceCount;
            }
            tempDoc.setPrecision((double)relevanceCount/(i+1));
            tempDoc.setRecall((double)relevanceCount/totalRelevantDocs);
        }
    }

    private int getTotalOfRelevantDocs(Map<String, Integer> map) {

        int totalRelevantCount = 0;

        for (int relevanceValue : map.values()) {
            totalRelevantCount = totalRelevantCount + relevanceValue;
        }
        return totalRelevantCount;
    }

    public void printPrecisionRecallValues() throws IOException {

        int count = 1;

        for (String query : resultsMap.keySet()) {
            BufferedWriter bw = new BufferedWriter(new FileWriter("precisionRecallValues"+ count++ +".csv"));
            List<Doc> docs = resultsMap.get(query);
            System.out.println("QUERY: "+ query);
            for (Doc doc : docs) {
                bw.write(doc.getPrecision()+","+doc.getRecall()+ "\n");
            }
            bw.flush();
            bw.close();
        }
    }

    public void generateNDGCandFScore() throws IOException {
        for (String query : resultsMap.keySet()) {

            List<Doc> docs = resultsMap.get(query);
            int totalRelevantDocs = getTotalOfRelevantDocs(qrelMap.get(query));

            Score fScore = ComputeFScore(query, docs);
            Score ndgcScore = ComputeNDGCScore(query, docs);
            double avgScore =  getAveragePrecision(query, docs, totalRelevantDocs);
            double rPrecisionScore = getRPrecision(query, docs, totalRelevantDocs);

            avgPrecisionValues.add(avgScore);
            rPrecisionValues.add(rPrecisionScore);

            System.out.println("Query ID "+ query);
            System.out.println("Retrieved Documents "+ docs.size());
            System.out.println("Relevant Documents "+ totalRelevantDocs);
            System.out.println("Retrieved Relevant Docs "+ getRelevantDocuments(docs));
            System.out.println("          Rank-5     Rank-10    Rank-20    Rank-50    Rank-100");

            System.out.println("Precision: "+ docs.get(4).getPrecision() + "  "+ docs.get(9).getPrecision()
                    + "  "+ docs.get(19).getPrecision()+ "  "+ docs.get(49).getPrecision() + "  "+ docs.get(99).getPrecision());

            System.out.println("Recall: "+ docs.get(4).getRecall() + "  "+ docs.get(9).getRecall()
                    + "  "+ docs.get(19).getRecall()+ "  "+ docs.get(49).getRecall() + "  "+ docs.get(99).getRecall());

            System.out.println("F1 Score: " + fScore.getFiveCut() + "  " +fScore.getTenCut() + "  "+ fScore.getTwentyCut()
                    + "  " + fScore.getFiftyCut() + "  "
            + fScore.getHundredCut());

            System.out.println("NDGC Score: "+ ndgcScore.getFiveCut() + "  " + ndgcScore.getTenCut() + "  " + ndgcScore.getTwentyCut()
                    + "  " + ndgcScore.getFiftyCut() + "  "+ ndgcScore.getHundredCut());

            System.out.println("Average Precision " + avgScore);
            System.out.println("R Precision "+ rPrecisionScore);

        }

        double avgScore = 0.0;
        for (Double tempAvg : avgPrecisionValues) {
            avgScore = avgScore + tempAvg;
        }
        //for overall z scores of all the queries
        double rScore = 0.0;
        for (Double temprScore : rPrecisionValues) {
            rScore = rScore + temprScore;
        }

        double _5Precision=0.0,_10Precision=0.0, _20Precision = 0, _50Precision = 0, _100Precision= 0;
        for (Map.Entry<String, Score> entry : fScoreMap.entrySet()) {
            _5Precision = _5Precision + entry.getValue().getFiveCut();
            _10Precision = _10Precision + entry.getValue().getTenCut();
            _20Precision = _20Precision + entry.getValue().getTwentyCut();
            _50Precision = _50Precision + entry.getValue().getFiftyCut();
            _100Precision = _100Precision + entry.getValue().getHundredCut();

        }

        double _5PrecisionNDCG = 0.0, _10PrecisionNDCG = 0.0, _20PrecisionNDCG = 0.0, _50PrecisionNDCG = 0.0,
                _100PrecisionNDCG=0.0, _1000PrecisionNDGC = 0.0;

        for (Map.Entry<String, Score> entry : ndgcScoreMap.entrySet()) {
            _5PrecisionNDCG = _5PrecisionNDCG + entry.getValue().getFiveCut();
            _10PrecisionNDCG = _10PrecisionNDCG + entry.getValue().getTenCut();
            _20PrecisionNDCG = _20PrecisionNDCG + entry.getValue().getTwentyCut();
            _50PrecisionNDCG = _50PrecisionNDCG + entry.getValue().getFiftyCut();
            _100PrecisionNDCG = _100PrecisionNDCG + entry.getValue().getHundredCut();
            _1000PrecisionNDGC = _1000PrecisionNDGC + entry.getValue().getThousandCut();
        }


        double _5PrecisionPrec=0.0, _10PrecisionPrec=0.0 , _20PrecisionPrec = 0.0, _50PrecisionPrec = 0.0,
                _100PrecisionPrec=0.0;

        for (Map.Entry<String, List<Doc>> entry : resultsMap.entrySet()) {

            _5PrecisionPrec =_5PrecisionPrec + entry.getValue().get(4).getPrecision();
            _10PrecisionPrec =_10PrecisionPrec + entry.getValue().get(9).getPrecision();
            _20PrecisionPrec =_20PrecisionPrec + entry.getValue().get(19).getPrecision();
            _50PrecisionPrec =_50PrecisionPrec + entry.getValue().get(49).getPrecision();
            _100PrecisionPrec =_100PrecisionPrec + entry.getValue().get(99).getPrecision();

        }

        //scores at 5,10,20,50,100,200,500,1000 precision for Recall
        double _5PrecisionRecall=0.0,_10PrecisionRecall=0.0, _20PrecisionRecall = 0.0, _50PrecisionRecall = 0.0,
                _100PrecisionRecall=0.0;

        for (Map.Entry<String, List<Doc>> entry : resultsMap.entrySet()) {

            _5PrecisionRecall =_5PrecisionRecall + entry.getValue().get(4).getRecall();
            _10PrecisionRecall =_10PrecisionRecall + entry.getValue().get(9).getRecall();
            _20PrecisionRecall =_20PrecisionRecall + entry.getValue().get(19).getRecall();
            _50PrecisionRecall =_50PrecisionRecall + entry.getValue().get(49).getRecall();
            _100PrecisionRecall =_100PrecisionRecall + entry.getValue().get(99).getRecall();

        }

        //print for all the values

        System.out.println("           Rank-5             Rank-10            Rank-100            Rank-200");
        System.out.println("Overall Precision:  "+ _5PrecisionPrec/resultsMap.entrySet().size()
                +"  "+ _10PrecisionPrec/resultsMap.entrySet().size()
                +"  "+ _20PrecisionPrec/resultsMap.entrySet().size()
                +"  "+ _50PrecisionPrec/resultsMap.entrySet().size()
                +"  "+_100PrecisionPrec/resultsMap.entrySet().size());

        System.out.println("Overall Recall:     "+ _5PrecisionRecall/resultsMap.entrySet().size()
                +"  "+ _10PrecisionRecall/resultsMap.entrySet().size()
                +"  "+ _20PrecisionRecall/resultsMap.entrySet().size()
                +"  "+ _50PrecisionRecall/resultsMap.entrySet().size()
                +"  "+_100PrecisionRecall/resultsMap.entrySet().size());

        System.out.println("Overall F-1:        "+ _5Precision/fScoreMap.size()
                +"  "+ _10Precision/fScoreMap.size()
                +"  "+ _20Precision/fScoreMap.size()
                +"  "+ _50Precision/fScoreMap.size()
                +"  "+_100Precision/fScoreMap.size());

        System.out.println("Overall NDGC Score:       "+ _5PrecisionNDCG/ndgcScoreMap.size()
                +"  "+ _10PrecisionNDCG/ndgcScoreMap.size()
                +"  "+ _20PrecisionNDCG/ndgcScoreMap.size()
                +"  "+ _50PrecisionNDCG/ndgcScoreMap.size()
                +"  "+_100PrecisionNDCG/ndgcScoreMap.size()
        +"  "+ _1000PrecisionNDGC/ndgcScoreMap.size());

        System.out.println("Overall Average precision: " + avgScore/avgPrecisionValues.size());

        System.out.println("Overall R-Precision  : " + rScore/rPrecisionValues.size());

    }

    private String getRelevantDocuments(List<Doc> docs) {

        int count = 0;
        for (Doc doc : docs) {
            if(doc.getRelevance() == relevance){
                ++count;
            }
        }
        return String.valueOf(count);
    }

    private double getRPrecision(String query, List<Doc> docs, int totalRelevantDocs) {

        if(qrelMap.containsKey(query)){
            return docs.get(totalRelevantDocs - 1).getPrecision();
        }
        return 0.0;
    }

    private double getAveragePrecision(String query, List<Doc> docs, int totalRelevantDocs) {

        if(qrelMap.containsKey(query)) {

            double precision = 0.0;
            for(int i = 0 ; i < docs.size(); i++) {
                if(docs.get(i).getRelevance() == relevance)
                    precision = precision + docs.get(i).getPrecision();
            }
            return (precision/totalRelevantDocs);
        }
        return 0.0;
    }

    private Score ComputeNDGCScore(String query, List<Doc> docs) {

        Score score = new Score();
        score.setFiveCut(NDGCScore.ndgcScore(resultsMap.get(query), 5, query));
        score.setTenCut(NDGCScore.ndgcScore(resultsMap.get(query), 10, query));
        score.setTwentyCut(NDGCScore.ndgcScore(resultsMap.get(query), 20, query));
        score.setFiftyCut(NDGCScore.ndgcScore(resultsMap.get(query), 50, query));
        score.setHundredCut(NDGCScore.ndgcScore(resultsMap.get(query), 100, query));
        score.setThousandCut(NDGCScore.ndgcScore(resultsMap.get(query), 1000, query));
        ndgcScoreMap.put(query, score);
        return score;
    }

    private Score ComputeFScore(String query, List<Doc> docs) {

        Score score = new Score();
        score.setFiveCut(F1Score.f1Score(docs, 5));
        score.setTenCut(F1Score.f1Score(docs, 10));
        score.setTwentyCut(F1Score.f1Score(docs, 20));
        score.setFiftyCut(F1Score.f1Score(docs, 50));
        score.setHundredCut(F1Score.f1Score(docs, 100));
        fScoreMap.put(query, score);
        return score;
    }
}
