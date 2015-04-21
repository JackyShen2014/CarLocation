package com.carlocation.comm.messaging;

/**
 * Created by Jacky on 2015/4/21.
 */
public class RestrictedAreaMessage extends Message {

    private Location[] mLocationArea;

    public RestrictedAreaMessage(long mTransactionID, Location[] mLocationArea) {
        super(mTransactionID);
        this.mLocationArea = mLocationArea;
    }

    @Override
    public String translate() {
        return null;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
