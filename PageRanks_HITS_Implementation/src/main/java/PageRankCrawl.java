
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Biyanta on 02/07/17.
 */
public class PageRankCrawl {

    private static Map<String, Double> PageRankScore = new HashMap<String, Double>(50000);

    private static Map<String, Set<String>> inlinksMap = new HashMap<String, Set<String>>(50000);

    private static Map<String, Set<String>> outlinksMap = new HashMap<String, Set<String>>(50000);

    private static Map<String, Long> outlinksSizeMap = new HashMap<String, Long>(50000);

    private static Set<String> sinkNodes = new HashSet<String>();

    public static void main (String [] args) throws IOException {

        loadDataFromLinkGraph();

        String link  = "http://en.wikipedia.org/wiki/Hinduism";

        System.out.println("data loaded");
//        printInlink(link);
        calculatePageRank();
        
    }

    private static void printInlink(String link) {

        if (inlinksMap.containsKey(link)) {
            Set<String> inlinks = inlinksMap.get(link);

            for (String x : inlinks) {
                System.out.println(x);
            }
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
                PageRankScore, sinkNodes, "Output/CrawledPageRankNew.txt");
    }

    private static void loadDataFromLinkGraph() throws IOException {

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("mergedInlinks.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String line = new String();

        while ((line  = reader.readLine()) != null) {

            String [] inlinks = line.split("=");

            if (inlinks.length > 1) {

                String [] inlinksID = inlinks[1].split(" ");
                for (int i = 0; i < inlinksID.length; i++) {

                    if (inlinksMap.containsKey(inlinks[0]))  {
                        inlinksMap.get(inlinks[0]).add(inlinksID[i]);
                    }
                    else {
                        Set<String> tempSet = new HashSet<String>();
                        tempSet.add(inlinksID[i]);
                        inlinksMap.put(inlinks[0], tempSet);
                    }

                    PageRankScore.put(inlinksID[i], null);

                    //update outlinks
                    if(outlinksMap.containsKey(inlinksID[i])){
                        outlinksMap.get(inlinksID[i]).add(inlinks[0]);
                    }else{
                        Set<String> set = new HashSet<String>();
                        set.add(inlinks[0]);
                        outlinksMap.put(inlinksID[i], set);
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
