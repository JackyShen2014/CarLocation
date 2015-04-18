package com.carlocation.view;

import com.carlocation.comm.IMessageService;
import com.carlocation.comm.NotificationListener;
import com.carlocation.comm.messaging.AuthMessage;
import com.carlocation.comm.messaging.Notification;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class CarLocationApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Intent serviceIntent = new Intent("com.carlocation.comm.message.service");
		//TODO ADD server parrameter
		startService(serviceIntent);
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}
	
	

}
