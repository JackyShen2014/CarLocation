package com.carlocation.comm.messaging;

import android.util.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;

public class MessageFactory {


	public static JSONObject makeJson(BaseMessage message) {

		MessageHeader msgHead = new MessageHeader(MessageHeader.HeaderType.REQUEST,1);
		MessageJsonFormat msg = new MessageJsonFormat(msgHead,message);

		return msg.translateJsonObject();
	}

	public static JSONObject makeJson(ResponseMessage message) {
		MessageHeader msgHead = new MessageHeader(MessageHeader.HeaderType.RESPONSE,1);
		RspMessageJsonFormat rspMsg = new RspMessageJsonFormat(msgHead,message);

		return rspMsg.translateJsonObject();
	}

}
