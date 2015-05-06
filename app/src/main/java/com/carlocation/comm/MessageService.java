package com.carlocation.comm;

import java.io.IOException;
import java.lang.ref.WeakReference;
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
import com.carlocation.comm.messaging.IMMessage;
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
import com.rabbitmq.client.impl.DefaultExceptionHandler;

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
 * {@link EXTRA_MESSAGE_SERVER_AUTH_REQUIRED} : Use user name and password to
 * pass message server authentication<br>
 * </ul>
 * <ul>
 * If need to switch new server: start service with flag
 * {@link EXTRA_SWITCH_SERVER_FLAG}
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

	private ConsumerThread mConsumer;

	/**
	 * Use to save unsolicited message listeners
	 */
	private List<WeakReference<NotificationListener>> notificationListeners = new ArrayList<WeakReference<NotificationListener>>();

	/**
	 * Use to save which messages are waiting for response
	 */
	private Map<BaseMessage, TimeStamp> mPendingResponse = new HashMap<BaseMessage, TimeStamp>();

	/**
	 * Local IBinder service
	 */
	private NativeService mService;

	/**
	 * Current device data connection state
	 */
	private NetworkState mNS = NetworkState.NONE;

	/**
	 * Current server connection state
	 */
	private ConnectionState mSCS = ConnectionState.NONE;

	private ConnectivityManager connMgr;

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

		connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		mNS = getConnectivityState();

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
		Log.i(TAG, "#############service destoryed#############");
		clearAllPendingMessage();

		Intent i = new Intent(BROADCAST_ACTION_SERVICE_DOWN);
		i.addCategory(BROADCAST_CATEGORY);
		sendBroadcast(i);
		unregisterReceiver(mConnectionReceiver);

		notificationListeners.clear();
		dispose();
		mLocalHandlerThread.quit();
		mLocalHandler = null;
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
	private Channel getChannel(String server, int port, String username,
			String password) {
		if (mChannel != null && mChannel.isOpen()) {
			return mChannel;
		}

		boolean opened = false;
		int count = 1;
		while (count++ < 5) {
			try {
				mConnection = newConnection(server, port, username, password);
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
			Log.i(TAG, "Try to open new channel for receiver : " + mChannel);
			if (mChannel != null && mChannel.isOpen()) {
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
	 * @param res
	 * @return true send response, false no listener to send
	 */
	private boolean fireBackMessage(BaseMessage msg, Notification.Result res) {
		TimeStamp ts = mPendingResponse.remove(msg);
		if (ts != null) {
			ts.listener.onResponse(new Notification(msg,
					Notification.NotificationType.RESPONSE, res));
			// Remove time out handler
			mLocalHandler.removeCallbacks(ts.timeoutRunnable);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Send message back to listener
	 * 
	 * @param msg
	 * @param res
	 * @return true send response, false no listener to send
	 */
	private boolean fireBackMessage(ResponseMessage msg, Notification.Result res) {
		TimeStamp ts = mPendingResponse.remove(msg.message);

		if (ts != null) {
			ts.listener.onResponse(new Notification(msg.message,
					Notification.NotificationType.RESPONSE, res));
			// Remove time out handler
			mLocalHandler.removeCallbacks(ts.timeoutRunnable);
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
	private void fireUnsolicitedMessage(BaseMessage msg) {
		Notification notif = new Notification(msg,
				Notification.NotificationType.UNSOLICITED, Result.SUCCESS);

		for (WeakReference<NotificationListener> ref : notificationListeners) {
			if (ref.get() != null) {
				ref.get().onNotify(notif);
			}
		}

	}

	private void clearAllPendingMessage() {
		for (Entry<BaseMessage, TimeStamp> entry : mPendingResponse.entrySet()) {
			TimeStamp ts = entry.getValue();
			if (ts != null) {
				ts.listener.onResponse(new Notification(ts.message,
						Notification.NotificationType.RESPONSE, Result.FAILED));
			}
			// Remove time out handler
			mLocalHandler.removeCallbacks(ts.timeoutRunnable);

		}

		mPendingResponse.clear();
	}

	/**
	 * Dispose old connection and create new connection
	 */
	private void reconnect() {
		dispose();
		updateServerConnectionState(ConnectionState.CONNECTING);
		// Re-get channel instance
		if (getChannel() != null) {
			updateServerConnectionState(ConnectionState.CONNECTED);
			// re-start consumer thread
			mConsumer = new ConsumerThread(getChannel());
			mConsumer.start();
		} else {
			updateServerConnectionState(ConnectionState.CONNECT_FAILED);
			Log.e(TAG, "Can not connect to new server:" + mServer + " port:"
					+ mPort + " username:" + mUserName + " pwd:" + mPassword);
			return;
		}
	}

	private void updateServerConnectionState(ConnectionState newState) {
		if (mSCS != newState) {
			Intent i = new Intent(BROADCAST_ACTION_STATE_CHANGED);
			i.addCategory(BROADCAST_CATEGORY);
			i.putExtra(EXTRA_CONNECTION_STATE, newState);
			sendBroadcast(i);
			Log.e(TAG, "Send broadcast with state:" + newState);

			mSCS = newState;
		}
	}

	/**
	 * Dispose call connection
	 */
	private void dispose() {

		if (mConsumer != null) {
			mConsumer.stopListener();
		}
		if (mChannel != null && mChannel.isOpen()) {
			try {
				mChannel.basicCancel(mUserName);
			} catch (Exception e1) {
				Log.e(TAG, "Cancel consumer :" + mUserName + " failed", e1);
			}
			try {
				mChannel.queueUnbind(mUserName, EXCHANGE_NAME_DIRECT, mUserName);
			} catch (Exception e1) {
				Log.e(TAG, "Unbind queue:" + EXCHANGE_NAME_DIRECT + " failed",
						e1);
			}

			try {
				mChannel.queueUnbind(mUserName, EXCHANGE_NAME_FANOUT, mUserName);
			} catch (Exception e1) {
				Log.e(TAG, "Unbind queue:" + EXCHANGE_NAME_FANOUT + " failed",
						e1);
			}

			try {
				mChannel.queueUnbind(mUserName, EXCHANGE_NAME_TOPIC, "*."
						+ mUserName + ".*");
			} catch (Exception e) {
				Log.e(TAG, "Unbind queue:" + EXCHANGE_NAME_TOPIC + " failed", e);
			}

			try {
				mChannel.close();
			} catch (Exception e) {
				Log.e(TAG, "Send Channel close failed", e);
			}
		}

		if (mConnection != null && mConnection.isOpen()) {
			try {
				mConnection.close();
			} catch (Exception e) {
				Log.e(TAG, "Connection close failed", e);
			}
		}

		// connectionFactory = null;

		mChannel = null;
		mConnection = null;
		mConsumer = null;
		// Update server connection state to none
		updateServerConnectionState(ConnectionState.NONE);

	}

	private NetworkState getConnectivityState() {
		android.net.NetworkInfo wifi = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkState newNS = NetworkState.NONE;

		android.net.NetworkInfo mobile = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mobile != null && mobile.isConnected()) {
			newNS = NetworkState.MOBILE;
		} else if (wifi != null && wifi.isConnected()) {
			newNS = NetworkState.WIFI;
		}

		return newNS;
	}

	private ExceptionHandler mExceptionHandler = new DefaultExceptionHandler() {

		@Override
		public void handleConsumerException(Channel ch, Throwable t,
				Consumer arg2, String arg3, String arg4) {
			updateServerConnectionState(ConnectionState.CONNECT_FAILED);

		}

		@Override
		public void handleUnexpectedConnectionDriverException(Connection ch,
				Throwable t) {
			updateServerConnectionState(ConnectionState.CONNECT_FAILED);
		}

	};

	private BroadcastReceiver mConnectionReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)
					|| WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {

				NetworkState newNS = getConnectivityState();
				
				Log.e(TAG, " network state changed [old:" + mNS + "  new:"
						+ newNS + "] ");
				if (newNS != mNS) {
					if (newNS == NetworkState.NONE) {
						mLocalHandler.post(mDisposeRunnable);
					} else {
						mLocalHandler.post(mReconnectRunnable);
					}
					mNS = newNS;
				}
			}

		}

	};

	private Runnable mDisposeRunnable = new Runnable() {

		@Override
		public void run() {
			Log.e(TAG, " request to dispose!");
			dispose();
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
				AMQP.Queue.DeclareOk deok = ch.queueDeclare(mUserName, true,
						false, false, null);
				Log.i(TAG, "Declare queue:" + deok.getQueue()
						+ "  consume count:" + deok.getConsumerCount()
						+ "  msg count:" + deok.getMessageCount());

				AMQP.Queue.BindOk blok = ch.queueBind(mUserName,
						EXCHANGE_NAME_DIRECT, mUserName);
				Log.i(TAG, "Bound direct queue  to:" + blok);
				blok = ch.queueBind(mUserName, EXCHANGE_NAME_TOPIC, "*."+mUserName+".*");
				Log.i(TAG, "Bound fanout queue  to:" + blok);
				blok = ch.queueBind(mUserName, EXCHANGE_NAME_FANOUT, "");
				Log.i(TAG, "Bound topic queue  to:" + blok);

				ch.basicConsume(mUserName, true, mUserName, consumer);

			} catch (IOException e) {
				updateServerConnectionState(ConnectionState.SERVER_REJECT);
				Log.e(TAG, " bind queue error", e);
				// Bind queue failed, or add consumer failed. release all
				// connections
				dispose();
				return;
			}

			while (isLooping) {
				try {
					Delivery d = consumer.nextDelivery();
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
						BaseMessage bm = MessageFactory
								.parseRequestFromJSON(header.body);
						// If send back message success, means caller waiting
						// for this response.
						// Others no caller waiting for this response, it's
						// unsolicited message.
						if (!fireBackMessage(bm, Notification.Result.SUCCESS)) {
							fireUnsolicitedMessage(bm);
						}
					} else {
						fireBackMessage(
								MessageFactory
										.parseResponseFromJSON(header.body),
								Notification.Result.SUCCESS);
					}
					// Send acknowledge
					getChannel().basicAck(d.getEnvelope().getDeliveryTag(),
							false);
				} catch (ShutdownSignalException e) {
					Log.e(TAG, " Shutdown signal exception", e);
					// TODO really need to re-connect?
					if (mNS != NetworkState.NONE
							&& mSCS == ConnectionState.CONNECTED) {
						mLocalHandler.postDelayed(mReconnectRunnable, 1000);
					}
					break;
				} catch (ConsumerCancelledException e) {
					Log.e(TAG, " Consumer is cancelled ", e);
					break;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			Log.i(TAG, "Message Service listener exit!");

		}

		public void stopListener() {
			Log.i(TAG, "request consumer thread quit");
			isLooping = false;
			this.interrupt();
		}

	}

	/**
	 * Used to send message to server
	 * 
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
			if (mSCS != ConnectionState.CONNECTED) {
				fireBackMessage(message, Notification.Result.NO_CONNECTION);
			} else {
				try {
					// Print out the json format of sending msg
					Log.d(TAG,
							"JSON format: " + MessageFactory.addHeader(message));
					byte[] contents = MessageFactory.addHeader(message).getBytes();
					//FIXME how to send IM message to PC?
					//FIXME terminal id is not username how to exchange
					if (message.getMessageType() == MessageType.IM_MESSAGE) {
						IMMessage im = (IMMessage)message;
						//send broadcast 
						if (im.mToTerminalId == null || im.mToTerminalId.size() == 0) {
							getChannel().basicPublish(EXCHANGE_NAME_FANOUT, "",
									null, contents);
						//send direct message
						} else if (im.mToTerminalId != null && im.mToTerminalId.size() == 1) {
							getChannel().basicPublish(EXCHANGE_NAME_DIRECT, "",
									null, contents);
						} else {
							StringBuilder routekey = new StringBuilder(250);
							List<Long> ids = im.mToTerminalId;
							for (Long id : ids) {
								routekey.append(id).append(".");
								//If routekey greater than 200 than send message
								// because rabbitmq routekey of topic limit to 255
								if (routekey.length() > 200) {
									//Send multicast message
									getChannel().basicPublish(EXCHANGE_NAME_TOPIC, routekey.toString(),
											null, contents);
									routekey = new StringBuilder(250);
								}
							}
							
							if (routekey.length() > 0) {
								getChannel().basicPublish(EXCHANGE_NAME_TOPIC, routekey.toString(),
										null, contents);
							}
							
						}
						
					} else {
						getChannel().basicPublish(EXCHANGE_NAME_CONTROLLER, "",
							null, contents);
					}
				} catch (IOException e) {
					e.printStackTrace();
					fireBackMessage(message, Notification.Result.FAILED);
				}
			}
		}

	};

	/**
	 * Used to send response message to server
	 * 
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
                // Print out the json format of sending msg
                Log.d(TAG,"JSON format of rsp: " + MessageFactory.addHeader(response));
				getChannel().basicPublish(EXCHANGE_NAME_CONTROLLER, "", null,
						MessageFactory.addHeader(response).getBytes());
			} catch (IOException e) {
				e.printStackTrace();
				fireBackMessage(response, Notification.Result.FAILED);
			}
		}

	};

	/**
	 * Time out runnable handler.
	 * 
	 * @author 28851274
	 * 
	 */
	class TimeoutRunnable implements Runnable {

		private BaseMessage message;

		public TimeoutRunnable(BaseMessage message) {
			this.message = message;
		}

		@Override
		public void run() {
			fireBackMessage(message, Notification.Result.TIME_OUT);
		}

	}

	/**
	 * Use to authenticate with server
	 * 
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
				updateServerConnectionState(ConnectionState.NONE);
				return;
			}
			updateServerConnectionState(ConnectionState.CONNECTING);
			getChannel(mServer, mPort, message.mUserName, message.mPassword);
			Notification.Result nt = Notification.Result.FAILED;
			if (mChannel != null && mChannel.isOpen()) {
				mUserName = message.mUserName;
				mPassword = message.mPassword;
				nt = Notification.Result.SUCCESS;
				updateServerConnectionState(ConnectionState.CONNECTED);
			} else {
				updateServerConnectionState(ConnectionState.CONNECT_FAILED);
			}
			fireBackMessage(message, nt);

			// Start consumer thread if we passed authentication
			if (mSCS == ConnectionState.CONNECTED
					&& (mConsumer == null || !mConsumer.isAlive())) {
				mConsumer = new ConsumerThread(mChannel);
				mConsumer.start();
			}

		}

	};

	class CallbackRunnable implements Runnable {

		private BaseMessage message;
		private ResponseListener listener;

		public CallbackRunnable(BaseMessage message, ResponseListener listener) {
			this.message = message;
			this.listener = listener;
		}

		@Override
		public void run() {
			TimeStamp ts = new TimeStamp(message, listener);
			mPendingResponse.put(message, ts);
			// Start timer for time out
			mLocalHandler.postDelayed(ts.timeoutRunnable, TIME_OUT);
		}

	}

	class TimeStamp {
		BaseMessage message;
		long timestamp;
		ResponseListener listener;
		TimeoutRunnable timeoutRunnable;

		public TimeStamp(BaseMessage message, ResponseListener listener) {
			this.message = message;
			this.listener = listener;
			this.timestamp = System.currentTimeMillis();
			timeoutRunnable = new TimeoutRunnable(message);
		}

	}

	/**
	 * Binder service. Use to for implement interface.
	 * 
	 * @author jiangzhen
	 * 
	 */
	class NativeService extends Binder implements IMessageService {

		@Override
		public void sendMessage(BaseMessage message) {
			Log.i(TAG, "send message:" + message);
			if (mLocalHandler == null) {
				fireBackMessage(message, Notification.Result.SERVICE_DOWN);
				return;
			}
			if (message.getMessageType() == MessageType.AUTH_MESSAGE) {
				if (mSCS != ConnectionState.CONNECTING
						&& mSCS != ConnectionState.CONNECTED) {
					mLocalHandler.post(new AuthRunnable((AuthMessage) message));
				} else {
					fireBackMessage(message, Notification.Result.SUCCESS);
				}
			} else {
				if (mSCS != ConnectionState.CONNECTED) {
					fireBackMessage(message, Notification.Result.NO_CONNECTION);
					return;
				}
				mLocalHandler.post(new SendMessageRunnable(message));
			}
		}

		@Override
		public void sendMessage(BaseMessage message, ResponseListener listener) {
			if (listener != null) {
				// mLocalHandler.post(new CallbackRunnable(message, listener));
				TimeStamp ts = new TimeStamp(message, listener);
				mPendingResponse.put(message, ts);
				// Start timer for time out
				mLocalHandler.postDelayed(ts.timeoutRunnable, TIME_OUT);
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
			if (mLocalHandler == null) {
				fireBackMessage(rm, Notification.Result.SERVICE_DOWN);
				return;
			}
			if (mSCS != ConnectionState.CONNECTED) {
				fireBackMessage(rm, Notification.Result.NO_CONNECTION);
				return;
			}

			mLocalHandler.post(new SendResponseRunnable(rm));
		}

		@Override
		public void cancelWaiting(BaseMessage message) {
			TimeStamp ts = mPendingResponse.remove(message);
			if (ts != null && ts.timeoutRunnable != null) {
				mLocalHandler.removeCallbacks(ts.timeoutRunnable);
			}
		}

		@Override
		public void registerNotificationListener(NotificationListener listener) {
			notificationListeners.add(new WeakReference<NotificationListener>(
					listener));
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
