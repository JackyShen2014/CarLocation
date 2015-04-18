package com.carlocation.comm.messaging;

/**
 * <ul>Define message type for communications.</uL>
 * <ul>
 * <li>AUTH_MESSAGE     : For login or logout</li>
 * <li>LOCATION_MESSAGE : For report location or receive location update</li>
 * <li>TAXI_MESSAGE     : For car taxi</li>
 * <li>TEXT_MESSAGE     : For text message</li>
 * </ul>
 * @author 28851274
 *
 */
public enum MessageType {

	/**
	 * Use to authentication 
	 */
	AUTH_MESSAGE,
	
	/**
	 * Use to location update 
	 */
	LOCATION_MESSAGE,
	
	/**
	 * Use to car taxing 
	 */
	TAXI_MESSAGE,
	
	/**
	 * Use to text message
	 */
	TEXT_MESSAGE,
	
	/**
	 * Use to voice message
	 */
	VOICE_MESSAGE,
	
	/**
	 * Use to assignment
	 */
	ASSIGNMENT_MESSAGE,
	
	
}
