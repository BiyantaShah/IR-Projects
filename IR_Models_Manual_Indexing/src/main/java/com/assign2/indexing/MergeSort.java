package com.assign2.indexing;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Biyanta on 06/06/17.
 */
public class MergeSort {

    public static void MergeSort(List<TokenInfo> a, int low, int high){
        if(low<high){
            int mid = (low+high)/2;
            MergeSort(a,low,mid);
            MergeSort(a,mid+1,high);
            Merge(a,low,mid,high);
        }
    }

    public static void Merge(List<TokenInfo> a, int low, int mid, int high){

        List<TokenInfo> tempArray = new ArrayList<TokenInfo>();
        tempArray.addAll(a);

        int i = low;
        int j = mid+1;
        int count = low;

        while(i <= mid && j <= high){
            if(tempArray.get(j).getCount() < tempArray.get(i).getCount()) {

                a.set(count, tempArray.get(i));
                i++;
            }
            else{

                a.set(count, tempArray.get(j));
                j++;
            }
            count++;
        }

        while(i <= mid){

            a.set(count, tempArray.get(i));
            i++;
            count++;
        }
    }
}
