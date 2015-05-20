package com.carlocation.comm;

import com.carlocation.comm.messaging.Notification;

import java.io.Serializable;

/**
 * <ul>
 * Listener for notify response message.
 * </ul>
 * <ul>
 * To get response message, you have to make sure use
 * {@link IMessageService#sendMessage(com.carlocation.comm.messaging.BaseMessage, ResponseListener)}
 * to send request. Otherwise you don't receive response for ever.
 * </ul>
 * 
 * @author 28851274
 * 
 */
public interface ResponseListener extends Serializable{

	/**
	 * Response message notification.<br>
	 * Note: please make sure do not update UI in this function, because this
	 * function doesn't run in UI thread.
	 * 
	 * @param noti
	 */
	public void onResponse(Notification noti);
}
