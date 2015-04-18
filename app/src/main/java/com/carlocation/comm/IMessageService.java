package com.carlocation.comm;

import com.carlocation.comm.messaging.Message;

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
	public void sendMessage(Message message);
	
	
	/**
	 * Send message and need response.<br>
	 * Response will send to listener
	 * @param message
	 * @param listener
	 */
	public void sendMessage(Message message, ResponseListener listener);
	
	
	
	/**
	 * Inform this service, UI won't wait this message response any longer
	 * @param message
	 */
	public void cancelWaiting(Message message);
	
	/**
	 * Register listener for unsolicited message notification
	 * @param listener
	 */
	public void registerNotificationListener(NotificationListener listener);
	
	
	/**
	 * Unregister listener and never receive unsolicited message any longer
	 * @param listener
	 */
	public void unRegisterNotificationListener(NotificationListener listener);

}
