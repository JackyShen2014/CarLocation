package com.carlocation.comm.messaging;

import android.util.JsonReader;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;

public class MessageHeader {
	private final static String LOG_TAG = "MessageHeader";

	public HeaderType type;
	public int version;


	public MessageHeader() {
		super();
	}

	public MessageHeader(HeaderType type, int version) {
		this.type = type;
		this.version = version;
	}

	public enum HeaderType {
		REQUEST(0),
		RESPONSE(1),
		UNKNOWN(-1);

		private int code;

		HeaderType(int code) {
			this.code = code;
		}

		public static HeaderType valueOf(int code){
			switch (code){
				case 0:
					return REQUEST;
				case 1:
					return RESPONSE;
				default:return UNKNOWN;
			}

		}
	}

	public JSONObject translateJsonObject(){
		try {
			JSONObject object = new JSONObject();

			object.put("type",this.type.ordinal());
			object.put("version",this.version);

			return object;
		}catch (JSONException e){
			e.printStackTrace();
			Log.e(LOG_TAG,"translateJsonObject():JSONException!");
		}

		return null;
	}

	public static MessageHeader paseJsonObject(JsonReader reader){
		MessageHeader msgHeader = new MessageHeader();
		try {
			reader.beginObject();
			while (reader.hasNext()){
				String tagName = reader.nextName();
				if (tagName.equals("type")) {
					msgHeader.type = HeaderType.valueOf(reader.nextInt());
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
