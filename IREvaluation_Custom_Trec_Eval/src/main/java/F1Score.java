import java.util.List;

/**
 * Created by Biyanta on 16/07/17.
 */
public class F1Score {

    public static double f1Score(List<Doc> listDoc, int cutOff) {

        if (listDoc.size() < cutOff)
            return 0.0;

        Doc tempDoc = listDoc.get(cutOff - 1);

        if (tempDoc.getPrecision() != 0.0 && tempDoc.getRecall() != 0.0) {
            return 2.0 * tempDoc.getPrecision() *
                    tempDoc.getRecall()/(tempDoc.getPrecision()
                    + tempDoc.getRecall());
        }
        return 0.0;
    }
}
