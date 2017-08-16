import sun.jvm.hotspot.debugger.Page;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Biyanta on 02/07/17.
 */
public class PageRankOtherGraph {

    private static Map<String, Double> PageRankScore = new HashMap<String, Double>(50000);

    private static Map<String, Set<String>> inlinksMap = new HashMap<String, Set<String>>(50000);

    private static Map<String, Set<String>> outlinksMap = new HashMap<String, Set<String>>(50000);

    private static Map<String, Long> outlinksSizeMap = new HashMap<String, Long>(50000);

    private static Set<String> sinkNodes = new HashSet<String>();

    public static void main (String [] args) throws IOException {

        loadDataFromLinkGraph();

        Set<String> links = new LinkedHashSet<String>();
        links.add("WT21-B37-76");
        links.add("WT21-B37-75");
        links.add("WT25-B39-116");
        links.add("WT23-B21-53");
        links.add("WT24-B26-10");
        links.add("WT24-B40-171");
        links.add("WT23-B39-340");
        links.add("WT23-B37-134");
        links.add("WT08-B18-400");
        links.add("WT13-B06-284");




//        System.out.println("data loaded");
//        for (String link : links) {
//            printInlink(link);
//        }
        calculatePageRank();

    }

    private static void printInlink(String link) {

        if (inlinksMap.containsKey(link)) {
            int inlinks = inlinksMap.get(link).size();
            System.out.println(link + " "+ inlinks);

        }
        else {
            System.out.println("does not contain "+ link);
        }

    }

    private static void calculatePageRank() {

        int totalNodes = PageRankScore.size();

        Iterator<Map.Entry<String, Double>> iter = PageRankScore.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<String, Double> value = iter.next();
            value.setValue(1.0/totalNodes);
        }

        Iterator<Map.Entry<String, Set<String>>> outlinkIter = outlinksMap.entrySet().iterator();

        // get size of outlinks for delta calculation
        while (outlinkIter.hasNext()) {
            Map.Entry<String, Set<String>> value  = outlinkIter.next();
            outlinksSizeMap.put(value.getKey(), Long.valueOf(value.getValue().size()));
        }

        // update dangling nodes
        sinkNodes.addAll(inlinksMap.keySet());
        sinkNodes.removeAll(outlinksSizeMap.keySet());

        System.out.println("base set");
        ComputePageRank.computePageRank(inlinksMap, outlinksSizeMap,
                PageRankScore, sinkNodes, "Output/WT2GPageRank.txt");
    }

    private static void loadDataFromLinkGraph() throws IOException {

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("wt2g_inlinks.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String line = new String();

        while ((line  = reader.readLine()) != null) {

            String [] inlinks = line.split(" ");

            if (inlinks.length > 1) {

                for (int i = 1; i < inlinks.length; i++) {

                    if (inlinksMap.containsKey(inlinks[0]))  {
                        inlinksMap.get(inlinks[0]).add(inlinks[i]);
                    }
                    else {
                        Set<String> tempSet = new HashSet<String>();
                        tempSet.add(inlinks[i]);
                        inlinksMap.put(inlinks[0], tempSet);
                    }

                    PageRankScore.put(inlinks[i], null);

                    //update outlinks
                    if(outlinksMap.containsKey(inlinks[i])){
                        outlinksMap.get(inlinks[i]).add(inlinks[0]);
                    }else{
                        Set<String> set = new HashSet<String>();
                        set.add(inlinks[0]);
                        outlinksMap.put(inlinks[i], set);
                    }
                }
            }
            else  {
                if(!inlinksMap.containsKey(inlinks[0])){
                    inlinksMap.put(inlinks[0], new HashSet<String>());
                }
            }

            PageRankScore.put(inlinks[0], null);
        }

    }
}
