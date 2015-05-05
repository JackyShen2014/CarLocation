package com.carlocation.view;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.carlocation.R;
import com.carlocation.comm.IMessageService;
import com.carlocation.comm.NotificationListener;
import com.carlocation.comm.ResponseListener;
import com.carlocation.comm.messaging.ActionType;
import com.carlocation.comm.messaging.AuthMessage;
import com.carlocation.comm.messaging.GlidingPathMessage;
import com.carlocation.comm.messaging.IMMessage;
import com.carlocation.comm.messaging.IMTxtMessage;
import com.carlocation.comm.messaging.IMVoiceMessage;
import com.carlocation.comm.messaging.Location;
import com.carlocation.comm.messaging.MessageResponseStatus;
import com.carlocation.comm.messaging.MessageType;
import com.carlocation.comm.messaging.Notification;
import com.carlocation.comm.messaging.RankType;
import com.carlocation.comm.messaging.RestrictedAreaMessage;
import com.carlocation.comm.messaging.StatusMessage;
import com.carlocation.comm.messaging.TaskAssignmentMessage;
import com.carlocation.comm.messaging.TerminalType;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks {
    private static final String LOG_TAG = "MainActivity";


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
     * Fragment managing the behaviors, interactions and presentation of the
     * navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in
     * {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    /**
     * An indicator to indicate if this MainActivity still alive (not destroyed).
     */
    private boolean mResumed;


    private static final int REGISTER_NOTIFICATION = 0;
    private static final int HANDLE_NOTIFICATION = 1;

    private static final int NOTIFY_TYPE_REQUEST = 0;
    private static final int NOTIFY_TYPE_RESPONSE = 1;
    private static final int NOTIFY_TYPE_UNSOLICITED = 2;

    private static final int AUTH_MESSAGE = 0;
    private static final int LOCATION_MESSAGE = 1;
    private static final int IM_MESSAGE = 2;
    private static final int TASK_MESSAGE = 3;
    private static final int GLIDE_MESSAGE = 4;
    private static final int WARN_MESSAGE = 5;
    private static final int STATUS_MESSAGE = 6;

    private static final int ACTION_ASSIGN = 0;
    private static final int ACTION_QUERY = 1;
    private static final int ACTION_START = 2;
    private static final int ACTION_FINISH = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Retrieve native service.
         */
        mListener = new LocalListener();
        mNativeService = ((CarLocationApplication) getApplicationContext()).getService();
        if (mUserService == null) {
            mUserService = new UserService(mNativeService, mListener);
        }


        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navigation_drawer);


        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager
                .beginTransaction()
                .replace(R.id.container, new MapFragment()).commit();


        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        //RegisterNotificationListener
        //make sure service is not null
        Message msg = Message.obtain(mHandler, REGISTER_NOTIFICATION);
        mHandler.sendMessageDelayed(msg, 500);

        //FIXME asynTask used to test all APIs of logical service layer
        new send().execute();
    }

    /**
     * This is the fragment-orientated version of {@link #onResume()} that you
     * can override to perform operations in the Activity at the same point
     * where its fragments are resumed.  Be sure to always call through to
     * the super-class.
     */
    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
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

        (((CarLocationApplication) getApplicationContext()).getService()).unRegisterNotificationListener(this.mListener);
    }

    private class send extends AsyncTask<String, Void, Void> {

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected Void doInBackground(String... params) {
            //Retrieve Native Service and Send LogIn MSG to server to login.

            /**
             * Examples for how to use UserService to send MSG to Server
             */

            List<Long> toArray  = new ArrayList<Long>();
            toArray.add(567l);
            toArray.add(789l);

            byte[] bArray = {(byte) 1, (byte) 2, (byte) 3};

            ArrayList<Location> array = new ArrayList<Location>();
            array.add(new Location(321.123, 456.654));
            array.add(new Location(789.987, 890.098));

            /**
             * Print out  JSON format of request messages
             */

            mUserService.sendMyStatus(StatusMessage.StatusMsgType.STATUS_ONLINE);
            mUserService.sendMyLocation();
            mUserService.sendImTxtMsg(toArray, RankType.EMERGENCY,"IM txt msg");
            mUserService.sendImVoiceMsg(toArray,bArray);
            mUserService.startWorkMsg((short) 1);
            mUserService.finishWorkMsg((short) 1);
            mUserService.queryWorkById((short) 1);
            mUserService.queryGlidePathById(1);
            mUserService.queryWarnAreaById(1);

            /**
             * Print out  JSON format of response messages
             */

            GlidingPathMessage glideMsg = new GlidingPathMessage(123, ActionType.ACTION_QUERY, 456, "title", 7, array);
            RestrictedAreaMessage warnMsg = new RestrictedAreaMessage(123,456,ActionType.ACTION_QUERY,12,array);
            TaskAssignmentMessage taskMsg = new TaskAssignmentMessage(123,456,ActionType.ACTION_QUERY,(short)1,null);

            MessageResponseStatus status = MessageResponseStatus.SUCCESS;
            mUserService.responActionAssign(glideMsg,status);
            mUserService.responActionAssign(warnMsg,status);
            mUserService.responActionAssign(taskMsg,status);




            return null;
        }
    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {
//        // update the main content by replacing fragments
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        fragmentManager
//                .beginTransaction()
//                .replace(R.id.container,
//                        PlaceholderFragment.newInstance(position + 1)).commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private Handler mHandler = new Handler() {

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
                    /**
                     * Register notification listener after getService() is not null.
                     * Otherwise register for another time after 500ms.
                     */
                    IMessageService nativeService = (((CarLocationApplication) getApplicationContext()).getService());
                    if (nativeService != null) {
                        nativeService.registerNotificationListener(mListener);
                    } else {
                        Message mesg = Message.obtain(mHandler, REGISTER_NOTIFICATION);
                        mHandler.sendMessageDelayed(mesg, 500);
                    }
                    break;
                case HANDLE_NOTIFICATION:
                    Notification noti = (Notification) msg.obj;

                    switch (noti.notiType.ordinal()) {
                        case NOTIFY_TYPE_REQUEST: {
                            //TODO deal with all request notify
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


    private void handleResponseMessage(Message msg) {
        Notification noti = (Notification) msg.obj;
        MessageType msgType = noti.message.getMessageType();
        switch (msgType.ordinal()) {
            case AUTH_MESSAGE:
                //Deal with auth response
                AuthMessage authMsg = (AuthMessage) noti.message;

                if (authMsg.mAuthType == AuthMessage.AuthMsgType.AUTH_LOGIN_MSG) {
                    //Already done in LoginActivity, so nothing to do

                } else if (authMsg.mAuthType == AuthMessage.AuthMsgType.AUTH_LOGOUT_MSG) {
                    //Deal with logout RSP
                    if (noti.result == Notification.Result.SUCCESS) {
                        Toast.makeText(MainActivity.this, R.string.notify_logout_success, Toast.LENGTH_SHORT).show();
                        //TODO Start another activity to enter next action after logout

                    } else {
                        String notifyFail = getResources().getText(R.string.notify_logout_fail).toString();
                        Toast.makeText(MainActivity.this, notifyFail + noti.result, Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Log.e(LOG_TAG, "handleMessage(): Error: Wrong AuthType!");
                    return;
                }
                break;
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

    private void handleTaskAssignmentMsgRsp(Notification noti) {
        TaskAssignmentMessage taskMsg = (TaskAssignmentMessage) noti.message;
        switch (taskMsg.mActionType.ordinal()) {
            case ACTION_ASSIGN:
                Log.e(LOG_TAG, "handleTaskAssignmentMsgRsp(): It should not be here!");
                break;
            case ACTION_QUERY:
            case ACTION_START:
            case ACTION_FINISH:
                if (noti.result == Notification.Result.SUCCESS) {
                    Toast.makeText(MainActivity.this, R.string.notify_msg_resp_OK, Toast.LENGTH_SHORT).show();
                } else if (noti.result == Notification.Result.NO_CONNECTION) {
                    //Indicate to user that msg sent failed due to no connection
                    Toast.makeText(MainActivity.this, R.string.notify_msg_resp_no_connection, Toast.LENGTH_SHORT).show();

                } else if (noti.result == Notification.Result.SERVER_RJECT) {
                    Toast.makeText(MainActivity.this, R.string.notify_msg_resp_reject, Toast.LENGTH_SHORT).show();
                    //TODO if msg has been reject by server, what to do next?
                } else {
                    Log.d(LOG_TAG, "handleTaskAssignmentMsgRsp(): Server failed to receive " +
                            "this msg for reason= " + noti.result);
                    //Resend msg if server didn't receive msg for these reasons.
                    mNativeService.sendMessage(taskMsg, mListener);
                }

                break;
        }


    }

    private void handleGlidePathMsgRsp(Notification noti) {
        GlidingPathMessage glideMsg = (GlidingPathMessage) noti.message;
        switch (glideMsg.mActionType.ordinal()) {
            case ACTION_ASSIGN:
            case ACTION_START:
            case ACTION_FINISH:
                Log.e(LOG_TAG, "handleGlidePathMsgRsp(): It should not be here!");
                break;
            case ACTION_QUERY:
                if (noti.result == Notification.Result.SUCCESS) {
                    Toast.makeText(MainActivity.this, R.string.notify_msg_resp_OK, Toast.LENGTH_SHORT).show();
                } else if (noti.result == Notification.Result.NO_CONNECTION) {
                    //Indicate to user that msg sent failed due to no connection
                    Toast.makeText(MainActivity.this, R.string.notify_msg_resp_no_connection, Toast.LENGTH_SHORT).show();

                } else if (noti.result == Notification.Result.SERVER_RJECT) {
                    Toast.makeText(MainActivity.this, R.string.notify_msg_resp_reject, Toast.LENGTH_SHORT).show();
                    //TODO if msg has been reject by server, what to do next?
                } else {
                    Log.d(LOG_TAG, "handleGlidePathMsgRsp(): Server failed to receive " +
                            "this msg for reason= " + noti.result);
                    //Resend msg if server didn't receive msg for these reasons.
                    mNativeService.sendMessage(glideMsg, mListener);
                }
                break;
        }

    }

    private void handleWarnMsgRsp(Notification noti) {
        RestrictedAreaMessage warnMsg = (RestrictedAreaMessage) noti.message;
        switch (warnMsg.mActionType.ordinal()) {
            case ACTION_ASSIGN:
            case ACTION_START:
            case ACTION_FINISH:
                Log.e(LOG_TAG, "handleWarnMsgRsp(): It should not be here!");
                break;
            case ACTION_QUERY:
                if (noti.result == Notification.Result.SUCCESS) {
                    Toast.makeText(MainActivity.this, R.string.notify_msg_resp_OK, Toast.LENGTH_SHORT).show();
                } else if (noti.result == Notification.Result.NO_CONNECTION) {
                    //Indicate to user that msg sent failed due to no connection
                    Toast.makeText(MainActivity.this, R.string.notify_msg_resp_no_connection, Toast.LENGTH_SHORT).show();

                } else if (noti.result == Notification.Result.SERVER_RJECT) {
                    Toast.makeText(MainActivity.this, R.string.notify_msg_resp_reject, Toast.LENGTH_SHORT).show();
                    //TODO if msg has been reject by server, what to do next?
                } else {
                    Log.d(LOG_TAG, "handleWarnMsgRsp(): Server failed to receive " +
                            "this msg for reason= " + noti.result);
                    //Resend msg if server didn't receive msg for these reasons.
                    mNativeService.sendMessage(warnMsg, mListener);
                }
                break;
        }

    }

    private void handleUnsolicitedMessage(Message msg) {
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

    private void handleStatusMsg(Notification noti) {
        StatusMessage statMsg = (StatusMessage)noti.message;
        if(statMsg.mSenderType != TerminalType.TERMINAL_PC){
            Log.d(LOG_TAG,"handleStatusMsg(): Wrong user type msg!");
        }else{
            /**
             * Add the terminal id and correspond status (online, offline)to local database
             * What mobile pad deal with schedule pc's status message?
             */
            //TODO deal with status msg of schedule pc

        }
    }

    private void handleWarnMsg(Notification noti) {
        RestrictedAreaMessage warnMsg = (RestrictedAreaMessage)noti.message;
        if(warnMsg.mActionType == ActionType.ACTION_ASSIGN){
            if(warnMsg.mLocationArea != null && !warnMsg.mLocationArea.isEmpty()){
                //TODO check whether this id exists in database, add in if not.


            }else {
                //TODO check whether this id exists in database, send query msg if not

            }
            //Send back respond to server with success
            MessageResponseStatus status = MessageResponseStatus.SUCCESS;
            mUserService.responActionAssign(warnMsg,status);
        }else {
            Log.e(LOG_TAG,"handleWarnMsg(): Wrong action type!");
            //Send back respond to server with type not supported
            MessageResponseStatus status = MessageResponseStatus.NOT_SUPPORTED;
            mUserService.responActionAssign(warnMsg,status);
        }
    }

    private void handleGlideMsg(Notification noti) {
        GlidingPathMessage glideMsg = (GlidingPathMessage)noti.message;
        if (glideMsg.mActionType == ActionType.ACTION_ASSIGN){
            if(glideMsg.mLocationArray != null && !glideMsg.mLocationArray.isEmpty()){
                //TODO check whether this id exists in database, add in if not.

            }else {
                //TODO check whether this id exists in database, send query msg if not

            }
            //Send back respond to server with success
            MessageResponseStatus status = MessageResponseStatus.SUCCESS;
            mUserService.responActionAssign(glideMsg,status);

        }else {
            Log.e(LOG_TAG,"handleGlideMsg(): Wrong action type!");
            //Send back respond to server with type not supported
            MessageResponseStatus status = MessageResponseStatus.NOT_SUPPORTED;
            mUserService.responActionAssign(glideMsg,status);
        }
    }


    private void handleTaskMsg(Notification noti) {
        TaskAssignmentMessage taskMsg = (TaskAssignmentMessage)noti.message;
        if(taskMsg.mActionType == ActionType.ACTION_ASSIGN){
            if(taskMsg.mTaskContent != null && taskMsg.mTaskContent.length()>0 ){
                //TODO check whether this id exists in database, add in if not.


            }else {
                //TODO check task id if exists in data base, otherwise need to send taskMsg query.

            }
            //Send back respond to server with success
            MessageResponseStatus status = MessageResponseStatus.SUCCESS;
            mUserService.responActionAssign(taskMsg,status);
        }else{
            Log.e(LOG_TAG,"handleTaskMsg(): Wrong action type!");
            //Send back respond to server with type not supported
            MessageResponseStatus status = MessageResponseStatus.NOT_SUPPORTED;
            mUserService.responActionAssign(taskMsg,status);
        }

    }

    private void handleLocations(Notification noti){
        //TODO update location on the map (How a server send locations with LocationMessage?)
    }

    private void handleImMsg(Notification noti){
        IMMessage imMsg = (IMMessage)noti.message;
        if (imMsg.mImMsgType == IMMessage.IMMsgType.IM_TXT_MSG){
            //TODO Display txt to the user

        }else{
            //TODO Play voice to the user
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
                Log.e(LOG_TAG, "onResponse():MainActivity has been destroyed,no alive handler set to handle message!");
                //TODO Add more action to save pending msg and re-handle after activity restart.
            }
        }
    }

}
