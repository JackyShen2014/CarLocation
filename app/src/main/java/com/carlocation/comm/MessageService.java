package com.carlocation.comm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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


	public static final String BROADCAST_CATEGORY = "com.carlocation";
	public static final String BROADCAST_ACTION_STATE_CHANGED = "com.carlocation.connection_state_changed";
	public static final String BROADCAST_ACTION_SERVICE_DOWN = "com.carlocation.MessageServiceDown";

	/**
	 * timer interval
	 */
	private static final int TIME_OUT = 3000;

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

	private Connection mConnection;

	private Channel mChannel;


	private List<NotificationListener> notificationListeners = new ArrayList<NotificationListener>();

	private Map<BaseMessage, TimeStamp> mPendingResponse = new HashMap<BaseMessage, TimeStamp>();

	/**
	 * Local IBinder service
	 */
	private NativeService mService;

	private ConsumerThread mConsumer;

	/**
	 * Current device data connection state
	 */
	private NetworkState mNS = NetworkState.NONE;
	
	/**
	 * Current server connection state
	 */
	private ServerConnectionState mSCS = ServerConnectionState.NONE;

	/**
	 * Local message handler, not in UI thread
	 */
	private Handler mLocalHandler;

	/**
	 * Back-end handler thread
	 */
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
			Log.e(TAG, "Handler thread can not start");
			stopSelf();
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
		Log.e(TAG, " service destoryed ");
		mLocalHandlerThread.quit();
		
		clearAllPendingMessage();
		
		notificationListeners.clear();
		dispose();


		Intent i = new Intent(BROADCAST_ACTION_SERVICE_DOWN);
		i.addCategory(BROADCAST_CATEGORY);
		this.sendBroadcast(i);
		this.unregisterReceiver(mConnectionReceiver);
		
		mService = null;
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
				mLocalHandler.post(mReconnectRunnable);
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
			connectionFactory.setAutomaticRecoveryEnabled(false);
			connectionFactory.setRequestedHeartbeat(5);
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
	private Channel getChannel() {
		if (mChannel == null || !mChannel.isOpen()) {
			mChannel = getChannel(mServer, mPort, mUserName, mPassword);
		} 
		
		return mChannel;
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
	private Channel getChannel(String server, int port,
			String username, String password) {
		if (mChannel != null && mChannel.isOpen()) {
			return mChannel;
		}

		boolean opened = false;
		int count = 1;
		while (count++ < 5) {
			try {
				mConnection = newConnection(server, port, username,
						password);
			} catch (AuthenticationFailureException ae) {
				Log.e(TAG, "Create receiver User name or password incorrect",
						ae);
				break;
			} catch (Exception e) {
				Log.e(TAG, "Create receiver connection failed", e);
			}
			Log.i(TAG, "Try to open new connection for receiver : "
					+ mConnection);
			if (mConnection != null && mConnection.isOpen()) {
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
				mChannel = mConnection.createChannel();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Log.i(TAG, "Try to open new channel for receiver : "
					+ mChannel);
			if (mChannel != null && mChannel.isOpen()) {
				mSCS = ServerConnectionState.CONNECTED; 
				break;
			}
			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return mChannel;

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
	private boolean fireBackMessage(ResponseMessage msg, Notification.Result res) {
		TimeStamp ts = mPendingResponse.remove(msg.message);

		if (ts != null) {
			ts.listener.onResponse(new Notification(msg.message,
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
	
	
	private void clearAllPendingMessage() {
		for (Entry<BaseMessage, TimeStamp> entry :mPendingResponse.entrySet()) {
			TimeStamp ts = entry.getValue();
			if (ts != null) {
				ts.listener.onResponse(new Notification(ts.message,
						Notification.NotificationType.RESPONSE, Result.TIME_OUT));
			}
			
		}
		
		mPendingResponse.clear();
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
		if (getChannel() != null) {
			mConsumer = new ConsumerThread(getChannel());
			mConsumer.start();
		} else {
			mSCS  = ServerConnectionState.NONE;
			Log.e(TAG, "Can not connect to new server:" + mServer + " port:"
					+ mPort + " username:" + mUserName + " pwd:" + mPassword);
			return;
		}
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
			Log.i(TAG, "request consumer thread quit");
			mConsumer.stopListener();
		}



		if (mChannel != null && mChannel.isOpen()) {
			try {
				mChannel.queueUnbind(mUserName, EXCHANGE_NAME_DIRECT,
						mUserName);
				mChannel.queueUnbind(mUserName, EXCHANGE_NAME_FANOUT,
						mUserName);
				mChannel.queueUnbind(mUserName, EXCHANGE_NAME_TOPIC,
						mUserName);

				mChannel.close();
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
 
//		connectionFactory = null;

		mChannel = null;
		mConnection = null;
		mConsumer = null;

	}


	private ExceptionHandler mExceptionHandler = new ExceptionHandler() {

		@Override
		public void handleBlockedListenerException(Connection conn, Throwable t) {
			broadcastConnectState(ConnectionState.CONNECT_FAILED);

		}

		@Override
		public void handleChannelRecoveryException(Channel ch, Throwable t) {
			broadcastConnectState(ConnectionState.CONNECT_FAILED);
		}

		@Override
		public void handleConfirmListenerException(Channel ch, Throwable t) {
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
			broadcastConnectState(ConnectionState.CONNECT_FAILED);

		}

		@Override
		public void handleFlowListenerException(Channel ch, Throwable t) {
			broadcastConnectState(ConnectionState.CONNECT_FAILED);

		}

		@Override
		public void handleReturnListenerException(Channel ch, Throwable t) {
			broadcastConnectState(ConnectionState.CONNECT_FAILED);

		}

		@Override
		public void handleTopologyRecoveryException(Connection conn,
				Channel ch, TopologyRecoveryException arg2) {
			broadcastConnectState(ConnectionState.CONNECT_FAILED);
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
			if (newNS == NetworkState.NONE) {
				Log.e(TAG, " network [old:" +mNS+"  new:" +newNS+"] ");
				dispose();
				mNS = newNS;
			} else if (mNS == NetworkState.NONE){
				mLocalHandler.post(mReconnectRunnable);
				mNS = newNS;
			}
			

		}

	};

	private Runnable mReconnectRunnable = new Runnable() {

		@Override
		public void run() {
			Log.e(TAG, " request to re-connect!");
			reconnect();
		}

	};
	
	
	
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
						false, true, null);
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
				try {
					ch.basicCancel(mUserName);
				} catch (IOException e) {
					
				}
				//ch.setDefaultConsumer(null);
				ch.basicConsume(mUserName, true, mUserName, consumer);

			} catch (IOException e) {
				broadcastConnectState(ConnectionState.SERVER_REJECT);
				Log.e(TAG, " bind queue error", e);
				//Doesn't bind queue failed, release all connections
				dispose();
				return;
			}

			while (isLooping) {
				try {
					Delivery d = consumer.nextDelivery();
					consumer.handleConsumeOk("");
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
						fireBackMessage(
								MessageFactory
										.parseResponseFromJSON(header.body),
								Notification.Result.SUCCESS);
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

	
	/**
	 * Used to send message to server
	 * @author jiangzhen
	 *
	 */
	class SendMessageRunnable implements Runnable {

		private BaseMessage message;

		public SendMessageRunnable(BaseMessage message) {
			this.message = message;
		}

		@Override
		public void run() {
			if (mSCS  != ServerConnectionState.CONNECTED) {
				fireBackMessage(message, Notification.Result.NO_CONNECTION);
			} else {
				try {
					// FIXME add send P2P message
					getChannel().basicPublish(EXCHANGE_NAME_CONTROLLER,
							"", null,
							MessageFactory.addHeader(message).getBytes());
				} catch (IOException e) {
					e.printStackTrace();
					fireBackMessage(message, Notification.Result.FAILED);
				}
			}
		}

	};

	/**
	 * Used to send response message to server
	 * @author jiangzhen
	 *
	 */
	class SendResponseRunnable implements Runnable {

		private ResponseMessage response;

		public SendResponseRunnable(ResponseMessage response) {
			this.response = response;
		}

		@Override
		public void run() {
			try {
				getChannel().basicPublish(EXCHANGE_NAME_CONTROLLER, "",
						null, MessageFactory.addHeader(response).getBytes());
			} catch (IOException e) {
				e.printStackTrace();
				fireBackMessage(response, Notification.Result.FAILED);
			}
		}

	};
	
	

	/**
	 * Use to authenticate with server  
	 * @author jiangzhen
	 *
	 */
	class AuthRunnable implements Runnable {

		private AuthMessage message;

		public AuthRunnable(AuthMessage auth) {
			message = auth;
		}

		@Override
		public void run() {

			if (mServer == null || mPort <= 0) {
				Log.e(TAG, "No Available server :" + mServer + "  port:"
						+ mPort);
				fireBackMessage(message, Notification.Result.FAILED);
				mSCS  = ServerConnectionState.NONE;
				return;
			}
			
			getChannel(mServer, mPort,
					message.mUserName, message.mPassword);
			Notification.Result nt = Notification.Result.FAILED;
			if (mChannel != null && mChannel.isOpen()) {
				mUserName = message.mUserName;
				mPassword = message.mPassword;
				nt = Notification.Result.SUCCESS;
			}
			fireBackMessage(message, nt);

			if (mSCS  == ServerConnectionState.CONNECTED && (mConsumer == null || !mConsumer.isAlive())) {
				mConsumer = new ConsumerThread(mChannel);
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

	
	/**
	 * Binder service. Use to for implement interface.
	 * @author jiangzhen
	 *
	 */
	class NativeService extends Binder implements IMessageService {

		@Override
		public void sendMessage(BaseMessage message) {
			Log.i(TAG, "send message:" + message);
			if (message.getMessageType() == MessageType.AUTH_MESSAGE) {
				mSCS =  ServerConnectionState.CONNECTING;
				mLocalHandler.post(new AuthRunnable((AuthMessage) message));
			} else {
				mLocalHandler.post(new SendMessageRunnable(message));
			}
		}

		@Override
		public void sendMessage(BaseMessage message, ResponseListener listener) {
			if (listener != null) {
				mPendingResponse.put(message, new TimeStamp(message, listener));
			} else {
				Log.w(TAG, message + "  response listener is null");
			}

			sendMessage(message);
		}

		@Override
		public void sendMessageResponse(ResponseMessage rm) {
			Log.i(TAG, "send response:" + rm);
			if (rm == null) {
				throw new RuntimeException("ResponseMessage is null");
			}
			mLocalHandler.post(new SendResponseRunnable(rm));
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
	
	
	enum ServerConnectionState {
		NONE, CONNECTING, CONNECTED
	}

}
