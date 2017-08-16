import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Biyanta on 12/08/17.
 */
public class TFMapping {

    public static void main (String[] args) {

        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile =
                    new RandomAccessFile(new File("NonStemmed/FinalInvertedFile.txt"), "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Map<String, Long> termOffset = new HashMap<String, Long>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File("NonStemmed/FinalCatalogFile.txt")));

            String line = null;
            while((line = reader.readLine())!= null){

                String[] offset = line.split(" ");
                termOffset.put(offset[0], Long.valueOf(offset[1]));
            }
            reader.close();

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        BufferedWriter writer = null;
        try {
             writer = new BufferedWriter(new FileWriter("Mapping/TFMapping.txt"));

             for (String term : termOffset.keySet()) {

                 Long offset = termOffset.get(term);
                 randomAccessFile.seek(offset);

                 StringBuilder sb = new StringBuilder(randomAccessFile.readLine());
                 String[] documents = sb.toString().split("=")[1].split(";");

                 int ttf = 0;
                 for (String document : documents) {

                     String tfData = document.split("#")[1].split("~")[0];
                     ttf += Integer.valueOf(tfData);

                 }
                 writer.write(term + " "+ttf+"\n");

             }
             writer.flush();
             writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
