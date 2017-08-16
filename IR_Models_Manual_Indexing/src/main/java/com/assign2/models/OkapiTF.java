package com.assign2.models;

/**
 * Created by Biyanta on 08/06/17.
 */
public class OkapiTF {

    public static double okapiScore(long termFrequency, Long docLen) {

        return Double.valueOf(termFrequency)/(termFrequency + 0.5 + 1.5 * (docLen / 250.0));
    }

}
