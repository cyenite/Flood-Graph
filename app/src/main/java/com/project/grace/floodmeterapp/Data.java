package com.project.grace.floodmeterapp;

import java.sql.Timestamp;
import java.util.Comparator;

public class Data {

    private Timestamp dateTimeRead;
    private double waterLevel;

    public Data(Timestamp dateTimeRead, double waterLevel){
        this.dateTimeRead = dateTimeRead;
        this.waterLevel = waterLevel;
    }

}

 class compTime implements Comparator<Timestamp> {
     @Override
     public int compare(Timestamp o1, Timestamp o2) {
         return o1.compareTo(o2);
     }
 }
