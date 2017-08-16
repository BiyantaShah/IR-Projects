package com.assign2.processing;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Biyanta on 08/06/17.
 */
public class DocumentIdMapping {


    public static void docToIntMapping(Map<String, Long> docToIntMap) throws IOException {

        File file = new File("intToDocIdsMapping.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        for (String key : docToIntMap.keySet()) {
            writer.write(docToIntMap.get(key)+"="+key+"\n");
        }

        writer.flush();
        writer.close();
    }
}
