import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Biyanta on 15/07/17.
 */
public class GenerateBoolandNonBoolFiles {

    public static void main (String [] args) throws IOException {

        generateBoolNonBool("Input/MergedQrelFile.txt");
    }

    private static void generateBoolNonBool(String fileName) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(fileName));

        BufferedWriter writerBool = new BufferedWriter(new FileWriter("BooleanQrel.txt"));
        BufferedWriter writerNonBool = new BufferedWriter(new FileWriter("NonBooleanQrel.txt"));

        String line = new String();

        while ((line = reader.readLine()) != null) {

            String [] values = line.split(" ");
            String queryID = values[0].trim();
            String link = values[1].trim();
            int value1 = Integer.parseInt(values[2].trim());
            int value2 = Integer.parseInt(values[3].trim());
            int value3 = Integer.parseInt(values[4].trim());

            double average = (value1 + value2 + value3) / 6.0;

            if(average < 0.5){
                writerBool.write(queryID + " " + 0 + " " + link + " " + 0 + "\n");
            }else{
                writerBool.write(queryID + " " + 0 + " " + link + " " + 1 + "\n");
            }

            writerNonBool.write(queryID + " " + 0 + " " +link + " " + (value1 + value2 + value3)/3.0 + "\n");
        }
        reader.close();

        writerBool.flush();
        writerBool.close();

        writerNonBool.flush();
        writerNonBool.close();
    }

}
