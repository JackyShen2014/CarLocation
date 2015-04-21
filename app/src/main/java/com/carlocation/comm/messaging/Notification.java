package com.carlocation.comm.messaging;

public class Notification {
    public Notification(){
        super();
    }
	
	public Message message;

	public enum NotificationType {
		REQUEST,
		RESPONSE,
		UNSOLICITED,
	}

}
