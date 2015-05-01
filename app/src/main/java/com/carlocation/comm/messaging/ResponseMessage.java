package com.carlocation.comm.messaging;

import org.json.JSONException;
import org.json.JSONObject;

public class ResponseMessage {
	
	
	public static final String KEY_RQ = "rq";
	
	public static final String KEY_RET = "ret";

	public BaseMessage message;

	public MessageResponseStatus status;

    public ResponseMessage() {
        super();
    }

    public ResponseMessage(BaseMessage message, MessageResponseStatus status) {
        this.message = message;
        this.status = status;
    }

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

    @Override
    public String toString() {
        return "ResponseMessage [ status= " + status +"]";
    }
}
