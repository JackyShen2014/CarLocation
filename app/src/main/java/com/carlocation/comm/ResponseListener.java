package com.carlocation.comm;

import android.util.Log;

import com.carlocation.comm.messaging.Notification;

public interface ResponseListener {
	
	public void onResponse(Notification noti);
}
