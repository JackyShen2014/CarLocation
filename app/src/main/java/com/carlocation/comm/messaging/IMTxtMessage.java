package com.carlocation.comm.messaging;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Jacky on 2015/4/21.
 *
 * @author Jacky Shen
 */
public class IMTxtMessage extends IMMessage {
    private static final String LOG_TAG = "IMTxtMessage";

    public byte mRank;
    public String mTxtCont;

    public IMTxtMessage(long mTransactionID) {
        super(mTransactionID);
    }

    public IMTxtMessage(long mTransactionID, long mFromTerminalId,
                        long mToTerminalId, byte mRank, String mTxtCont) {
        super(mTransactionID, mFromTerminalId, mToTerminalId, IMMsgType.IM_TXT_MSG);
        this.mRank = mRank;
        this.mTxtCont = mTxtCont;
    }


    @Override
    public String translate() {
        //Define return result
        String jSonResult = "";
        try {
            JSONObject object = new JSONObject();
            object.put("mTransactionID", IMTxtMessage.this.mTransactionID);
            object.put("mMessageType", IMTxtMessage.this.mMessageType.ordinal());
            object.put("mFromTerminalId", mFromTerminalId);
            object.put("mToTerminalId", mToTerminalId);
            object.put("mImMsgType", mImMsgType.ordinal());
            object.put("mRank", mRank);
            object.put("mTxtCont", mTxtCont);

            jSonResult = object.toString();

        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSONException accured!");
            e.printStackTrace();
        }
        Log.d(LOG_TAG, "Output json format is " + jSonResult);
        return jSonResult;
    }

    @Override
    public String toString() {
        return "IMTxtMessage ["
                + super.toString()
                + "mRank=" + mRank
                + ", mTxtCont=" + mTxtCont
                + "]";
    }
}
