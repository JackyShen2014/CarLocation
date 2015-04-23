package com.carlocation.comm.messaging;

import android.util.Log;

/**
 * Use to authentication
 *
 * @author 28851274
 */
public class AuthMessage extends Message {
    private static final long serialVersionUID = -7313293501889870528L;

    private final String LOG_TAG = "AuthMessage";

    public long mTerminalId;
    public String mUserName;
    public String mPassword;
    public AuthMsgType mAuthType;

    public static enum AuthMsgType {
        AUTH_LOGIN_MSG,
        AUTH_LOGOUT_MSG,
    }

    public AuthMessage() {
        super(System.currentTimeMillis());
    }

    public AuthMessage(String mUserName, String mPassword) {
        super();
        this.mUserName = mUserName;
        this.mPassword = mPassword;
    }

    public AuthMessage(long mTransactionID,long mTerminalId, String mUserName, String mPassword, AuthMsgType mAuthType) {
        super(mTransactionID);
        this.mTerminalId = mTerminalId;
        this.mUserName = mUserName;
        this.mPassword = mPassword;
        this.mAuthType = mAuthType;
    }

    public void onResponseHandler(Notification notify) {
        if (null == notify) {
            Log.e(LOG_TAG, "Error: No response received from server for Authentication MSG.");
            return;
        }
        //Retrieve notification Msg
        Notification respNotify = notify;

        if (respNotify.message != null && respNotify.message.mMessageType == MessageType.AUTH_MESSAGE) {
            // AuthMessage authMsg =  new AuthMessage();
            AuthMessage authMsg = (AuthMessage) respNotify.message;

            if (authMsg.mAuthType == AuthMsgType.AUTH_LOGIN_MSG) {
                //TODO deal with login RSP

            } else if (authMsg.mAuthType == AuthMsgType.AUTH_LOGOUT_MSG) {
                //TODO deal with logout RSP

            } else {
                Log.e(LOG_TAG,"Error: Wrong AuthType!");
                return;
            }

        }

    }


    @Override
    public String translate() {
        return null;
    }

}
