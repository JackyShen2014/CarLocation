package com.carlocation.demo;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.carlocation.R;
import com.carlocation.comm.ConnectionState;
import com.carlocation.comm.IMessageService;
import com.carlocation.comm.MessageService;
import com.carlocation.comm.NativeServInterface;
import com.carlocation.comm.NotificationListener;
import com.carlocation.comm.ResponseListener;
import com.carlocation.comm.messaging.AuthMessage;
import com.carlocation.comm.messaging.IMMessage;
import com.carlocation.comm.messaging.IMTxtMessage;
import com.carlocation.comm.messaging.IMVoiceMessage;
import com.carlocation.comm.messaging.LocationMessage;
import com.carlocation.comm.messaging.MessageType;
import com.carlocation.comm.messaging.Notification;
import com.carlocation.comm.messaging.RankType;
import com.carlocation.comm.messaging.StatusMessage;
import com.carlocation.comm.messaging.TaskAssignmentMessage;
import com.carlocation.view.CarLocationApplication;
import com.carlocation.view.MainActivity;
import com.carlocation.view.UserService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DemoActivity extends ListActivity implements NativeServInterface {
    private final static String LOG_TAG = "DemoActivity";

    /**
     * Notification listener
     */
    private LocalListener mListener;
    /**
     * Native Service
     */
    private IMessageService mNativeService;
    /**
     * User Service
     */
    private UserService mUserService;

    /**
     * Record current connection state
     */
    private ConnectionState mConnState = ConnectionState.NONE;

    /**
     * An indicator to indicate if this MainActivity still alive (not destroyed).
     */
    private boolean mResumed;

    private static List<String> mItems;
    private ListView mList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getUserService();
        regNotifictListenner();

        //"mConnStateReceiver" used to recv connection state notification
        IntentFilter filter = new IntentFilter();
        filter.addAction(MessageService.BROADCAST_ACTION_STATE_CHANGED);
        filter.addCategory(MessageService.BROADCAST_CATEGORY);
        registerReceiver(mConnStateReceiver, filter);

        mList = getListView();
        mItems = new ArrayList<>();

        //Reflect all announced public methods in UserService
        try{
            Method[] methods = UserService.class.getDeclaredMethods();
            for (int i=0;i<methods.length;i++) {
                mItems.add(methods[i].getName());
            }
        }catch (SecurityException e){
            Log.e(LOG_TAG,"onCreate():NoSuchMethodException!");
            e.printStackTrace();
        }

        setListAdapter(new ArrayAdapter<>(DemoActivity.this,android.R.layout.simple_list_item_1,mItems));

        mList.setOnItemClickListener(onClickList);

    }


    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or
     * {@link #onPause}, for your activity to start interacting with the user.
     * This is a good place to begin animations, open exclusive-access devices
     * (such as the camera), etc.
     * <p/>
     * <p>Keep in mind that onResume is not the best indicator that your activity
     * is visible to the user; a system window such as the keyguard may be in
     * front.  Use {@link #onWindowFocusChanged} to know for certain that your
     * activity is visible to the user (for example, to resume a game).
     * <p/>
     * <p><em>Derived classes must call through to the super class's
     * implementation of this method.  If they do not, an exception will be
     * thrown.</em></p>
     *
     * @see #onRestoreInstanceState
     * @see #onRestart
     * @see #onPostResume
     * @see #onPause
     */
    @Override
    protected void onResume() {
        super.onResume();
        mResumed = true;
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mResumed = false;
        unregisterReceiver(mConnStateReceiver);
        (((CarLocationApplication) getApplicationContext()).getService()).unRegisterNotificationListener(this.mListener);
    }

    protected AdapterView.OnItemClickListener onClickList = new AdapterView.OnItemClickListener(){

        /**
         * Callback method to be invoked when an item in this AdapterView has
         * been clicked.
         * <p/>
         * Implementers can call getItemAtPosition(position) if they need
         * to access the data associated with the selected item.
         *
         * @param parent   The AdapterView where the click happened.
         * @param view     The view within the AdapterView that was clicked (this
         *                 will be a view provided by the adapter)
         * @param position The position of the view in the adapter.
         * @param id       The row id of the item that was clicked.
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String item = mItems.get(position);
            if (mConnState.equals(ConnectionState.CONNECTED)){
                List<String> toId = new ArrayList<>();
                String toTerminalId;
                if (UserService.getTerminalId().equals("t1")) {
                    toTerminalId = "t2";
                }else {
                    toTerminalId = "t1";
                }
                toId.add(toTerminalId);

                if (item.equals("getTerminalId")){
                    //getTerminalId()
                    String tId  = UserService.getTerminalId();
                    Toast.makeText(DemoActivity.this,tId,Toast.LENGTH_SHORT).show();
                }else if (item.equals("sendImTxtMsg")) {
                    //Send ImTxtMsg
                    mUserService.sendImTxtMsg(toId,RankType.NORMAL,"Hello!I'm "+UserService.getTerminalId());
                }else if (item.equals("sendImVoiceMsg")) {
                    byte[] voiceData = new byte[]{1,2,3};
                    mUserService.sendImVoiceMsg(toId,voiceData);
                }else if (item.equals("sendMyLocation")) {
                    //This msg should only be sent to schedule pc.
                    /*mUserService.sendMyLocation();*/
                }else if (item.equals("sendMyStatus")){
                    //This msg should only be sent to schedule pc.
                    /*mUserService.sendMyStatus(StatusMessage.StatusMsgType.STATUS_ONLINE);*/
                }else if (item.equals("getMyLocation")){
                    popUpToast(UserService.getMyLocation().toString());
                }else if (item.equals("getMySpeed")) {
                    popUpToast(String.valueOf(UserService.getMySpeed()));
                }else if (item.equals("getTerminalType")) {
                    popUpToast(UserService.getTerminalType().toString());
                }

            }else {
                Toast.makeText(DemoActivity.this,R.string.notify_disconnected,Toast.LENGTH_SHORT).show();
            }


        }
    };

    private BroadcastReceiver mConnStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action  = intent.getAction();
            //Use 'if' for the purpose of further extending filter more actions.
            if (MessageService.BROADCAST_ACTION_STATE_CHANGED.equals(action)) {
                ConnectionState newState = (ConnectionState)intent.getSerializableExtra(MessageService.EXTRA_CONNECTION_STATE);
                if (mConnState!= newState) {
                    Log.d(LOG_TAG,"mConnStateReceiver(): Connection state changed [old:"
                            +mConnState+" new:"+newState+"]");
                    mConnState = newState;
                }
                if (!ConnectionState.CONNECTED.equals(newState)){
                    String popMsg = getResources().getText(R.string.notify_disconnected).toString();
                    Toast.makeText(DemoActivity.this,popMsg+"[old:"+mConnState+" new:"+newState+"]",Toast.LENGTH_SHORT).show();
                }
            }

        }
    };

    @Override
    public void getUserService() {
        mListener = new LocalListener();
        mNativeService = ((CarLocationApplication) getApplicationContext()).getService();
        mConnState = mNativeService.getConnState();
        if (mUserService == null) {
            mUserService = new UserService(mNativeService, mListener);
        }
    }


    /**
     * Register notification listener after getService() is not null.
     * Otherwise register for another time after 500ms.
     */
    @Override
    public void regNotifictListenner() {

        if (mNativeService != null) {
            mNativeService.registerNotificationListener(mListener);
        } else {
            Message mesg = Message.obtain(mHandler, REGISTER_NOTIFICATION);
            mHandler.sendMessageDelayed(mesg, 500);
        }

    }

    class LocalListener implements ResponseListener, NotificationListener {

        /**
         * Unsolicited message notification.
         *
         * @param noti
         */
        @Override
        public void onNotify(Notification noti) {
            forward(noti);
        }

        @Override
        public void onResponse(Notification noti) {
            forward(noti);
        }


        private void forward(Notification notif) {
            if (mResumed) {
                if (null == notif) {
                    Log.e(LOG_TAG, "handleMessage(): Error: Neither response nor unsolicited msg received from server.");
                    return;
                }
                Message message = Message.obtain(mHandler, HANDLE_NOTIFICATION, notif);
                mHandler.sendMessage(message);
            } else {
                Log.e(LOG_TAG, "forward():DemoActivity has been destroyed,no alive handler set to handle message!");
                //TODO Add more action to save pending msg and re-handle after activity restart.
            }
        }
    }

    private Handler mHandler = new Handler(){
        /**
         * Subclasses must implement this to receive messages.
         *
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case REGISTER_NOTIFICATION:
                    regNotifictListenner();
                    break;
                case HANDLE_NOTIFICATION:
                    Notification noti = (Notification) msg.obj;

                    switch (noti.notiType.ordinal()) {
                        case NOTIFY_TYPE_REQUEST: {
                            break;
                        }
                        case NOTIFY_TYPE_RESPONSE: {
                            //Deal with all response notify
                            handleResponseMessage(msg);
                            break;
                        }
                        case NOTIFY_TYPE_UNSOLICITED: {
                            //Deal with all unsolicited notify
                            handleUnsolicitedMessage(msg);
                            break;
                        }
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }

            return;

        }
    };

    @Override
    public void handleResponseMessage(Message msg) {
        Notification noti = (Notification) msg.obj;
        MessageType msgType = noti.message.getMessageType();
        switch (msgType.ordinal()) {
            case LOCATION_MESSAGE:
                //Current we don't send location with response callback. So nothing to do.
                break;
            case IM_MESSAGE:
                //Current we don't send IM Msg with response callback. So nothing to do.
                break;
            case TASK_MESSAGE:
                handleTaskAssignmentMsgRsp(noti);
                break;
            case GLIDE_MESSAGE:
                handleGlidePathMsgRsp(noti);
                break;
            case WARN_MESSAGE:
                handleWarnMsgRsp(noti);
                break;
            case STATUS_MESSAGE:
                //Current we don't send IM Msg with response callback. So nothing to do.
                break;
            default:
                break;
        }

    }

    public void handleTaskAssignmentMsgRsp(Notification noti){

    }

    public void handleGlidePathMsgRsp(Notification noti){

    }

    public void handleWarnMsgRsp(Notification noti){

    }



    @Override
    public void handleUnsolicitedMessage(Message msg) {
        Notification noti = (Notification) msg.obj;
        MessageType msgType = noti.message.getMessageType();
        switch (msgType.ordinal()){
            case LOCATION_MESSAGE:
                handleLocations(noti);
                break;
            case IM_MESSAGE:
                handleImMsg(noti);
                break;
            case TASK_MESSAGE:
                handleTaskMsg(noti);
                break;
            case GLIDE_MESSAGE:
                handleGlideMsg(noti);
                break;
            case WARN_MESSAGE:
                handleWarnMsg(noti);
                break;
            case STATUS_MESSAGE:
                handleStatusMsg(noti);
                break;
            default:break;
        }

    }

    public void handleLocations(Notification noti){
        LocationMessage locMsg = (LocationMessage) noti.message;
        popUpToast("LocationMsg:"+locMsg.mLocationArray.toString());
    }
    public void handleImMsg(Notification noti){
        IMMessage imMsg = (IMMessage)noti.message;
        if (imMsg.mImMsgType == IMMessage.IMMsgType.IM_TXT_MSG){
            //Temporary to display toast content to user.
            IMTxtMessage txtMsg = (IMTxtMessage)imMsg;
            popUpToast("IMTxtMessage:"+txtMsg.mTxtCont);

        }else if (imMsg.mImMsgType == IMMessage.IMMsgType.IM_VOICE_MSG){
            IMVoiceMessage voiceMsg = (IMVoiceMessage)imMsg;
            popUpToast("IMVoiceMessage:"+new String(voiceMsg.mVoiceData));

        }else {
            popUpToast("Wrong IMMsgType!");
        }
    }

    private void popUpToast(String mTxtCont) {
        Toast.makeText(DemoActivity.this,mTxtCont,Toast.LENGTH_SHORT).show();
    }

    public void handleTaskMsg(Notification noti){
        TaskAssignmentMessage taskMsg = (TaskAssignmentMessage)noti.message;
        popUpToast("TaskAssign");

    }
    public void handleGlideMsg(Notification noti){

    }
    public void handleWarnMsg(Notification noti){

    }
    public void handleStatusMsg(Notification noti){
        StatusMessage statMsg = (StatusMessage)noti.message;

        popUpToast("StatusMsg:"+statMsg.mStatus);

    }

}
