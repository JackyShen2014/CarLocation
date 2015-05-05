package com.carlocation.comm.messaging;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jacky on 2015/4/21.
 *
 * @author Jacky Shen
 */
public class IMTxtMessage extends IMMessage {
    private static final String LOG_TAG = "IMTxtMessage";

    public RankType mRank;
    public String mTxtCont;

    public IMTxtMessage(long mTransactionID, long mSenderId, List<Long> mToTerminalId,
                        RankType mRank, String mTxtCont) {
        super(mTransactionID, mSenderId, mToTerminalId, IMMsgType.IM_TXT_MSG);
        this.mRank = mRank;
        this.mTxtCont = mTxtCont;
    }

    @Override
    public String translate() {
        // Define return result
        String jSonResult = "";
        JSONObject object = translateJsonObject();
        if (object != null) {
            jSonResult = object.toString();
        }
        Log.d(LOG_TAG, "Output json format is " + jSonResult);
        return jSonResult;
    }

    @Override
    public JSONObject translateJsonObject() {
        try {
            JSONObject object = new JSONObject();
            object.put("mTransactionID", IMTxtMessage.this.mTransactionID);
            object.put("mMessageType", IMTxtMessage.this.mMessageType.ordinal());
            object.put("mSenderId", IMTxtMessage.this.mSenderId);
            object.put("mSenderType", IMTxtMessage.this.mSenderType.ordinal());

            if (mToTerminalId != null) {
                JSONArray array = new JSONArray();
                for (long terminalId : mToTerminalId) {
                    array.put(terminalId);
                }

                object.put("mToTerminalId", array);
            }

            object.put("mImMsgType", mImMsgType.ordinal());

            object.put("mRank", mRank.ordinal());
            object.put("mTxtCont", mTxtCont);

            return object;

        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSONException accured!");
            e.printStackTrace();
        }
        return null;
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
