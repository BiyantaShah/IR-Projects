package com.assign1.processing;

import com.assign1.app.Driver;

import java.io.*;
import java.util.*;

/**
 * Created by Biyanta on 26/05/17.
 */
public class CleanQuery {

    File queryFile = new File(Driver.query_path);
    private static final Set<String> stopWords = new HashSet<String>();


    public List<String> readQueries() throws FileNotFoundException {

        BufferedReader br = new BufferedReader(new FileReader(queryFile));
        List<String> queryList = new ArrayList<String>();
        String eachLine;
        try {
            while ((eachLine = br.readLine()) != null) {
                if (eachLine.trim().length() != 0) {
                    queryList.add(eachLine);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return queryList;

    }

    private void processStopWords() throws IOException {

        File stopList_path = new File("stoplist.txt");
        BufferedReader br = new BufferedReader(new FileReader(stopList_path));
        String readLine;

        while ((readLine = br.readLine()) != null) {
            stopWords.add(readLine);
        }
    }

    public String[] queryListWithoutStopwords(String query) {

        // Get all the stop words
        try {
            processStopWords();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // remove unnecessary punctuations
        query = query.trim();
        query = query.replaceAll(",", "");
        query = query.replaceAll("\\.", "");
        query=  query.replaceAll("( )+", " ");
        query = query.replaceAll("[^a-zA-Z0-9 ]", "");

        List<String> queryArray = new ArrayList<String>(Arrays.asList(query.split(" ")));
        ListIterator<String> queryIter = queryArray.listIterator();

        while(queryIter.hasNext()){
            String term = queryIter.next();
            if(stopWords.contains(term)){
                queryIter.remove();
            }
        }
        return  queryArray.toArray(new String[queryArray.size()]);
    }


}
