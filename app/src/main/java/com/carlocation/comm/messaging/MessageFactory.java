package com.carlocation.comm.messaging;

import android.util.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;

public class MessageFactory {


	public static String addHeader(BaseMessage message) {
		JSONObject object = new JSONObject();
		conHeader(object, new MessageHeader(0, 1));
		try {
			object.put("body", message.translateJsonObject());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return object.toString();

	}

	public static String addHeader(ResponseMessage message) {
		JSONObject object = new JSONObject();
		conHeader(object, new MessageHeader(1, 1));
		try {
			object.put("body", message.translate());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return object.toString();
	}

	private static JSONObject conHeader(JSONObject object, MessageHeader header) {
		JSONObject h = new JSONObject();
		try {
			h.put("type", header.type);
			h.put("version", header.version);
			object.put("header", h);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return object;
	}

	public static MessageHeader parserHeader(String json) {
		MessageHeader h = new MessageHeader();
		JsonReader reader = new JsonReader(new StringReader(json));
		try {
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("header")) {
				String headerName = reader.nextName();
				reader.beginObject();
				while (reader.hasNext()) {
					if (headerName.equals("type")) {
							h.type = reader.nextInt();
					} else if (headerName.equals("version")) {
							h.version = reader.nextInt();
					}
				}
				reader.endObject();
				
			} else if (name.equals("body")) {
				h.body = reader.nextString();
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		} catch (Exception e) {
			
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return h;
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
