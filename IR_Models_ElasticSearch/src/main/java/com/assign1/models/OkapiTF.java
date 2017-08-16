package com.assign1.models;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by Biyanta on 26/05/17.
 */
public class OkapiTF {

    public static double okapiScore(long termFrequency, Long docLen) {

        return Double.valueOf(termFrequency)/(termFrequency + 0.5 + 1.5 * (docLen / 250.0));
    }

}
