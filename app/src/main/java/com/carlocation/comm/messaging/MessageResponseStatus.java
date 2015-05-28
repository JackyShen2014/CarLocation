package com.carlocation.comm.messaging;

public enum MessageResponseStatus {

	SUCCESS(0), FORMAT_FAILED(-1), NOT_SUPPORTED(-2), UNKNOWN(1);

	private int code;

	MessageResponseStatus(int code) {
		this.code = code;
	}

	public int getValue() {
		return this.code;
	}

	public static MessageResponseStatus valueOf(int code) {
		switch (code) {
		case 0:
			return SUCCESS;
		case -1:
			return FORMAT_FAILED;
		case -2:
			return NOT_SUPPORTED;
		default:
			return UNKNOWN;

		}
	}
}
