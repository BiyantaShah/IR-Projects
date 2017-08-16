package com.assign2.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Biyanta on 09/06/17.
 */
public class ProximitySearch {

    public static int scoreForDoc(List<List<Integer>> positionList) throws Exception {

        long maxListSize = 0;

        for (List<Integer> list : positionList) {

            if(maxListSize < list.size()){
                maxListSize = list.size();
            }
        }

        int docScore = Integer.MAX_VALUE;
        int[] scores = new int[positionList.size()];
        Map<Integer,Integer> scoreMap = new HashMap<Integer,Integer>();

        for (int i = 0; i < positionList.size() * maxListSize; i++){

            scoreMap.clear();
            for(int j = 0 ; j < positionList.size(); j++){
                scores[j] = positionList.get(j).get(0);
                scoreMap.put(scores[j], j);
            }

            bubbleSort(scores);

            int tempScore = scores[scores.length-1]-scores[0];
            if(tempScore < docScore){
                docScore = tempScore+1;
            }

            boolean isDone = true;
            for (int p = 0 ; p < positionList.size() ; p++) {
                isDone = isDone && (positionList.get(p).size() == 1);
            }

            if(isDone){
                return docScore;
            }

            for(int y = 0 ; y < positionList.size();y++){

                int arrPos = scoreMap.get(scores[y]);
                if(positionList.get(arrPos).size()==1){
                    continue;
                }else{
                    positionList.get(arrPos).remove(0);
                    break;
                }
            }

        }
        throw new Exception("bluh");
    }

    private static void bubbleSort(int[] scores) {
        for(int i = 0 ; i < scores.length; i++){
            boolean swap = false;
            for(int j=0; j < (scores.length-i-1); j++ ){
                if(scores[j] > scores[j+1]){
                    int temp = scores[j];
                    scores[j] = scores[j+1];
                    scores[j+1] = temp;
                    swap =true;
                }
            }
            if(!swap){
                break;
            }
        }
    }
}
