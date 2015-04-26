package com.carlocation.comm.messaging;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Jacky on 2015/4/21.
 * @author Jacky Shen
 */
public class IMVoiceMessage extends IMMessage{
    private static final String LOG_TAG = "IMVoiceMessage";

    //FIXME which type of data structure should I take
    public byte[] mVoiceData;

    public IMVoiceMessage(long mTransactionID) {
        super(mTransactionID);
    }

    public IMVoiceMessage(long mTransactionID, MessageType mMessageType, long mFromTerminalId,
                          long mToTerminalId, IMMsgType mImMsgType, byte[] mVoiceData) {
        super(mTransactionID, mMessageType, mFromTerminalId, mToTerminalId, mImMsgType);
        this.mVoiceData = mVoiceData;
    }


    @Override
    public String translate() {
        //Define return result
        String jSonResult = "";
        try{
            JSONObject object = new JSONObject();
            object.put("mTransactionID",IMVoiceMessage.this.mTransactionID);
            object.put("mMessageType",IMVoiceMessage.this.mMessageType);
            object.put("mFromTerminalId",mFromTerminalId);
            object.put("mToTerminalId",mToTerminalId);
            object.put("mImMsgType",mImMsgType);
            object.put("mVoiceData",mVoiceData);

            jSonResult = object.toString();

        }catch (JSONException e){
            Log.e(LOG_TAG, "JSONException accured!");
            e.printStackTrace();
        }
        Log.d(LOG_TAG,"Output json format is "+ jSonResult);
        return jSonResult;
    }

    @Override
    public String toString() {
        return "IMVoiceMessage ["
                + super.toString()
                + "mVoiceData=" + mVoiceData
                + "]";
    }
}
