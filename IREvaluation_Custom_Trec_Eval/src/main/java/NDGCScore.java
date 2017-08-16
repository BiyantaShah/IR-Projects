import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Biyanta on 16/07/17.
 */
public class NDGCScore {

    public static double ndgcScore(List<Doc> documents, int cutOff, String queryNo) {

        if (documents.size() < cutOff)
            return 0.0;

        List<Doc> sortedDocuments = new ArrayList<Doc>();
        List<Doc> unsortedDocuments = new ArrayList<Doc>();

        for (int i = 0; i < cutOff; i++) {
            unsortedDocuments.add(documents.get(i));
        }

        for (int i = 0; i < documents.size(); i++) {
            sortedDocuments.add(documents.get(i));
        }

        double unsortedDCGScore = dcgScore(unsortedDocuments);

        if (unsortedDCGScore == 0.0)
            return 0.0;

        Collections.sort(sortedDocuments, new Comparator<Doc>() {

            @Override
            public int compare(Doc o1, Doc o2) {
                return o2.getRelevance()-o1.getRelevance();
            }
        });

        double sortedDCGScore = dcgScore(sortedDocuments);

        return unsortedDCGScore/sortedDCGScore;
    }


    private static double dcgScore(List<Doc> documents) {

        double dcgScore = 0.0;

        if (documents.isEmpty())
            return dcgScore;

        dcgScore = documents.get(0).getRelevance();

        for(int i = 1; i < documents.size();i++) {
            double temp = Math.log(i+1)/Math.log(2.0);
            dcgScore = dcgScore + (documents.get(i).getRelevance()/temp);
        }

        return dcgScore;
    }
}


