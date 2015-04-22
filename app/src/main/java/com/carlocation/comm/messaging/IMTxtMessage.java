package com.carlocation.comm.messaging;

/**
 * Created by Jacky on 2015/4/21.
 */
public class IMTxtMessage extends IMMessage {

    public byte mrank;
    public char[] mtxtCont;

    public IMTxtMessage(long mTransactionID) {
        super(mTransactionID);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public String translate() {
        return super.translate();
    }
}
