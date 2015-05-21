package com.carlocation.comm;

import com.carlocation.comm.messaging.BaseMessage;
import com.carlocation.comm.messaging.ResponseMessage;

import java.io.Serializable;

/**
 * Interface for message service.<br>
 * 
 * @author 28851274
 *
 */
public interface IMessageService {
	
	
	/**
	 * Send message to remote. will ignore response
	 * @param message
	 */
	public void sendMessage(BaseMessage message);
	
	
	/**
	 * Send message and need response.<br>
	 * Response will send to listener
	 * @param message
	 * @param listener
	 */
	public void sendMessage(BaseMessage message, ResponseListener listener);
	
	
	/**
	 * Send response
	 * @param rm
	 */
	public void sendMessageResponse(ResponseMessage rm);
	
	
	
	/**
	 * Inform this service, UI won't wait this message response any longer
	 * @param message
	 */
	public void cancelWaiting(BaseMessage message);
	
	/**
	 * Register listener for unsolicited message notification
	 * @param listener
	 * 
	 * @see NotificationListener
	 */
	public void registerNotificationListener(NotificationListener listener);
	
	
	/**
	 * Unregister listener and never receive unsolicited message any longer
	 * @param listener
	 * 
	 * @see NotificationListener
	 */
	public void unRegisterNotificationListener(NotificationListener listener);

	public ConnectionState getConnState();

}
