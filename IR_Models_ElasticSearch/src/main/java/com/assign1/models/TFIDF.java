package com.assign1.models;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Biyanta on 26/05/17.
 */
public class TFIDF {

    public static double tfidfScore(Double okapiScore, int totalDocuments, long documentFrequency) {

        if(0 == documentFrequency){
            throw new IllegalStateException();
        }
        double tfScore = okapiScore * Math.log10(totalDocuments / documentFrequency);
        return tfScore;
    }


}
