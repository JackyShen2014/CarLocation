package com.carlocation.comm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.carlocation.comm.messaging.Message;

/**
 * Communication service.<br>
 * <ul>Start service:</ul>
 * <ul>When Service destroyed, will send broadcast: com.carlocation/com.carlocation.MessageServiceDown</ul>
 * <ul>Must use permission for this service {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/> }.<br>
 *     If connection state changed, will send broadcast: category: com.carlocation/com.carlocation.connection_state_changed with state code.
 * </ul>
 * 
 * @see ConnectionState
 * @author 28851274
 *
 */
public class MessageService extends Service {
	
	/**
	 * 
	 */
	public static final String EXTRA_CONNECTION_STATE = "state";

	private static final String TAG = "CarMessageService";
	
	private List<NotificationListener> notificationListeners = new ArrayList<NotificationListener>();
	
	private Map<Message, TimeStamp> mPendingResponse = new HashMap<Message, TimeStamp>();

	private NativeService mService;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mService = new NativeService();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		registerReceiver(mConnectionReceiver, filter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//TODO check server configuration
		
		//TODO start back-end thread for send or listen server message
		
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mService = null;
		notificationListeners.clear();
		//TODO destroy server connection
		
		
		//TODO remove all PendingResponse
		
		//TODO send broadcast for service down
		Intent i = new Intent("com.carlocation.MessageServiceDown");
		i.addCategory("com.carlocation");
		this.sendBroadcast(i);
		this.unregisterReceiver(mConnectionReceiver);
	}


	@Override
	public IBinder onBind(Intent intent) {
		return mService;
	}
	
	
	
	

	private BroadcastReceiver mConnectionReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

            /**
             * Temporary cancel to stop crash.
             */

			/*if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
				ConnectivityManager connMgr = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);

				android.net.NetworkInfo wifi = connMgr
						.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

				android.net.NetworkInfo mobile = connMgr
						.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				
				//TODO if network changed need to re connect to server
			}*/
		}
		
	};
	
	
	
	
	
	class TimeStamp {
		Message message;
		long timestamp;
		ResponseListener listener;
		
		public TimeStamp(Message message, ResponseListener listener) {
			super();
			this.message = message;
			this.listener = listener;
			this.timestamp = System.currentTimeMillis();
		}

		
	}
	
	
	class NativeService extends Binder implements IMessageService {

		
		
		@Override
		public void sendMessage(Message message) {
			Log.e(TAG, "get message:"+ message);
			// TODO Invoke send message.translate to format for network
			// Send message
			
		}

		@Override
		public void sendMessage(Message message, ResponseListener listener) {
			if (message == null) {
				throw new RuntimeException("message is null");
			}
			if (listener != null) {
				mPendingResponse.put(message, new TimeStamp(message,listener));
			} else {
				Log.w(TAG, message +"  response listener is null");
			}
			
			sendMessage(message);
		}

		@Override
		public void cancelWaiting(Message message) {
			mPendingResponse.remove(message);
		}

		@Override
		public void registerNotificationListener(NotificationListener listener) {
			notificationListeners.add(listener);
		}

		@Override
		public void unRegisterNotificationListener(NotificationListener listener) {
			notificationListeners.remove(listener);
		}
		
	}
	
	
	

}
