package com.carlocation.comm;

import java.io.IOException;
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

import com.carlocation.comm.messaging.AuthMessage;
import com.carlocation.comm.messaging.Message;
import com.carlocation.comm.messaging.MessageType;
import com.carlocation.comm.messaging.Notification;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Communication service.<br>
 * <ul>
 * Start service:
 * </ul>
 * <ul>
 * When Service destroyed, will send broadcast:
 * com.carlocation/com.carlocation.MessageServiceDown
 * </ul>
 * <ul>
 * Must use permission for this service
 * {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/> }
 * .<br>
 * If connection state changed, will send broadcast: category:
 * com.carlocation/com.carlocation.connection_state_changed with state code.
 * </ul>
 * 
 * @see ConnectionState
 * @author 28851274
 * 
 */
public class MessageService extends Service {

	private static final String TAG = "CarMessageService";

	/**
	 * 
	 */
	public static final String EXTRA_CONNECTION_STATE = "state";

	public static final String EXTRA_CONNECTION_SERVER_ADDR = "server_addr";

	public static final String EXTRA_CONNECTION_SERVER_PORT = "server_port";

	private String mServer;

	private int mPort;

	private String mUserName;

	private String mPassword;

	private ConnectionFactory connectionFactory;

	private Connection mConnection;

	private Channel mSendChannel;

	private List<NotificationListener> notificationListeners = new ArrayList<NotificationListener>();

	private Map<Message, TimeStamp> mPendingResponse = new HashMap<Message, TimeStamp>();

	private NativeService mService;

	private boolean mIsConnected;

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
		// TODO check server configuration

		// TODO start back-end thread for send or listen server message

		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mService = null;
		notificationListeners.clear();
		dispose();

		// TODO remove all PendingResponse

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

			/*
			 * if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
			 * ConnectivityManager connMgr = (ConnectivityManager) context
			 * .getSystemService(Context.CONNECTIVITY_SERVICE);
			 * 
			 * android.net.NetworkInfo wifi = connMgr
			 * .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			 * 
			 * android.net.NetworkInfo mobile = connMgr
			 * .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			 * 
			 * //TODO if network changed need to re connect to server }
			 */
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
			Log.e(TAG, "get message:" + message);
			if (message.getMessageType() == MessageType.AUTH_MESSAGE) {
				AuthMessage am = (AuthMessage) message;
				mSendChannel = getChannel(mServer, mPort, am.getUserName(),
						am.getPassword());
				TimeStamp ts = mPendingResponse.get(message);
				if (mSendChannel != null && mSendChannel.isOpen()) {
					mUserName = am.getUserName();
					mPassword = am.getPassword();
					if (ts != null) {
						ts.listener.onResponse(new Notification(message,
								Notification.NotificationType.RESPONSE,
								Notification.Result.SUCCESS));
					}
				} else {
					if (ts != null) {
						ts.listener.onResponse(new Notification(message,
								Notification.NotificationType.RESPONSE,
								Notification.Result.FAILED));
					}
				}
			} else {

			}
		}

		@Override
		public void sendMessage(Message message, ResponseListener listener) {
			if (message == null) {
				throw new RuntimeException("message is null");
			}
			if (listener != null) {
				mPendingResponse.put(message, new TimeStamp(message, listener));
			} else {
				Log.w(TAG, message + "  response listener is null");
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

	private Channel getChannel() {
		return getChannel(mServer, mPort, mUserName, mPassword);
	}

	private Channel getChannel(String server, int port, String username,
			String password) {
		if (connectionFactory == null) {
			connectionFactory = new ConnectionFactory();
			connectionFactory.setHost(server);
			connectionFactory.setUsername(username);
			connectionFactory.setPassword(password);
			connectionFactory.setPort(port);
		}
		try {
			if (mConnection == null || !mConnection.isOpen()) {
				mConnection = connectionFactory.newConnection();
				mSendChannel = mConnection.createChannel();
			}
		} catch (IOException e) {
			if (mConnection != null) {
				try {
					mConnection.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			Log.e(TAG, "Connection close failed", e);
			return null;
		}

		if (mSendChannel == null || !mSendChannel.isOpen()) {
			try {
				mSendChannel = mConnection.createChannel();
			} catch (IOException e) {
				Log.e(TAG, "Create channel failed", e);
			}
		}
		return mSendChannel;
	}

	private void dispose() {

		// TODO notify consumer thread

		if (mSendChannel != null && mSendChannel.isOpen()) {
			try {
				mSendChannel.close();
			} catch (IOException e) {
				Log.e(TAG, "Send Channel close failed", e);
			}
		}

		if (mConnection != null && mConnection.isOpen()) {
			try {
				mConnection.close();
			} catch (IOException e) {
				Log.e(TAG, "Connection close failed", e);
			}
		}

	}

}
