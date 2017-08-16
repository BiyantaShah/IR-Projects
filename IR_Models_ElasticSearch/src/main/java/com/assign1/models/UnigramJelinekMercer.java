package com.assign1.models;

import com.assign1.processing.Documents;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Biyanta on 26/05/17.
 */
public class UnigramJelinekMercer {

    static final long totalTerms = 21196640;
    static final double lambda = 0.5;

    public static double score(Documents doc, Long docLength, long totalTermCount) {

        if(docLength==0 || totalTermCount==0){
            return 0;
        }
        long lengthOfRemainingDocs = totalTerms - docLength;

        double term1 = lambda * Double.valueOf(doc.getTermFrequency()) / docLength;
        double term2 = (1-lambda) * (totalTermCount - doc.getTermFrequency())/
                Double.valueOf(lengthOfRemainingDocs);
        double sol = term1 + term2;

        if(sol == 0.0){
            System.out.println("test");
            System.exit(1);
        }

        return Math.log10(term1+term2);
    }

}
