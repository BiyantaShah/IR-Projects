import org.apache.tika.language.LanguageIdentifier;
import org.jsoup.Jsoup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Biyanta on 02/08/17.
 */
public class IndexFiles {

    public static int totalTrainingRecords = 60335;
    public static int spamTrainingRecords = 40223;
    public static int hamTrainingRecords = 20112;

    public void indexFiles(File[] listOfFiles) throws IOException {

        BufferedWriter writer1 = new BufferedWriter(new FileWriter("trainIDS.txt"));
        BufferedWriter writer2 = new BufferedWriter(new FileWriter("testIDS.txt"));

        int spam = 0, ham = 0;

        for (File file : listOfFiles) {

            if (file.getName().contains("inmail")) {
//                    && languageOkay(Jsoup.parse(file, "UTF-8").text().toLowerCase())) {

                String text = parseFile(file);
                int id = Integer.valueOf(file.getName().split("\\.")[1]);
                String label = ElasticSearchConnection.spamHamMap.get(file.getName().trim());

                if (label.equals("spam") && spam < spamTrainingRecords) {
                    Driver.elasticSearchConnection.indexFileOnES(id, text, label, file.getName(), "train", false);

                    spam ++;
                    writer1.write(id +" "+file.getName().trim()+"\n");
                }

                else if (label.equals("ham") && ham < hamTrainingRecords) {
                    Driver.elasticSearchConnection.indexFileOnES(id, text, label, file.getName(), "train", false);

                    ham ++;
                    writer1.write(id +" "+file.getName().trim()+"\n");
                }

                else {
                    Driver.elasticSearchConnection.indexFileOnES(id, text, label, file.getName(), "test", false);
                    writer2.write(id +" "+file.getName().trim()+"\n");
                }
            }
        }

        Driver.elasticSearchConnection.indexFileOnES(-1, null, null, null, null, true);

        writer1.flush();
        writer2.flush();

        writer1.close();
        writer2.close();
        System.out.println("Spam "+ spam + " "+ "Ham "+ ham);

    }

    private boolean languageOkay(String text) {
        try{
            return new LanguageIdentifier(text).
                    getLanguage().
                    equalsIgnoreCase("en");
        }
        catch(Exception e){
            return false;
        }

    }

    private String parseFile(File file) throws IOException {
        
        return cleanText(Jsoup.parse(file, "UTF-8").text());
    }

    private String cleanText(String text) {

        return text.replaceAll("[-=/]", " ")
                .replaceAll("\\.()", "")
                .replaceAll("\\(", "")
                .replaceAll("\\)", "")
                .replaceAll("[\":\"+()<>!,;\"]", "");
    }
}
