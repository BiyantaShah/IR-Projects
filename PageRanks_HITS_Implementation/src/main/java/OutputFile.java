import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Biyanta on 03/07/17.
 */
public class OutputFile {

    public static void writeToOutputFile(Map<String, Double> sortedPages, String fileName) throws IOException {
//        File opFolder = new File("Output");
//        opFolder.mkdir();

        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

        Iterator<Map.Entry<String, Double>> iter = sortedPages.entrySet().iterator();

        int count = 1;

        while (iter.hasNext()) {
            if (count ++ == 501)
                break;
            Map.Entry<String, Double> value = iter.next();
            writer.write(value.getKey() +"\t"+value.getValue()+"\n");
        }

        writer.flush();
        writer.close();

    }

    public static void writeOutput(Map<String, Double> sortedPages, String fileName, Map<String, Set<String>> inlinksMap) throws IOException {

        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

        Iterator<Map.Entry<String, Double>> iter = sortedPages.entrySet().iterator();

        int count = 1;

        while (iter.hasNext()) {
            if (count ++ == 501)
                break;
            Map.Entry<String, Double> value = iter.next();
            writer.write(value.getKey() +"\t"+value.getValue()+"\t"+inlinksMap.get(value.getKey()).size()+"\n");
        }

        writer.flush();
        writer.close();
    }
}
