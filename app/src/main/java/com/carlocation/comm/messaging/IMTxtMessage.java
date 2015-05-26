package com.carlocation.comm.messaging;

import android.util.JsonReader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
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

    public IMTxtMessage() {
    }

    public IMTxtMessage(long mTransactionID, String mSenderId, List<String> mToTerminalId,
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
            JSONObject object = super.translateJsonObject();

            object.put("mRank", mRank.ordinal());
            object.put("mTxtCont", mTxtCont);

            return object;

        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSONException accured!");
            e.printStackTrace();
        }
        return null;
    }

    public static BaseMessage parseJsonObject(JsonReader reader){
        IMTxtMessage txtMsg = new IMTxtMessage();

        try {
            reader.beginObject();
            while (reader.hasNext()){
                String tagName = reader.nextName();
                if (tagName.equals("mTransactionID")) {
                    txtMsg.mTransactionID = reader.nextLong();
                }else if (tagName.equals("mMessageType")) {
                    txtMsg.mMessageType = MessageType.valueOf(reader.nextInt());
                }else if (tagName.equals("mSenderId")) {
                    txtMsg.mSenderId = reader.nextString();
                }else if (tagName.equals("mSenderType")) {
                    txtMsg.mSenderType = TerminalType.valueOf(reader.nextInt());
                }else if (tagName.equals("mImMsgType")) {
                    txtMsg.mImMsgType = IMMsgType.valueOf(reader.nextInt());
                }else if (tagName.equals("mToTerminalId")) {
                    txtMsg.mToTerminalId = readListStr(reader);
                }else if (tagName.equals("mRank")) {
                    txtMsg.mRank = RankType.valueOf(reader.nextInt());
                }else if (tagName.equals("mTxtCont")) {
                    txtMsg.mTxtCont = reader.nextString();
                }else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            return txtMsg;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> readListStr(JsonReader reader) {
        try{
            List<String> list = new ArrayList<>();
            reader.beginArray();
            while (reader.hasNext()){
                list.add(reader.nextString());
            }
            reader.endArray();
            return list;
        } catch (IOException e) {
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
