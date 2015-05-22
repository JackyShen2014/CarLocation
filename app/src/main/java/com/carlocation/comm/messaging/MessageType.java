package com.carlocation.comm.messaging;

/**
 * <ul>Define message type for communications.</uL>
 * @author Jacky Shen
 */
public enum MessageType {

	/**
	 * Used for authentication
	 */
	AUTH_MESSAGE(0),
	
	/**
	 * Used for location update
	 */
	LOCATION_MESSAGE(1),

    /**
     * Used for Instant Message
     */
	IM_MESSAGE(2),

	/**
	 * Used to assign task to car
	 */
	TASK_MESSAGE(3),

    /**
     * Used for gliding path as Server asked
     */
	GLIDE_MESSAGE(4),

	/**
	 * Used for restrict areas for car
	 */
	WARN_MESSAGE(5),

    /**
     *Used for mobile pad or control pc to send for status notification
     */
    STATUS_MESSAGE(6),
	/**
	 * Unknown Message
	 */
	UNKNOWN_MESSAGE(-1);

	private int code;

	MessageType(int code) {
		this.code = code;
	}

	public static MessageType valueOf(int code){
		switch (code){
			case 0:
				return AUTH_MESSAGE;
			case 1:
				return LOCATION_MESSAGE;
			case 2:
				return IM_MESSAGE;
			case 3:
				return TASK_MESSAGE;
			case 4:
				return GLIDE_MESSAGE;
			case 5:
				return WARN_MESSAGE;
			case 6:
				return STATUS_MESSAGE;
			default:return UNKNOWN_MESSAGE;
		}
	}
}
