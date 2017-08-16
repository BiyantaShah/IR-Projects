import java.io.IOException;

/**
 * Created by Biyanta on 16/07/17.
 */
public class Driver {

    public static void main(String[] args) {

        boolean isTrecEval = false;

        TrecEval trecEval = new TrecEval(isTrecEval);
        try {
            if (isTrecEval) {
                trecEval.loadData("qrels.adhoc.51-100.AP89.txt", "Trec_Eval_Assignment5.txt");
            }
            else {
                trecEval.loadData("BooleanQrel.txt", "Input/RankedListFile");
            }

            trecEval.generatePrecisionRecall();
            trecEval.generateNDGCandFScore();

        } catch(ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
