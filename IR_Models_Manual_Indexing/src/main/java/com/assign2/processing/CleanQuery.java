package com.assign2.processing;

import com.assign2.app.Driver;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Biyanta on 08/06/17.
 */
public class CleanQuery {

    public List<String> readQueries() throws FileNotFoundException {

        BufferedReader br = new BufferedReader(new FileReader(Driver.query_path));
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

    public String[] queryListWithoutStopwords(String query) {

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
            if(Driver.stopWords.contains(term)){
                queryIter.remove();
            }
        }
        return  queryArray.toArray(new String[queryArray.size()]);
    }
}
