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
import com.carlocation.comm.messaging.MessageFactory;
import com.carlocation.comm.messaging.MessageType;
import com.carlocation.comm.messaging.Notification;
import com.carlocation.comm.messaging.Notification.Result;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownSignalException;

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

	private static final String EXCHANGE_NAME_DIRECT = "carlocation.client.direct";
	private static final String EXCHANGE_NAME_FANOUT = "carlocation.client.fanout";
	private static final String EXCHANGE_NAME_TOPIC = "carlocation.client.topic";

	private static final String EXCHANGE_NAME_CONTROLLER = "carlocation.reply.fanout";

	private static final int TIME_OUT = 3;

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

	private Connection mSenderConnection;

	private Channel mSenderChannel;

	private Connection mReciverConnection;

	private Channel mReceiverChannel;

	private List<NotificationListener> notificationListeners = new ArrayList<NotificationListener>();

	private Map<Message, TimeStamp> mPendingResponse = new HashMap<Message, TimeStamp>();

	private NativeService mService;

	private boolean mIsAuthed;

	private ConsumerThread mConsumer;

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
			// TODO should send message operation under other thread?
			Log.e(TAG, "get message:" + message);
			if (message.getMessageType() == MessageType.AUTH_MESSAGE) {
				AuthMessage am = (AuthMessage) message;
				mSenderChannel = getSenderChannel(mServer, mPort,
						am.getUserName(), am.getPassword());
				Notification.Result nt = Notification.Result.FAILED;
				if (mSenderChannel != null && mSenderChannel.isOpen()) {
					mUserName = am.getUserName();
					mPassword = am.getPassword();
					nt = Notification.Result.SUCCESS;
				}
				fireBackMessage(message, nt);

				if (mIsAuthed && mConsumer != null) {
					mConsumer = new ConsumerThread(getReceiverChannel());
					mConsumer.start();
				}
			} else {
				try {
					getSenderChannel().basicPublish(EXCHANGE_NAME_CONTROLLER,
							"", null, message.translate().getBytes());
				} catch (IOException e) {
					e.printStackTrace();
					fireBackMessage(message, Notification.Result.FAILED);
				}
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

	private Connection newConnection(String server, int port, String username,
			String password) {
		if (connectionFactory == null) {
			connectionFactory = new ConnectionFactory();
			connectionFactory.setHost(server);
			connectionFactory.setUsername(username);
			connectionFactory.setPassword(password);
			connectionFactory.setPort(port);
		}

		Connection conn = null;
		try {
			conn = connectionFactory.newConnection();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Connection close failed", e);
		}
		return conn;
	}

	private Channel getReceiverChannel() {
		if (!mIsAuthed) {
			Log.e(TAG, "Doesn't auth yet!");
			return null;
		}
		if (mReceiverChannel != null || mReceiverChannel.isOpen()) {
			return mReceiverChannel;
		}

		boolean opened = false;
		int count = 1;
		while ((mReciverConnection == null || !mReciverConnection.isOpen())
				&& count++ < 5) {
			mReciverConnection = newConnection(mServer, mPort, mUserName,
					mPassword);
			Log.e(TAG, "Try to open new connection for receiver : "
					+ mReciverConnection);
			if (mReciverConnection != null && mReciverConnection.isOpen()) {
				opened = true;
				break;
			}
			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		count = 1;
		while (opened && count++ < 5) {
			try {
				mReceiverChannel = mReciverConnection.createChannel();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.e(TAG, "Try to open new channel for receiver : "
					+ mReceiverChannel);
			if (mReceiverChannel != null && mReceiverChannel.isOpen()) {
				break;
			}
			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return mReceiverChannel;

	}

	private Channel getSenderChannel() {
		return getSenderChannel(mServer, mPort, mUserName, mPassword);
	}

	private Channel getSenderChannel(String server, int port, String username,
			String password) {
		boolean opened = true;

		int count = 1;
		while ((mSenderConnection == null || !mSenderConnection.isOpen())
				&& count++ < 5) {
			Log.e(TAG, "Try to open new connection for receiver : "
					+ mSenderConnection);
			mSenderConnection = newConnection(server, port, username, password);
			if (mSenderConnection != null && mSenderConnection.isOpen()) {
				opened = true;
				break;
			}
			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		while (opened && count++ < 5) {
			try {
				mSenderChannel = mSenderConnection.createChannel();
				if (mSenderChannel != null && mSenderChannel.isOpen()) {
					mIsAuthed = true;
					break;
				}
			} catch (IOException e) {
				Log.e(TAG, "Create channel failed", e);
			}
			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}


		return mSenderChannel;
	}

	/**
	 * Send message back to listener
	 * 
	 * @param msg
	 */
	private boolean fireBackMessage(Message msg, Notification.Result res) {
		TimeStamp ts = mPendingResponse.get(msg);
		if (ts != null) {
			ts.listener.onResponse(new Notification(msg,
					Notification.NotificationType.RESPONSE, res));
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Send message back to listener
	 * 
	 * @param msg
	 */
	private void fireMessage(Message msg) {
		TimeStamp ts = mPendingResponse.get(msg);

		if (ts != null) {
			ts.listener.onResponse(new Notification(msg,
					Notification.NotificationType.RESPONSE, Result.SUCCESS));
		}

		Notification notif = new Notification(msg,
				Notification.NotificationType.UNSOLICITED, Result.SUCCESS);

		for (NotificationListener listener : notificationListeners) {
			listener.onNotify(notif);
		}

	}

	private void dispose() {

		mConsumer.stopListener();

		if (mSenderChannel != null && mSenderChannel.isOpen()) {
			try {
				mSenderChannel.queueUnbind(mUserName, EXCHANGE_NAME_DIRECT,
						mUserName);
				mSenderChannel.close();
			} catch (IOException e) {
				Log.e(TAG, "Send Channel close failed", e);
			}
		}

		if (mSenderConnection != null && mSenderConnection.isOpen()) {
			try {
				mSenderConnection.close();
			} catch (IOException e) {
				Log.e(TAG, "Connection close failed", e);
			}
		}

	}

	class ConsumerThread extends Thread {
		private Channel ch;
		private QueueingConsumer consumer;
		private boolean isLooping = true;;

		public ConsumerThread(Channel ch) {
			super();
			this.ch = ch;
			consumer = new QueueingConsumer(ch);
		}

		@Override
		public void run() {
			try {
				AMQP.Queue.DeclareOk deok = ch.queueDeclare(mUserName, false,
						false, false, null);
				Log.i(TAG, "Declare queue:" + deok.getQueue()
						+ "  consume count:" + deok.getConsumerCount()
						+ "  msg count:" + deok.getMessageCount());

				AMQP.Queue.BindOk blok = ch.queueBind(mUserName,
						EXCHANGE_NAME_DIRECT, mUserName);
				Log.i(TAG, "Bound direct queue  to:" + blok);
				blok = ch.queueBind(mUserName, EXCHANGE_NAME_TOPIC, "");
				Log.i(TAG, "Bound fanout queue  to:" + blok);
				blok = ch.queueBind(mUserName, EXCHANGE_NAME_FANOUT, "");
				Log.i(TAG, "Bound topic queue  to:" + blok);
				ch.basicConsume(mUserName, consumer);

			} catch (IOException e) {
				e.printStackTrace();
			}

			while (isLooping) {
				Log.i(TAG, "Start to waiting....");
				try {
					Delivery d = consumer.nextDelivery(TIME_OUT);
					if (d == null) {
						continue;
					}
					Message msg = MessageFactory.parseFromJSON(new String(d
							.getBody()));
					fireMessage(msg);
				} catch (ShutdownSignalException e) {
					e.printStackTrace();
				} catch (ConsumerCancelledException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			Log.i(TAG, "Message Service listener exit!");

		}

		public void stopListener() {
			isLooping = false;
			this.interrupt();
		}

	}

}
