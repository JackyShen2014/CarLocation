package com.carlocation.comm;

import com.carlocation.comm.messaging.Notification;

/**
 * 
 * @author 28851274
 *
 */
public interface NotificationListener {

	
	/**
	 * Unsolicited message notification.
	 * @param noti
	 */
	public void onNotify(Notification noti);
}
