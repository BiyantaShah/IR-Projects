import java.util.*;

/**
 * Created by Biyanta on 03/07/17.
 */
public class Sorting {

    public static Map<String,Double> sort(Map<String, Double> pageRankScore) {

        List<Map.Entry<String, Double>> list =
                new LinkedList<Map.Entry<String, Double>>(pageRankScore.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<String, Double> sortedPageRank = new LinkedHashMap<String, Double>();

        for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext();) {
            Map.Entry<String, Double> entry = it.next();
            sortedPageRank.put(entry.getKey(), entry.getValue());
        }
        return sortedPageRank;
    }
}
