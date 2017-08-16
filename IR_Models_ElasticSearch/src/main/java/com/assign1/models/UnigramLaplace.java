package com.assign1.models;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Biyanta on 26/05/17.
 */
public class UnigramLaplace {

    static final long vocabulary = 178081;

    public static double laplaceSmoothingScore(long termFrequency, Long docLength) {

        return Double.valueOf(termFrequency+1.0)/(docLength + vocabulary);
    }

    public static double penalizedScore(Long doclength, int queryLength) {

        return 1.0/(doclength + vocabulary) * queryLength;
    }


}
