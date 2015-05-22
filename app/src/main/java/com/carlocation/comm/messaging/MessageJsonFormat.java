package com.carlocation.comm.messaging;

import android.util.JsonReader;
import android.util.Log;

import com.rabbitmq.tools.json.JSONReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;

/**
 * Created by 28851620 on 5/22/2015.
 */
public class MessageJsonFormat {
    private final static String LOG_TAG = "MessageJsonFormat";

    public MessageHeader mHead;
    public BaseMessage mBody;

    public MessageJsonFormat() {
    }

    public MessageJsonFormat(MessageHeader mHead, BaseMessage mBody) {
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
            Log.e(LOG_TAG,"translateJsonObject():JSONException!");
        }
        return null;
    }

    public static MessageJsonFormat parseJsonObject(String json) {
        JsonReader reader = new JsonReader(new StringReader(json));
        MessageJsonFormat msg = new MessageJsonFormat();
        try{
            reader.beginObject();
            while (reader.hasNext()){
                String tagName = reader.nextName();
                if(tagName.equals("mHead")){
                    msg.mHead = MessageHeader.paseJsonObject(reader.toString());
                }else if (tagName.equals("mBody")){
                    msg.mBody = parseBaseMsg(reader);

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

    /**
     * Check the msg_type and then parse and return the relative subclass of BaseMessage.
     * @param reader
     * @return
     */
    public static BaseMessage parseBaseMsg(JsonReader reader){
        //FIXME JSON
        return new BaseMessage() {
            @Override
            public String translate() {
                return null;
            }
        };

    }



}
