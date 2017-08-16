import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Biyanta on 05/07/17.
 */
public class AuthorityHub {

    static int i = 0;
    private boolean isConverged = false;

    // scores and ranks till the scores converge,
    // update scores of base set links
    public void score(Map<String, Set<String>> inlinks,
                      Map<String, Set<String>> outlinks,
                      Set<String> rootSet,
                      Map<String, Double> hubScore,
                      Map<String, Double> authorityScore) {

        Map <String, Double> tempHubScore = new HashMap<String, Double>(hubScore.size());
        Map <String, Double> tempAuthScore = new HashMap<String, Double>(authorityScore.size());


        while (!isConverged) {
            i++;
            tempHubScore.clear();
            tempAuthScore.clear();

            for (String doc : rootSet) {

                double authScores = getScores(inlinks.get(doc), hubScore);
                double hubScores = getScores(outlinks.get(doc), authorityScore);

                tempHubScore.put(doc, hubScores);
                tempAuthScore.put(doc, authScores);
            }

            normalizeScore(tempHubScore);
            normalizeScore(tempAuthScore);

            isConverged = hasConverged(tempHubScore, tempAuthScore, authorityScore, hubScore);

            hubScore.clear();
            hubScore.putAll(tempHubScore);

            authorityScore.clear();
            authorityScore.putAll(tempAuthScore);

        }
        System.out.println("converged "+ i);

//        printHITSstuff(hubScore, authorityScore, inlinks, outlinks,
//                "http://en.wikipedia.org/wiki/Crisis_management");

//        printInlinkOutlink(inlinks, outlinks, rootSet,
//                "http://en.wikipedia.org/wiki/Crisis_management");

//        Map<String, Double> sortedHubScores = Sorting.sort(hubScore);
//        Map<String, Double> sortedAuthScores = Sorting.sort(authorityScore);
//
//
//
//        try {
//            OutputFile.writeToOutputFile(sortedHubScores, "Output/HubScores.txt");
//            OutputFile.writeToOutputFile(sortedAuthScores, "Output/AuthorityScores.txt");
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    private void printInlinkOutlink(Map<String, Set<String>> inlinks,
                                    Map<String, Set<String>> outlinks,
                                    Set<String> rootSet, String link) {

        if (rootSet.contains(link)) {
            if (inlinks.containsKey(link)) {
                System.out.println("Inlinks");
                for (String inlink : inlinks.get(link)) {
                    System.out.println(inlink);
                }
            }
            if (outlinks.containsKey(link)) {
                System.out.println("Outlinks");
                for (String outlink : outlinks.get(link)) {
                    System.out.println(outlink);
                }
            }
        }
    }

    private void printHITSstuff(Map<String, Double> hubScore,
                                Map<String, Double> authorityScore,
                                Map<String, Set<String>> inlinks,
                                Map<String, Set<String>> outlinks,
                                String link) {

        if (hubScore.containsKey(link)) {
            System.out.println("hub");
            System.out.println(link +" "+ hubScore.get(link) +" " + outlinks.get(link).size());
        }
        if (authorityScore.containsKey(link)) {
            System.out.println("auth");
            System.out.println(link +" "+ authorityScore.get(link) + " " + inlinks.get(link).size());
        }
    }

    private boolean hasConverged(Map<String, Double> tempHubScore,
                                 Map<String, Double> tempAuthScore,
                                 Map<String, Double> authorityScore,
                                 Map<String, Double> hubScore) {

        Double convergence = 0.00001;

        for (String doc : authorityScore.keySet()) {

            if(Math.abs(authorityScore.get(doc) - tempAuthScore.get(doc)) > convergence)
                return false;


            if (Math.abs(hubScore.get(doc) - tempHubScore.get(doc)) > convergence)
                return false;
        }
        return true;
    }

    private void normalizeScore(Map<String, Double> scoreMap) {

        double normalize = 0.0;

        for (Double score : scoreMap.values()) {
            normalize += Math.pow(score, 2);
        }
        normalize = Math.sqrt(normalize);

        Iterator<Map.Entry<String, Double>> iter = scoreMap.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<String, Double> entry = iter.next();

            scoreMap.put(entry.getKey(), entry.getValue()/normalize);
        }

    }

    private double getScores(Set<String> links, Map<String, Double> scoreMap) {
        double score = 0.0;
        if (links ==  null || links.isEmpty())
            return 0.0;

        else {
            for (String doc: links) {
                if (scoreMap.containsKey(doc)) {
                    score += scoreMap.get(doc);
                }
            }
            return score;
        }
    }

}
