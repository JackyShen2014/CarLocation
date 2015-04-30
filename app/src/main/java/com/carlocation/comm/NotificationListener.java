package com.carlocation.comm;

import com.carlocation.comm.messaging.Notification;

/**
 * <ul>Notification listener, used to notify unsolicited message.</ul>
 * @author 28851274
 *
 */
public interface NotificationListener {

	
	/**
	 * Unsolicited message notification.<br>
	 * Note: please make sure do not update UI in this function, 
	 *    because this function doesn't run in UI thread. 
	 * @param noti
	 */
	public void onNotify(Notification noti);
}
