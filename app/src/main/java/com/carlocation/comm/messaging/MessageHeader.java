package com.carlocation.comm.messaging;

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

	
	
	
	public enum HeaderType {
		REQUEST,
		RESPONSE
	}
}
