package com.carlocation.comm;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.carlocation.comm.messaging.AuthMessage;
import com.carlocation.comm.messaging.BaseMessage;
import com.carlocation.comm.messaging.MessageFactory;
import com.carlocation.comm.messaging.MessageHeader;
import com.carlocation.comm.messaging.MessageType;
import com.carlocation.comm.messaging.Notification;
import com.carlocation.comm.messaging.Notification.Result;
import com.carlocation.comm.messaging.ResponseMessage;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AuthenticationFailureException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.ExceptionHandler;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.TopologyRecoveryException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * Must use permission for this service:<br>
 * {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/> }
 * <br>
 * {@code <uses-permission android:name="android.permission.INTERNET"/> }<br>
 * If connection state changed, will send broadcast: category:
 * com.carlocation/com.carlocation.connection_state_changed with state code.
 * </ul>
 * 
 * <ul>
 * Important: First time start service need to input parameters:<br>
 * {@link EXTRA_CONNECTION_SERVER_ADDR} : server address<br>
 * {@link EXTRA_CONNECTION_SERVER_PORT} : server port<br>
 * {@LINK EXTRA_MESSAGE_SERVER_AUTH_REQUIRED} : Use user name and
 * password to pass message server authentication<br>
 * {@LINK EXTRA_SWITCH_SERVER_FLAG} : Switch server flag<br>
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

	private static final int TIME_OUT = 3000;

	public static final String BROADCAST_CATEGORY = "com.carlocation";
	public static final String BROADCAST_ACTION_STATE_CHANGED = "com.carlocation.connection_state_changed";

	/**
	 * Use to as parameter in broadcast
	 */
	public static final String EXTRA_CONNECTION_STATE = "state";

	/**
	 * Server address
	 */
	public static final String EXTRA_CONNECTION_SERVER_ADDR = "server_addr";

	/**
	 * Server port
	 */
	public static final String EXTRA_CONNECTION_SERVER_PORT = "server_port";

	/**
	 * This flag to indicate connect server with user name and password or not.
	 */
	public static final String EXTRA_MESSAGE_SERVER_AUTH_REQUIRED = "server_auth_required";

	/**
	 * Flag to indicate it's necessary to switch new server
	 */
	public static final String EXTRA_SWITCH_SERVER_FLAG = "server_switch";

	private String mServer;

	private int mPort;

	private String mUserName;

	private String mPassword;

	private boolean mServerAuthRequired = true;

	private ConnectionFactory connectionFactory;

	private Connection mSenderConnection;

	private Channel mSenderChannel;

	private Connection mReciverConnection;

	private Channel mReceiverChannel;

	private List<NotificationListener> notificationListeners = new ArrayList<NotificationListener>();

	private Map<BaseMessage, TimeStamp> mPendingResponse = new HashMap<BaseMessage, TimeStamp>();

	private NativeService mService;

	private boolean mIsAuthed;

	private ConsumerThread mConsumer;

	private NetworkState mNS = NetworkState.NONE;

	private Handler mLocalHandler;

	private HandlerThread mLocalHandlerThread;

	@Override
	public void onCreate() {
		super.onCreate();
		mLocalHandlerThread = new HandlerThread("MSHandlerThread");
		mLocalHandlerThread.start();
		int count = 1;
		synchronized (mLocalHandlerThread) {
			while (!mLocalHandlerThread.isAlive() && count++ < 5) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if (mLocalHandlerThread.getLooper() != null) {
			mLocalHandler = new Handler(mLocalHandlerThread.getLooper());
		} else {
			// TODO add log
		}

		mService = new NativeService();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		registerReceiver(mConnectionReceiver, filter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (setupEnv(intent)) {
			return super.onStartCommand(intent, flags, startId);
		} else {
			return START_STICKY;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mLocalHandlerThread.quit();

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
		if (setupEnv(intent)) {
			return mService;
		} else {
			return null;
		}
	}

	/**
	 * Set up connect server configuration
	 * 
	 * @param intent
	 * @return
	 */
	private boolean setupEnv(Intent intent) {
		mServerAuthRequired = intent.getBooleanExtra(
				EXTRA_MESSAGE_SERVER_AUTH_REQUIRED, true);
		String server = intent.getStringExtra(EXTRA_CONNECTION_SERVER_ADDR);
		int port = intent.getIntExtra(EXTRA_CONNECTION_SERVER_PORT, -1);
		boolean switchServer = intent.getBooleanExtra(EXTRA_SWITCH_SERVER_FLAG,
				false);
		if (!TextUtils.isEmpty(server) && port != -1) {
			mServer = server;
			mPort = port;
			// switch server if requested
			if (switchServer) {
				reconnect();
			}
			return true;
		} else {
			return false;
		}

		

	}

	/**
	 * Create new connection according to parameters
	 * 
	 * @param server
	 * @param port
	 * @param username
	 * @param password
	 * @return null means connect failed
	 */
	private Connection newConnection(String server, int port, String username,
			String password) throws AuthenticationFailureException {
		if (connectionFactory == null) {
			connectionFactory = new ConnectionFactory();
			connectionFactory.setHost(server);
			connectionFactory.setPort(port);
			connectionFactory.setExceptionHandler(mExceptionHandler);
			Log.i(TAG, "Connection server with auth:" + mServerAuthRequired);
		}
		if (mServerAuthRequired) {
			connectionFactory.setUsername(username);
			connectionFactory.setPassword(password);
		}

		Connection conn = null;
		try {
			conn = connectionFactory.newConnection();
		} catch (AuthenticationFailureException ae) {
			throw ae;
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Connection close failed", e);
		}
		return conn;
	}

	/**
	 * Get receiver channel, if doesn't create channel yet, create one.
	 * 
	 * @return
	 */
	private Channel getReceiverChannel() {
		if (mReceiverChannel == null || !mReceiverChannel.isOpen()) {
			return getReceiverChannel(mServer, mPort, mUserName, mPassword);
		} else {
			return mReceiverChannel;
		}
	}

	/**
	 * Get receiver channel, if doesn't create channel yet, create one.
	 * 
	 * @param server
	 * @param port
	 * @param username
	 * @param password
	 * @return
	 */
	private Channel getReceiverChannel(String server, int port,
			String username, String password) {
		if (mReceiverChannel != null && mReceiverChannel.isOpen()) {
			return mReceiverChannel;
		}

		boolean opened = false;
		int count = 1;
		while (count++ < 5) {
			try {
				mReciverConnection = newConnection(server, port, username,
						password);
			} catch (AuthenticationFailureException ae) {
				Log.e(TAG, "Create receiver User name or password incorrect",
						ae);
				break;
			} catch (Exception e) {
				Log.e(TAG, "Create receiver connection failed", e);
			}
			Log.i(TAG, "Try to open new connection for receiver : "
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
			Log.i(TAG, "Try to open new channel for receiver : "
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

	/**
	 * Get sender channel, if doesn't create channel yet, create one.
	 * 
	 * @return
	 */
	private Channel getSenderChannel() {
		if (mSenderChannel == null || !mSenderChannel.isOpen()) {
			return getSenderChannel(mServer, mPort, mUserName, mPassword);
		} else {
			return mSenderChannel;
		}
	}

	/**
	 * Get sender channel, if doesn't create channel yet, create one.
	 * 
	 * @param server
	 * @param port
	 * @param username
	 * @param password
	 * @return
	 */
	private Channel getSenderChannel(String server, int port, String username,
			String password) {
		boolean opened = false;

		if (mSenderChannel != null && mSenderChannel.isOpen()) {
			return mSenderChannel;
		}

		int count = 1;
		while (count++ < 5) {
			try {
				mSenderConnection = newConnection(server, port, username,
						password);
				if (mSenderConnection != null && mSenderConnection.isOpen()) {
					opened = true;
					break;
				}
			} catch (AuthenticationFailureException ae) {
				Log.e(TAG, "Create sender User name or password incorrect", ae);
				break;
			} catch (Exception e) {
				Log.e(TAG, "Create sender connection failed", e);
			}
			Log.i(TAG, "Try to open new connection for sender : "
					+ mSenderConnection);
			
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
			Log.i(TAG, "Try to open new channel for sender : "
					+ mSenderChannel);
		}

		if (mIsAuthed) {
			broadcastConnectState(ConnectionState.CONNECTED);
		}
		

		return mSenderChannel;
	}

	/**
	 * Send message back to listener
	 * 
	 * @param msg
	 */
	private boolean fireBackMessage(BaseMessage msg, Notification.Result res) {
		TimeStamp ts = mPendingResponse.remove(msg);
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
	private void fireBackMessage(ResponseMessage msg) {
		TimeStamp ts = mPendingResponse.remove(msg.message);

		if (ts != null) {
			ts.listener.onResponse(new Notification(msg.message,
					Notification.NotificationType.RESPONSE, Result.SUCCESS));
			return;
		}
	}

	/**
	 * Send message back to listener
	 * 
	 * @param msg
	 */
	private void fireMessage(BaseMessage msg) {
		TimeStamp ts = mPendingResponse.remove(msg);

		if (ts != null) {
			ts.listener.onResponse(new Notification(msg,
					Notification.NotificationType.RESPONSE, Result.SUCCESS));
			return;
		}

		Notification notif = new Notification(msg,
				Notification.NotificationType.UNSOLICITED, Result.SUCCESS);

		for (NotificationListener listener : notificationListeners) {
			listener.onNotify(notif);
		}

	}

	/**
	 * Dispose old connection and create new connection
	 */
	private void reconnect() {
		dispose();
		connect();
	}

	/**
	 * 
	 */
	private void connect() {
		getSenderChannel();
		if (this.mSenderChannel != null) {
			
			mConsumer = new ConsumerThread(getReceiverChannel());
			mConsumer.start();
		} else {
			mIsAuthed = false;
			Log.e(TAG, "Can not connect to new server:" + mServer + " port:"
					+ mPort +" username:" +mUserName+" pwd:"+mPassword);
			return;
		}
		
		getReceiverChannel();
	}

	private void broadcastConnectState(ConnectionState state) {
		Intent i = new Intent(BROADCAST_ACTION_STATE_CHANGED);
		i.addCategory(BROADCAST_CATEGORY);
		i.putExtra(EXTRA_CONNECTION_STATE, state);
		this.sendBroadcast(i);
		Log.e(TAG, "Send broadcast with state:" + state);
	}

	private void dispose() {

		if (mConsumer != null) {
			mConsumer.stopListener();
		}

		if (mSenderChannel != null && mSenderChannel.isOpen()) {
			try {
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

		if (mReceiverChannel != null && mReceiverChannel.isOpen()) {
			try {
				mReceiverChannel.queueUnbind(mUserName, EXCHANGE_NAME_DIRECT,
						mUserName);
				mReceiverChannel.queueUnbind(mUserName, EXCHANGE_NAME_FANOUT,
						mUserName);
				mReceiverChannel.queueUnbind(mUserName, EXCHANGE_NAME_TOPIC,
						mUserName);
				
				mReceiverChannel.close();
			} catch (IOException e) {
				Log.e(TAG, "Send Channel close failed", e);
			}
		}

		if (mReciverConnection != null && mReciverConnection.isOpen()) {
			try {
				mReciverConnection.close();
			} catch (IOException e) {
				Log.e(TAG, "Connection close failed", e);
			}
		}

		connectionFactory = null;

		mSenderChannel = null;
		mSenderConnection = null;
		mReceiverChannel = null;
		mReciverConnection = null;
		mConsumer = null;

	}

	/**
	 * Remote message listener thread
	 * 
	 * @author 28851274
	 * 
	 */
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
				ch.basicConsume(mUserName, true, consumer);

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
					String mj = new String(d.getBody());
					MessageHeader header = MessageFactory.parserHeader(mj);
					Log.i(TAG, "Get message " + mj);
					if (header.body == null) {
						continue;
					}
					if (header.type == MessageHeader.HeaderType.REQUEST
							.ordinal()) {
						fireMessage(MessageFactory
								.parseRequestFromJSON(header.body));
					} else {
						fireBackMessage(MessageFactory
								.parseResponseFromJSON(header.body));
					}
				} catch (ShutdownSignalException e) {
					e.printStackTrace();
					return;
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

	private ExceptionHandler mExceptionHandler = new ExceptionHandler() {

		@Override
		public void handleBlockedListenerException(Connection conn, Throwable t) {
			// TODO Auto-generated method stub
			broadcastConnectState(ConnectionState.CONNECT_FAILED);

		}

		@Override
		public void handleChannelRecoveryException(Channel ch, Throwable t) {
			broadcastConnectState(ConnectionState.CONNECT_FAILED);
		}

		@Override
		public void handleConfirmListenerException(Channel ch, Throwable t) {
			// TODO Auto-generated method stub
			broadcastConnectState(ConnectionState.CONNECT_FAILED);

		}

		@Override
		public void handleConnectionRecoveryException(Connection conn,
				Throwable t) {
			broadcastConnectState(ConnectionState.CONNECT_FAILED);
		}

		@Override
		public void handleConsumerException(Channel ch, Throwable t,
				Consumer arg2, String arg3, String arg4) {
			// TODO Auto-generated method stub
			broadcastConnectState(ConnectionState.CONNECT_FAILED);

		}

		@Override
		public void handleFlowListenerException(Channel ch, Throwable t) {
			// TODO Auto-generated method stub
			broadcastConnectState(ConnectionState.CONNECT_FAILED);

		}

		@Override
		public void handleReturnListenerException(Channel ch, Throwable t) {
			// TODO Auto-generated method stub
			broadcastConnectState(ConnectionState.CONNECT_FAILED);

		}

		@Override
		public void handleTopologyRecoveryException(Connection conn,
				Channel ch, TopologyRecoveryException arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void handleUnexpectedConnectionDriverException(Connection ch,
				Throwable t) {
			broadcastConnectState(ConnectionState.CONNECT_FAILED);
		}

	};

	private BroadcastReceiver mConnectionReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			NetworkState newNS = NetworkState.NONE;
			if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)
					|| WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
				ConnectivityManager connMgr = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				android.net.NetworkInfo wifi = connMgr
						.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

				android.net.NetworkInfo mobile = connMgr
						.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				if (mobile != null && mobile.isConnected()) {
					newNS = NetworkState.MOBILE;
				} else if (wifi != null && wifi.isConnected()) {
					newNS = NetworkState.WIFI;
				}
			}

			// If authed, means user already connected to server.
			// But network connection changed, we have to re-connect to server
			if (newNS != mNS) {
				mNS = newNS;
				if (newNS == NetworkState.NONE) {
					dispose();
				} else {
					mLocalHandler.post(new Runnable() {

						@Override
						public void run() {
							reconnect();

						}

					});
				}
			}

		}

	};
	
	
	
	
	class AuthRunnable implements Runnable {

		private AuthMessage message;
		public AuthRunnable(AuthMessage auth) {
			message = auth;
		}
		
		@Override
		public void run() {
			mSenderChannel = getSenderChannel(mServer, mPort, message.mUserName,
					message.mPassword);
			Notification.Result nt = Notification.Result.FAILED;
			if (mSenderChannel != null && mSenderChannel.isOpen()) {
				mUserName = message.mUserName;
				mPassword = message.mPassword;
				nt = Notification.Result.SUCCESS;
			}
			fireBackMessage(message, nt);

			if (mIsAuthed && (mConsumer == null || !mConsumer.isAlive())) {
				mConsumer = new ConsumerThread(getReceiverChannel());
				mConsumer.start();
			}
			
		}
		
	};

	class TimeStamp {
        BaseMessage message;
		long timestamp;
		ResponseListener listener;

		public TimeStamp(BaseMessage message, ResponseListener listener) {
			super();
			this.message = message;
			this.listener = listener;
			this.timestamp = System.currentTimeMillis();
		}

	}

	class NativeService extends Binder implements IMessageService {

		@Override
		public void sendMessage(BaseMessage message) {
			// TODO should send message operation under other thread?
			Log.e(TAG, "get message:" + message);

			if (mServer == null || mPort <= 0) {
				Log.e(TAG, "No Available server :" + mServer + "  port:"
						+ mPort);
				fireBackMessage(message, Notification.Result.FAILED);
				return;
			}
			if (message.getMessageType() == MessageType.AUTH_MESSAGE) {
				mLocalHandler.post(new AuthRunnable((AuthMessage)message));
			} else {
				if (!mIsAuthed) {
					fireBackMessage(message, Notification.Result.FAILED);
				} else {
					try {
						// FIXME add send P2P message
						getSenderChannel().basicPublish(
								EXCHANGE_NAME_CONTROLLER, "", null,
								MessageFactory.addHeader(message).getBytes());
					} catch (IOException e) {
						e.printStackTrace();
						fireBackMessage(message, Notification.Result.FAILED);
					}
				}
			}
		}

		@Override
		public void sendMessage(BaseMessage message, ResponseListener listener) {
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
		public void sendMessageResponse(ResponseMessage rm) {
			try {
				// FIXME add send P2P message
				getSenderChannel().basicPublish(EXCHANGE_NAME_CONTROLLER, "",
						null, MessageFactory.addHeader(rm).getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void cancelWaiting(BaseMessage message) {
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

	enum NetworkState {
		NONE, MOBILE, WIFI;
	}

}
