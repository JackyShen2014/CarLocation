package com.carlocation.view;

import com.carlocation.R;
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
import android.util.Log;
import android.widget.Toast;

public class CarLocationApplication extends Application {

    private final String LOG_TAG = "CarLocationApplication";

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
                Log.d(LOG_TAG,"onCreate(): Native Service has been retrieved!");
                mBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mBound = false;
            }
        };

        boolean bindOK;
        byte bindTimes = 3;
        do {
            bindOK = this.bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            bindTimes--;
            if (!bindOK){
                Log.e(LOG_TAG,"onCreate():bind Native service failed!");
                //Pop up toast to indicate User and try again
                //If failed to bind service, then pop up left try times and sleep 3s for another bind.
                String strBindFail = getResources().getText(R.string.info_bindServiceFail).toString();
                Toast.makeText(CarLocationApplication.this,strBindFail + bindTimes , Toast.LENGTH_SHORT)
                        .show();
                try {
                    Thread.sleep(3000);
                }catch(InterruptedException e){
                    Log.d(LOG_TAG,"onCreate():Sleep is interrupted!");
                    e.printStackTrace();
                }
            }else{
                //Pop up toast to indicate User bind native service successfully
                Log.d(LOG_TAG,"onCreate():bind Native service successfully !");
                Toast.makeText(CarLocationApplication.this, R.string.info_bindServiceOK, Toast.LENGTH_SHORT)
                        .show();
            }
        }while (!bindOK && (bindTimes != 0));
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
        Log.d(LOG_TAG,"getService()...");
        return mNativeService;
    }


}
