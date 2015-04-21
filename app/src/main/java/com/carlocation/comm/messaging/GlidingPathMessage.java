package com.carlocation.comm.messaging;

/**
 * Created by Jacky on 2015/4/21.
 */
public class GlidingPathMessage extends Message {
    private long mTerminalId;
    private String mTitle;
    private int mGlidPathId;
    private Location[] mLocationArray;


    public GlidingPathMessage(long mTransactionID, long mTerminalId, String mTitle, int mGlidPathId,
                              Location[] mLocationArray) {
        super(mTransactionID);
        this.mTerminalId = mTerminalId;
        this.mTitle = mTitle;
        this.mGlidPathId = mGlidPathId;
        this.mLocationArray = mLocationArray;
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
