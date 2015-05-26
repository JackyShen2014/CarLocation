package com.carlocation.comm.messaging;

import android.util.JsonReader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jacky on 2015/4/21.
 *
 * @author Jacky Shen
 */
public class IMVoiceMessage extends IMMessage {
    private static final String LOG_TAG = "IMVoiceMessage";

    public byte[] mVoiceData;

    public IMVoiceMessage() {

    }

    public IMVoiceMessage(long mTransactionID, String mSenderId, List<String> mToTerminalId,
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
            JSONObject object = super.translateJsonObject();

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

    public static BaseMessage parseJsonObject(JsonReader reader){
        IMVoiceMessage voiceMsg =  new IMVoiceMessage();

        try {
            reader.beginObject();
            while (reader.hasNext()) {
                String tagName = reader.nextName();
                if (tagName.equals("mTransactionID")) {
                    voiceMsg.mTransactionID = reader.nextLong();
                } else if (tagName.equals("mMessageType")) {
                    voiceMsg.mMessageType = MessageType.valueOf(reader.nextInt());
                } else if (tagName.equals("mSenderId")) {
                    voiceMsg.mSenderId = reader.nextString();
                } else if (tagName.equals("mSenderType")) {
                    voiceMsg.mSenderType = TerminalType.valueOf(reader.nextInt());
                } else if (tagName.equals("mImMsgType")) {
                    voiceMsg.mImMsgType = IMMsgType.valueOf(reader.nextInt());
                } else if (tagName.equals("mToTerminalId")) {
                    voiceMsg.mToTerminalId = IMTxtMessage.readListStr(reader);
                } else if (tagName.equals("mVoiceData")) {
                    voiceMsg.mVoiceData = readArray(reader);
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            return voiceMsg;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] readArray(JsonReader reader) {
        List<Byte> array = new ArrayList<>();
        try {
            reader.beginArray();
            while (reader.hasNext()){
                array.add(Byte.parseByte(reader.nextString()));
            }
            reader.endArray();

            byte[] bt = new byte[array.size()];
            for (int i=0; i<array.size(); i++){
                bt[i] = array.get(i);
            }
            return bt;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    public String toString() {
        return "IMVoiceMessage ["
                + super.toString()
                + "mVoiceData=" + mVoiceData
                + "]";
    }
}
