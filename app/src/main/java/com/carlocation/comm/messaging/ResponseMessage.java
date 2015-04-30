package com.carlocation.comm.messaging;

import org.json.JSONException;
import org.json.JSONObject;

public class ResponseMessage {
	
	
	public static final String KEY_RQ = "rq";
	
	public static final String KEY_RET = "ret";

	public Message message;

	public MessageResponseStatus status;

	public String translate() {
		JSONObject object = new JSONObject();
		try {
			object.put(KEY_RQ, message.translate());
			object.put(KEY_RET, status.getValue());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return object.toString();
	}

}
