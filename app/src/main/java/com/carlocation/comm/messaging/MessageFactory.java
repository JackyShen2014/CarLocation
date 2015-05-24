package com.carlocation.comm.messaging;

import android.util.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;

public class MessageFactory {


	public static String addHeader(BaseMessage message) {

		MessageHeader msgHead = new MessageHeader(MessageHeader.HeaderType.REQUEST,1);
		MessageJsonFormat msg = new MessageJsonFormat(msgHead,message);

		return msg.translateJsonObject().toString();
	}

	public static String addHeader(ResponseMessage message) {
		MessageHeader msgHead = new MessageHeader(MessageHeader.HeaderType.RESPONSE,1);
		RspMessageJsonFormat msg = new RspMessageJsonFormat(msgHead,message);

		return msg.translateJsonObject().toString();
	}

	public static BaseMessage parseRequestFromJSON(String json) {
		JsonReader reader = new JsonReader(new StringReader(json));
		try {
			return parseRequestFromJSON(reader);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static BaseMessage parseRequestFromJSON(JsonReader reader) {
		return null;
	}

	public static ResponseMessage parseResponseFromJSON(JsonReader reader) {
		try {
			ResponseMessage rm = new ResponseMessage();
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (ResponseMessage.KEY_RET.equals(name)) {
					rm.status = MessageResponseStatus.fromCode(reader.nextInt());
				} else if (ResponseMessage.KEY_RQ.equals(name)) {
					rm.message = parseRequestFromJSON(reader);
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
			return rm;
		} catch (Exception e) {

		}

		return null;
	}

	public static ResponseMessage parseResponseFromJSON(String json) {
		JsonReader reader = new JsonReader(new StringReader(json));
		try {
			return parseResponseFromJSON(reader);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
