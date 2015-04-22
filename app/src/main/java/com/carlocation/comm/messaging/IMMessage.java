package com.carlocation.comm.messaging;

/**
 * Created by Jacky on 2015/4/21.
 */
public class IMMessage extends Message{

    public long mFromTerminalId;
    public long mToTerminalId;
    public IMMsgType mImMsgType;

    public static enum IMMsgType{
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
