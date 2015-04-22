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
    /**
     * Native Service
     */
    private IMessageService mNativeService;

    /**
     * Used for connect native service
     */
    private ServiceConnection mServiceConnection;

    /**
     * Flag used for indicate if service is still on
     */
    private boolean mBound;

    @Override
	public void onCreate() {
		super.onCreate();

        //Bind Native Service and Retrieve service
        Intent serviceIntent = new Intent("com.carlocation.comm.message.service");
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mNativeService = (IMessageService)service;
                mBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mBound = false;
            }
        };

        boolean bindOK;
        do {
            bindOK = this.bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            if (!bindOK){
                //TODO pop up toast to indicate User and try again
            }
        }while (!bindOK);
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
        //Unbind the Native Service
        if(mBound){
            unbindService(mServiceConnection);
            mBound = false;
        }
	}

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }



    public IMessageService getService(){
        return mNativeService;
    }


}
