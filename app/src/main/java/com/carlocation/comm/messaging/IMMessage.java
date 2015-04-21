package com.carlocation.comm.messaging;

/**
 * Created by Jacky on 2015/4/21.
 */
public class IMMessage extends Message{

    private long mFromTerminalId;
    private long mToTerminalId;

    public enum IMMsgType{
        IM_TXT_MSG,
        IM_VOICE_MSG,
    }


    public IMMessage(long mTransactionID) {
        super(mTransactionID);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public String translate() {
        return null;
    }
}
