package com.carlocation.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.carlocation.R;
import com.carlocation.comm.IMessageService;
import com.carlocation.comm.MessageService;

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

	// Indicate the Max retry bind times
	private int mBindRetryTimes = 3;

	// Retry bind "what"
	static final int TRY_REBIND = 1;

	private static final String EXTRA_CONNECTION_SERVER_ADDR = "server_addr";

	private static final String EXTRA_CONNECTION_SERVER_PORT = "server_port";

	@Override
	public void onCreate() {
		super.onCreate();

		// Bind Native Service and Retrieve service
		Intent serviceIntent = new Intent();
		serviceIntent.setClass(getApplicationContext(), MessageService.class);

		String serverAddr = null;
		int port = -1;
				
		try {
			InputStream in = this.getAssets().open("server.config");
			Properties prop = new Properties();
			prop.load(in);
			serverAddr = prop.getProperty("server");
			port = Integer.parseInt(prop.getProperty("port"));
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		//TODO handle if can not get server addr
		serviceIntent.putExtra(EXTRA_CONNECTION_SERVER_ADDR, serverAddr);
		serviceIntent.putExtra(EXTRA_CONNECTION_SERVER_PORT, port);

		mServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mNativeService = (IMessageService) service;
				Log.d(LOG_TAG, "onCreate(): Native Service has been retrieved!");
				mBound = true;
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				mBound = false;
			}
		};

		boolean bindOK;

		bindOK = this.bindService(serviceIntent, mServiceConnection,
				Context.BIND_AUTO_CREATE);
		if (!bindOK) {
			Log.e(LOG_TAG, "onCreate():bind Native service failed!");
			// Pop up toast to indicate User and try again
			// If failed to bind service, then pop up left try times and sleep
			// 200ms for another try.
			String strBindFail = getResources().getText(
					R.string.info_bindServiceFail).toString();
			Toast.makeText(CarLocationApplication.this,
					strBindFail + mBindRetryTimes, Toast.LENGTH_SHORT).show();

			Message m = Message.obtain(h, TRY_REBIND);
			h.sendMessageDelayed(m, 200);
		} else {
			// Pop up toast to indicate User bind native service successfully
			Log.d(LOG_TAG, "onCreate():bind Native service successfully !");
			Toast.makeText(CarLocationApplication.this,
					R.string.info_bindServiceOK, Toast.LENGTH_SHORT).show();
		}

	}

	private Handler h = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TRY_REBIND:
				if (mBindRetryTimes != 0) {
					boolean bindOK;
					Intent serviceIntent = new Intent(
							"com.carlocation.comm.message.service");
					bindOK = bindService(serviceIntent, mServiceConnection,
							Context.BIND_AUTO_CREATE);
					mBindRetryTimes--;

					if (!bindOK) {
						Log.e(LOG_TAG, "onCreate():bind Native service failed!");
						// Pop up toast to indicate User and try again
						// If failed to bind service, then pop up left try times
						// and sleep 200ms for another try.
						String strBindFail = getResources().getText(
								R.string.info_bindServiceFail).toString();
						Toast.makeText(CarLocationApplication.this,
								strBindFail + mBindRetryTimes,
								Toast.LENGTH_SHORT).show();

						Message m = Message.obtain(h, TRY_REBIND);
						h.sendMessageDelayed(m, 200);

					} else {
						// Pop up toast to indicate User bind native service
						// successfully
						Log.d(LOG_TAG,
								"onCreate():bind Native service successfully !");
						Toast.makeText(CarLocationApplication.this,
								R.string.info_bindServiceOK, Toast.LENGTH_SHORT)
								.show();
					}
				}
				break;
			case 2:
				break;
			}
		}

	};

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
		// Unbind the Native Service
		if (mBound) {
			unbindService(mServiceConnection);
			mBound = false;
		}
	}

	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN
				|| level == ComponentCallbacks2.TRIM_MEMORY_COMPLETE) {
			unbindService(mServiceConnection);
			mBound = false;
		}
	}

	public IMessageService getService() {
		Log.d(LOG_TAG, "getService()...");
		return mNativeService;
	}

}
