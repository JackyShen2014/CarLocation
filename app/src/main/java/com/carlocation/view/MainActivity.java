package com.carlocation.view;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.carlocation.R;
import com.carlocation.comm.IMessageService;
import com.carlocation.comm.NotificationListener;
import com.carlocation.comm.ResponseListener;
import com.carlocation.comm.messaging.AuthMessage;
import com.carlocation.comm.messaging.GlidingPathMessage;
import com.carlocation.comm.messaging.IMMessage;
import com.carlocation.comm.messaging.IMTxtMessage;
import com.carlocation.comm.messaging.IMVoiceMessage;
import com.carlocation.comm.messaging.Location;
import com.carlocation.comm.messaging.LocationMessage;
import com.carlocation.comm.messaging.MessageType;
import com.carlocation.comm.messaging.Notification;
import com.carlocation.comm.messaging.RestrictedAreaMessage;
import com.carlocation.comm.messaging.StatusMessage;
import com.carlocation.comm.messaging.TaskAssignmentMessage;
import com.carlocation.comm.messaging.TerminalType;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks {
    private static final String LOG_TAG = "MainActivity";


    private LocalListener mListener;
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

    //LOG IN Views
    private EditText field_usrName;
    private EditText field_pasWord;
    private Button button_logIn;

    private String mUserName;
    private String mPasWord;


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
                    //Deal with login RSP
                    if (noti.result == Notification.Result.SUCCESS) {
                        Toast.makeText(MainActivity.this, R.string.notify_login_success, Toast.LENGTH_SHORT).show();
                        //TODO Start another activity to enter next action after login


                    } else {
                        String notifyFail = getResources().getText(R.string.notify_login_fail).toString();
                        Toast.makeText(MainActivity.this, notifyFail + noti.result, Toast.LENGTH_SHORT).show();
                    }

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
                //TODO deal with auth response
                break;
            case IM_MESSAGE:
                //TODO deal with auth response
                break;
            case TASK_MESSAGE:
                //TODO deal with auth response
                break;
            case GLIDE_MESSAGE:
                //TODO deal with auth response
                break;
            case WARN_MESSAGE:
                //TODO deal with auth response
                break;
            case STATUS_MESSAGE:
                //TODO Deal with status response
                break;
            default:
                break;
        }

    }

    private void handleUnsolicitedMessage(Message msg) {
        //TODO Deal with all unsolicited Message

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        setListeners();
        mListener = new LocalListener();

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        //RegisterNotificationListener
        //make sure service is not null
        Message msg = Message.obtain(mHandler, REGISTER_NOTIFICATION);
        mHandler.sendMessageDelayed(msg, 500);
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

    public void findViews() {
        field_usrName = (EditText) findViewById(R.id.userName);
        field_pasWord = (EditText) findViewById(R.id.passWord);
        button_logIn = (Button) findViewById(R.id.logIn);
    }

    public void setListeners() {
        button_logIn.setOnClickListener(new Button.OnClickListener() {
            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                MainActivity.this.start();
            }
        });
    }

    protected void start() {
        if (field_usrName.getText().toString().length() == 0) {
            Toast.makeText(this.getApplicationContext(), "Pls enter UserName!", Toast.LENGTH_SHORT).show();
            field_usrName.requestFocus();
            return;
        } else {
            mUserName = field_usrName.getText().toString();
        }
        if (field_pasWord.getText().toString().length() == 0) {
            Toast.makeText(this.getApplicationContext(), "Pls enter Pass Word!", Toast.LENGTH_SHORT).show();
            field_pasWord.requestFocus();
            return;
        } else {
            mPasWord = field_pasWord.getText().toString();
        }

        new send().execute();
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
             * Retrieve native service.
             */
            mUserService = new UserService(((CarLocationApplication) getApplicationContext()).getService(), mListener);


            /**
             * Examples for how to use UserService to send MSG to Server
             */

            //Test case 1: send login msg to server
            mUserService.logIn(mUserName, mPasWord);

            //Test case 2: send logout msg to server
            mUserService.logOut(mUserName,mPasWord);

            //Test case 2: update mylocation to server
            mUserService.sendMyLocation();



            //Print out all MSGs' json format
            new AuthMessage(123, MessageType.AUTH_MESSAGE, 456, "Name", "password", AuthMessage.AuthMsgType.AUTH_LOGIN_MSG).translate();
            ArrayList<Location> array = new ArrayList<Location>();
            array.add(new Location(321.123, 456.654));
            array.add(new Location(789.987, 890.098));
            new GlidingPathMessage(123, 456, "title", 7, array).translate();
            new IMTxtMessage(123, MessageType.IM_MESSAGE, 456, 789, IMMessage.IMMsgType.IM_TXT_MSG, (byte) 11, "TXTContent").translate();
            byte[] bArray = {(byte) 1, (byte) 2, (byte) 3};
            new IMVoiceMessage(123, MessageType.IM_MESSAGE, 456, 789, IMMessage.IMMsgType.IM_VOICE_MSG, bArray).translate();
            new LocationMessage(123, 456, TerminalType.TERMINAL_CAR, new Location(321.123, 456.654), 1.1f).translate();
            new RestrictedAreaMessage(123, MessageType.WARN_MESSAGE, array).translate();
            new StatusMessage(123, MessageType.STATUS_MESSAGE, 456, StatusMessage.StatusMsgType.STATUS_ONLINE, StatusMessage.UserType.MOBILE_PAD).translate();
            new TaskAssignmentMessage(123, MessageType.TASK_MESSAGE, 456, (short) 1, TaskAssignmentMessage.TaskMsgType.TASK_BEGIN_MSG).translate();

            return null;
        }
    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager
                .beginTransaction()
                .replace(R.id.container,
                        PlaceholderFragment.newInstance(position + 1)).commit();
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container,
                    false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(getArguments().getInt(
                    ARG_SECTION_NUMBER));
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
