import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Biyanta on 03/07/17.
 */
public class ComputePageRank {

    static int i = 0;
    public static void computePageRank(Map<String, Set<String>> inlinksMap,
                                       Map<String, Long> outlinksMap,
                                       Map<String, Double> pageRankScore,
                                       Set<String> sinkNodes,
                                       String fileName) {

        System.out.println("started");

        long totalNodes = pageRankScore.size();
        double lambda = 0.85;

        System.out.println("sink node size "+ sinkNodes.size());
        boolean isConverged = false;

        while (!isConverged) {
            i++;
            double delta = 0.0;

            for (String sinkNode : sinkNodes) {
                delta += pageRankScore.get(sinkNode);
            }

            Map<String, Double> intermediatePageRank = new HashMap<String, Double>();
            Set<String> pages = pageRankScore.keySet();

            for (String docId : pages) {

                double newScore = (1.0 - lambda)/ totalNodes;
                newScore += delta * lambda / totalNodes;

                Set<String> inlinks = inlinksMap.get(docId);
                if (inlinks != null && !inlinks.isEmpty()) {

                    for (String id : inlinks) {
                        if (outlinksMap.containsKey(id) &&
                                pageRankScore.containsKey(id)) {
                            newScore += lambda * pageRankScore.get(id) / (double) outlinksMap.get(id);
                        }
                    }
                }
                intermediatePageRank.put(docId, newScore);
            }

            isConverged = hasConverged(intermediatePageRank, pageRankScore);

            // update page rank map
            Iterator<Map.Entry<String, Double>> iter = pageRankScore.entrySet().iterator();
            while(iter.hasNext()){
                Map.Entry<String,Double> entry = iter.next();
                entry.setValue(intermediatePageRank.get(entry.getKey()));
            }

        }

        System.out.println("converged after "+ i);
        // sort the scores
//        Map<String, Double> sortedPages = Sorting.sort(pageRankScore);

//        printLinkScoreInlinkCount(pageRankScore, inlinksMap, "WT25-B13-371");

//        try {
//            OutputFile.writeOutput(sortedPages, fileName, inlinksMap);
//            OutputFile.writeToOutputFile(sortedPages, fileName);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private static void printLinkScoreInlinkCount(Map<String, Double> pageRankScore,
                                                  Map<String, Set<String>> inlinksMap,
                                                  String link) {

        if (pageRankScore.containsKey(link) && inlinksMap.containsKey(link)) {

            System.out.println(link +" "+ pageRankScore.get(link)+" "+ inlinksMap.get(link).size());
        }
    }

    private static boolean hasConverged(Map<String, Double> intermediatePageRank,
                                        Map<String, Double> pageRankScore) {

        Double convergence = 0.00001;
        Double delta = 0.0;

        for (String doc : intermediatePageRank.keySet()) {
             delta += Math.abs(intermediatePageRank.get(doc) - pageRankScore.get(doc));
        }

        if (delta > convergence)
            return false;
        else
            return true;
    }

//    private static boolean scoresConverge(double oldConvergence, double newConvergence) {
//
//        boolean converges = ((int)Math.floor(oldConvergence) % 10 == (int)Math.floor(newConvergence) % 10)
//                && ((int)oldConvergence == (int)newConvergence) ;
//
//        return converges;
//    }
//
//    private static double computeConvergence(Map<String, Double> pageRankScore) {
//        double convergenceScore = 0.0;
//
//        for (Double value : pageRankScore.values()) {
//            convergenceScore += value * Math.log(value)/ Math.log(2);
//        }
//
//        return Math.pow(2, -1 * convergenceScore);
//    }
}
