package com.carlocation.comm.messaging;

/**
 * <ul>Define message type for communications.</uL>
 *
 */
public enum MessageType {

	/**
	 * Used for authentication
	 */
	AUTH_MESSAGE,
	
	/**
	 * Used for location update
	 */
	LOCATION_MESSAGE,

    /**
     * Used for Instant Message
     */
	IM_MESSAGE,

	/**
	 * Used to assign task to car
	 */
	TASK_MESSAGE,

    /**
     * Used for gliding path as Server asked
     */
	GLIDE_MESSAGE,

	/**
	 * Used for restrict areas for car
	 */
	WARN_MESSAGE,

}
