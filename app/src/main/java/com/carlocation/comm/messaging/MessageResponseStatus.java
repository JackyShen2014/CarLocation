package com.carlocation.comm.messaging;

public enum MessageResponseStatus {

	SUCCESS(0), FORMAT_FAILED(-1), NOT_SUPPORTED(-2), UNKNOWN(1);

	private int code;

	private MessageResponseStatus(int code) {
		this.code = code;
	}

	public int getValue() {
		return this.code;
	}

	public static MessageResponseStatus fromCode(int code) {
		switch (code) {
		case 0:
			return SUCCESS;
		case -1:
			return FORMAT_FAILED;
		case -2:
			return NOT_SUPPORTED;
		default:
			return NOT_SUPPORTED;

		}
	}
}
