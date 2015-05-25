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

    final static int AUTH_MESSAGE = 0;
    final static int LOCATION_MESSAGE = 1;
    final static int IM_MESSAGE = 2;
    final static int TASK_MESSAGE = 3;
    final static int GLIDE_MESSAGE = 4;
    final static int WARN_MESSAGE = 5;
    final static int STATUS_MESSAGE = 6;

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
                    msg.mHead = MessageHeader.paseJsonObject(reader);
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
            Log.e(LOG_TAG, "parseJsonObject():IOException!");
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
        JsonReader temp = reader;
        try {
            BaseMessage baseMsg = null;
            temp.beginObject();
            while (temp.hasNext()){
                String tagName = temp.nextName();
                if (tagName.equals("mMessageType")) {
                    int msgType = temp.nextInt();
                    baseMsg = parseBaseMsgByType(msgType,reader);
                }else {
                    temp.skipValue();
                }
            }
            temp.endObject();
            return baseMsg;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                temp.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private static BaseMessage parseBaseMsgByType(int msgType, JsonReader reader) {

        switch (msgType) {
            case LOCATION_MESSAGE:

                break;
            case IM_MESSAGE:
                break;
            case TASK_MESSAGE:
                break;
            case GLIDE_MESSAGE:
                GlidingPathMessage glideMsg = (GlidingPathMessage) GlidingPathMessage.parseJsonObject(reader.toString());
                return glideMsg;
            case WARN_MESSAGE:
                break;
            case STATUS_MESSAGE:
                break;
            default:
                break;
        }

        return null;
    }


}
