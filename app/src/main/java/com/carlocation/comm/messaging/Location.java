package com.carlocation.comm.messaging;

/**
 * This class is used for LocationMsg, GlidingPathMsg as well as RestrictedAreaMsg
 * @author Jacky Shen
 */

public class Location {

    public double mLng;

    public double mLat;

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
	
}
