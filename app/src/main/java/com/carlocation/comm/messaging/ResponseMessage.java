package com.carlocation.comm.messaging;

import android.util.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;

public class ResponseMessage {


    final static int AUTH_MESSAGE = 0;
    final static int LOCATION_MESSAGE = 1;
    final static int IM_MESSAGE = 2;
    final static int TASK_MESSAGE = 3;
    final static int GLIDE_MESSAGE = 4;
    final static int WARN_MESSAGE = 5;
    final static int STATUS_MESSAGE = 6;

	public BaseMessage message;
	public MessageResponseStatus status;

    public ResponseMessage() {
        super();
    }

    public ResponseMessage(BaseMessage message, MessageResponseStatus status) {
        this.message = message;
        this.status = status;
    }

    public JSONObject translateJsonObject() {
		JSONObject object = new JSONObject();
		try {
			object.put("message", message.translateJsonObject());
			object.put("status", status.getValue());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return object;
	}

    public static ResponseMessage parseJsonObject(JsonReader reader, String json) {
        ResponseMessage rspMsg = new ResponseMessage();

        try {
            reader.beginObject();
            while (reader.hasNext()){
                String tagName = reader.nextName();
                if (tagName.equals("status")) {
                    rspMsg.status = MessageResponseStatus.valueOf(reader.nextInt());
                }else if (tagName.equals("message")) {
                    rspMsg.message = parseBaseMsg(reader, json);

                }else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            return rspMsg;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static BaseMessage parseBaseMsg(JsonReader reader, String json) {
        try {
            int msgType = -1;
            reader.beginObject();
            while (reader.hasNext()){
                String tagName = reader.nextName();
                if (tagName.equals("mMessageType")) {
                    msgType = reader.nextInt();
                }else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            return parseBaseMsgByType(msgType,json);
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
                if (tagName.equals("mBody")) {
                    return parseBaseMsgByType(reader,msgType,json);
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

    private static BaseMessage parseBaseMsgByType(JsonReader reader, int msgType, String json) {
        try {
            reader.beginObject();
            while (reader.hasNext()){
                String tagName = reader.nextName();
                if (tagName.equals("message")) {
                    switch (msgType) {
                        case LOCATION_MESSAGE:
                            return LocationMessage.parseJsonObject(reader);
                        case IM_MESSAGE:
                            int imMsgType = getImTypeFromStr(json);
                            if (imMsgType == IMMessage.IMMsgType.IM_TXT_MSG.ordinal()) {
                                return IMTxtMessage.parseJsonObject(reader);
                            } else if (imMsgType == IMMessage.IMMsgType.IM_VOICE_MSG.ordinal()) {
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
                if (tagName.equals("message")) {
                    return getImType(reader);
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

    private static int getImType(JsonReader reader) {
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

    @Override
	public String toString() {
		return "ResponseMessage [message=" + message + "   status:" + status
				+ "]";
	}
}
