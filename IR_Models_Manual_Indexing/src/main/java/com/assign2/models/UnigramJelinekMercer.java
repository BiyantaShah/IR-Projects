package com.assign2.models;

import com.assign2.indexing.TokenInfo;

/**
 * Created by Biyanta on 08/06/17.
 */
public class UnigramJelinekMercer {

    static final long totalTerms = 21196640;
    static final double lambda = 0.5;

    public static double score(TokenInfo doc, Long docLength, long totalTermCount) {

        if(docLength == 0 || totalTermCount == 0){
            return 0;
        }
        long lengthOfRemainingDocs = totalTerms - docLength;

        double term1 = lambda * Double.valueOf(doc.getCount()) / docLength;
        double term2 = (1-lambda) * (totalTermCount - doc.getCount())/
                Double.valueOf(lengthOfRemainingDocs);
        double sol = term1 + term2;

        if(sol == 0.0){
            System.out.println("test");
            System.exit(1);
        }

        return Math.log10(term1+term2);
    }
}
