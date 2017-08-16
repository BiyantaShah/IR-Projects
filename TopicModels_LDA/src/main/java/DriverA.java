import java.io.File;
import java.io.IOException;

/**
 * Created by Biyanta on 10/08/17.
 */
public class DriverA {

    public static File folder_path = new File("ap89_collection");
    public static File[] listOfFiles = folder_path.listFiles();

    public static void main (String[] args) throws IOException {

        WordMapper mapper = new WordMapper();
        mapper.loadFiles(listOfFiles);
        mapper.generateWordMapper();
        System.out.println("generate topics");
        mapper.generateTopics();
    }

}
