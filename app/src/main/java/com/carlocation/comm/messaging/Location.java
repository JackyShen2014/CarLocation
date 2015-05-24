package com.carlocation.comm.messaging;

import android.util.JsonReader;

import java.io.IOException;
import java.io.StringReader;

/**
 * This class is used for LocationMsg, GlidingPathMsg as well as RestrictedAreaMsg
 * @author Jacky Shen
 */

public class Location {

    public double mLng;

    public double mLat;

    public Location() {
    }

    public Location(double lng, double lat) {
		super();
		this.mLng = lng;
        this.mLat = lat;
	}

    public String toString() {
        return "Location ["
                + "mLng=" + mLng
                + ", mLat=" + mLat
                + "]";
    }

    public static Location parseLocation(JsonReader reader){
        Location lc =  new Location();
        try{
            reader.beginObject();
            while (reader.hasNext()){
                String tagName = reader.nextName();
                if (tagName.equals("mLng")) {
                    lc.mLng = reader.nextDouble();
                } else if (tagName.equals("mLat")) {
                    lc.mLat = reader.nextDouble();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            return lc;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
	
}
