package com.assign2.processing;

import com.assign2.app.Driver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Biyanta on 08/06/17.
 */
public class OutputFile {

    public static void outputResultsToFile(Map<String, Double> sortedScore, String queryTerm, String filePath) throws IOException {

        File okapiTFIDFFile = new File(filePath);

        if(!okapiTFIDFFile.exists())
            okapiTFIDFFile.createNewFile();

        FileWriter fw = new FileWriter(okapiTFIDFFile,true);
        Iterator<Map.Entry<String, Double>> qsIter = sortedScore.entrySet().iterator();

        int count = 1;
        while(qsIter.hasNext()){
            if(count == 1001){ // only 1000 documents per query
                break;
            }

            Map.Entry<String, Double> tempEntry = qsIter.next();

            fw.write(queryTerm+" "+ "Q0" + " " + Driver.intToDocIdsMapping.getProperty(tempEntry.getKey())
                    +" "+ count + " " +
                    tempEntry.getValue()+" " +"IR");
            ++count;
            fw.append(System.getProperty("line.separator"));
        }

        fw.flush();
        fw.close();
    }
}
