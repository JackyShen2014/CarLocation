package com.carlocation.comm.messaging;

import android.util.JsonReader;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;

/**
 * Created by Jacky on 2015/5/23.
 */
public class RspMessageJsonFormat {

    private final static String LOG_TAG = "RspMessageJsonFormat";

    public MessageHeader mHead;
    public ResponseMessage mBody;

    public RspMessageJsonFormat() {
    }

    public RspMessageJsonFormat(MessageHeader mHead, ResponseMessage mBody) {
        this.mHead = mHead;
        this.mBody = mBody;
    }

    public JSONObject translateJsonObject() {
        try{
            JSONObject object = new JSONObject();

            object.put("mHead",mHead.translateJsonObject());
            object.put("mBody",mBody.translateJsonObject());

            return object;

        }catch (JSONException e){
            e.printStackTrace();
            Log.e(LOG_TAG, "translateJsonObject():JSONException!");
        }
        return null;
    }

    public static RspMessageJsonFormat parseJsonObject(String json) {
        JsonReader reader = new JsonReader(new StringReader(json));
        RspMessageJsonFormat msg = new RspMessageJsonFormat();
        try{
            reader.beginObject();
            while (reader.hasNext()){
                String tagName = reader.nextName();
                if(tagName.equals("mHead")){
                    msg.mHead = MessageHeader.paseJsonObject(reader);
                }else if (tagName.equals("mBody")){
                    msg.mBody = parseRspMessage(reader);

                }else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            return msg;
        }catch (IOException e){
            e.printStackTrace();
            Log.e(LOG_TAG,"parseJsonObject():IOException!");
        }finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static ResponseMessage parseRspMessage(JsonReader reader) {
        //TODO JSON parse ResponseMessage

        return null;
    }


}
