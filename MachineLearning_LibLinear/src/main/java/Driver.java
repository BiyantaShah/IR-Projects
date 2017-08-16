import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Biyanta on 20/07/17.
 */
public class Driver {

    public static void main (String [] args) throws IOException {


        LinearRegression linearRegression = new LinearRegression();

        linearRegression.loadDataForFeatures();
//        linearRegression.buildMatrix();
        linearRegression.readModel();
        linearRegression.testQueries();
        linearRegression.testOnTrainedSet();
    }

}
