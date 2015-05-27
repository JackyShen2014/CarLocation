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
                    msg.mBody = parseBaseMsg(reader,json);
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
     * @param json
     * @return
     */
    public static BaseMessage parseBaseMsg(JsonReader reader, String json){
        try {
            reader.beginObject();
            while (reader.hasNext()){
                String tagName = reader.nextName();
                if (tagName.equals("mMessageType")) {
                    int msgType = reader.nextInt();
                    return parseBaseMsgByType(msgType,json);
                }else {
                    reader.skipValue();
                }
            }
            reader.endObject();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static BaseMessage parseBaseMsgByType(int msgType, String json) {
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            reader.beginObject();
            while (reader.hasNext()){
                String tagName = reader.nextName();
                if(tagName.equals("mHead")){
                    reader.skipValue();
                }else if (tagName.equals("mBody")){
                    switch (msgType) {
                        case LOCATION_MESSAGE:
                            return LocationMessage.parseJsonObject(reader);
                        case IM_MESSAGE:
                            int imMsgType = getImTypeFromStr(json);
                            if (imMsgType == IMMessage.IMMsgType.IM_TXT_MSG.ordinal()) {
                                return IMTxtMessage.parseJsonObject(reader);
                            }else if (imMsgType == IMMessage.IMMsgType.IM_VOICE_MSG.ordinal()) {
                                return IMVoiceMessage.parseJsonObject(reader);
                            }
                            break;
                        case TASK_MESSAGE:
                            return TaskAssignmentMessage.parseJsonObject(reader);
                        case GLIDE_MESSAGE:
                            return GlidingPathMessage.parseJsonObject(reader);
                        case WARN_MESSAGE:
                            return RestrictedAreaMessage.parseJsonObject(reader);
                        case STATUS_MESSAGE:
                            return StatusMessage.parseJsonObject(reader);
                        default:
                            break;
                    }
                }else {
                    reader.skipValue();
                }
            }
            reader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static int getImTypeFromStr(String json) {
        JsonReader reader = new JsonReader(new StringReader(json));
        try {
            reader.beginObject();
            while (reader.hasNext()){
                String tagName = reader.nextName();
                if (tagName.equals("mBody")){
                    return parseImType(reader);
                }else {
                    reader.skipValue();
                }
            }
            reader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    private static int parseImType(JsonReader reader) {
        try {
            reader.beginObject();
            while (reader.hasNext()){
                String tagName = reader.nextName();
                if (tagName.equals("mImMsgType")) {
                    return reader.nextInt();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

}
