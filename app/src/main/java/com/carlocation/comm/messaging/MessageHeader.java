package com.carlocation.comm.messaging;

import android.util.JsonReader;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;

public class MessageHeader {
	private final static String LOG_TAG = "MessageHeader";

	public int type;
	public int version;


	public MessageHeader() {
		super();
	}

	public MessageHeader(int type, int version) {
		super();
		this.type = type;
		this.version = version;
	}


	public enum HeaderType {
		REQUEST,
		RESPONSE
	}

	public JSONObject translateJsonObject(){
		try {
			JSONObject object = new JSONObject();

			object.put("type",this.type);
			object.put("version",this.version);

			return object;
		}catch (JSONException e){
			e.printStackTrace();
			Log.e(LOG_TAG,"translateJsonObject():JSONException!");
		}

		return null;
	}

	public static MessageHeader paseJsonObject(String json){
		JsonReader reader = new JsonReader(new StringReader(json));
		MessageHeader msgHeader = new MessageHeader();
		try {
			reader.beginObject();
			while (reader.hasNext()){
				String tagName = reader.nextName();
				if (tagName.equals("type")) {
					msgHeader.type = reader.nextInt();
				}else if (tagName.equals("version")) {
					msgHeader.version = reader.nextInt();
				}
			}
			reader.endObject();
			return msgHeader;
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(LOG_TAG,"paseJsonObject():IOException!");
		}
		return null;
	}
}
