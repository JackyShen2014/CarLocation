package com.carlocation.comm.messaging;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Jacky on 2015/4/21.
 *
 * @author Jacky Shen
 */
public class IMVoiceMessage extends IMMessage {
    private static final String LOG_TAG = "IMVoiceMessage";

    public byte[] mVoiceData;

    public IMVoiceMessage(long mTransactionID, long mSenderId, List<Long> mToTerminalId,
                          byte[] mVoiceData) {
        super(mTransactionID, mSenderId, mToTerminalId, IMMsgType.IM_VOICE_MSG);
        this.mVoiceData = mVoiceData;
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
            object.put("mTransactionID", IMVoiceMessage.this.mTransactionID);
            object.put("mMessageType", IMVoiceMessage.this.mMessageType.ordinal());
            object.put("mSenderId", IMVoiceMessage.this.mSenderId);
            object.put("mSenderType", IMVoiceMessage.this.mSenderType.ordinal());

            if (mToTerminalId != null) {
                JSONArray array = new JSONArray();
                for (long terminalId : mToTerminalId) {
                    array.put(terminalId);
                }

                object.put("mToTerminalId", array);
            }
            object.put("mImMsgType", mImMsgType.ordinal());

            JSONArray array = new JSONArray();
            for (int i = 0; i < mVoiceData.length; i++) {
                array.put(mVoiceData[i]);
            }

            object.put("mVoiceData", array);

            return object;

        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSONException accured!");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return "IMVoiceMessage ["
                + super.toString()
                + "mVoiceData=" + mVoiceData
                + "]";
    }
}
