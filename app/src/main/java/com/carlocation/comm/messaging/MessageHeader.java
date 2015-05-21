package com.carlocation.comm.messaging;

import org.json.JSONObject;

public class MessageHeader {
	public int type;
	public int version;
	public String body;

	public MessageHeader() {
		super();
	}

	public MessageHeader(int type, int version) {
		super();
		this.type = type;
		this.version = version;
	}

	public MessageHeader(int type, int version, String body) {
		super();
		this.type = type;
		this.version = version;
		this.body = body;
	}

	public enum HeaderType {
		REQUEST,
		RESPONSE
	}
}
