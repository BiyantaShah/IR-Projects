import java.io.*;

/**
 * Created by Biyanta on 16/07/17.
 */
public class RefactorFile {

    public static void main (String [] args) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader("okapiBM25_results.txt"));

        BufferedWriter writer = new BufferedWriter(new FileWriter("Trec_Eval_Assignment5.txt"));
        
        String line = new String();

        while ((line = reader.readLine()) != null) {

            String [] data = line.split(" ");
            writer.write(data[0] + " " + data [2]+" " + data [3]+ "\n");
        }
        writer.flush();
        writer.close();

    }

}
