import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Biyanta on 15/08/17.
 */
public class Relevance {

    public static void main (String[] args) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader("qrels.adhoc.51-100.AP89.txt"));

        Set<Integer> queries = new HashSet<Integer>();
        Set<String> combinations = new HashSet<String>();

        queries.add(54);queries.add(56);queries.add(57);queries.add(58);queries.add(59);
        queries.add(60);queries.add(61);queries.add(62);queries.add(64);queries.add(68);
        queries.add(71);queries.add(77);queries.add(80);queries.add(85);queries.add(87);
        queries.add(89);queries.add(91);queries.add(93);queries.add(94);queries.add(95);
        queries.add(97);queries.add(98);queries.add(63);queries.add(99);queries.add(100);

        String line = new String();
        List<String> set = new ArrayList<String>();

        while((line = reader.readLine())!=null){
            String[] values = line.split(" ");
            if(queries.contains(Integer.valueOf(values[0])) && values[3].equals("1")) {

                set.add(values[0]+":"+values[2]);
            }
        }
        System.out.println("relevant docs "+ set.size());

        for (int i = 0 ; i < set.size(); i++) {
            for (int j= i+1;j < set.size(); j++) {
                combinations.add(set.get(i)+"&&"+set.get(j));
            }
        }
        System.out.println("Combinations "+combinations.size());

        BufferedWriter writer = new BufferedWriter(new FileWriter("QueryCombinations.txt"));

        for (String combination : combinations) {
            writer.write(combination+"\n");
        }
        writer.flush();
        writer.close();

    }

}
