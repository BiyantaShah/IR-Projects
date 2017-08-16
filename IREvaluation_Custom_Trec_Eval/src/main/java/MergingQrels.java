import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Biyanta on 15/07/17.
 */
public class MergingQrels {

    public static void main (String [] args) throws IOException {

        BufferedReader readBiyanta = new BufferedReader(new FileReader("Qrels_Biyanta.txt"));
        BufferedReader readPaulomi = new BufferedReader(new FileReader("PMQrel.txt"));
        BufferedReader readPriyanka = new BufferedReader(new FileReader("ps_qrel.txt"));

        Map<String, Integer> biya = new LinkedHashMap<String, Integer>();
        Map<String, Integer> paul = new LinkedHashMap<String, Integer>();
        Map<String, Integer> pri = new LinkedHashMap<String, Integer>();

        String line = new String();

        while ((line = readBiyanta.readLine()) != null) {
            String [] array = line.split(" ");
            String qid = array[0] + " " + array[2];
            biya.put(qid, Integer.valueOf(array[3]));
        }
        readBiyanta.close();

        while ((line = readPaulomi.readLine()) != null) {
            String [] array = line.split(" ");
            String qid = array[0] + " " + array[2];
            paul.put(qid, Integer.valueOf(array[3]));
        }
        readPaulomi.close();

        while ((line = readPriyanka.readLine()) != null) {
            String [] array = line.split(" ");
            String qid = array[0] + " " + array[2];
            pri.put(qid, Integer.valueOf(array[3]));
        }
        readPriyanka.close();
        System.out.println(biya.size() +" " + pri.size() + " " + paul.size());

        BufferedWriter writer = new BufferedWriter(new FileWriter("Input/MergedQrelFile.txt"));

        for (String link : biya.keySet()) {
            writer.write(link + " "+ biya.get(link) + " " + paul.get(link)+ " " + pri.get(link) + "\n");
        }

        writer.flush();
        writer.close();

    }
}
