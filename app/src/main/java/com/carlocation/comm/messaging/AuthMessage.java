package com.carlocation.comm.messaging;

import android.util.Log;

/**
 * Use to authentication
 *
 * @author 28851274
 */
public class AuthMessage extends Message {
    private static final long serialVersionUID = -7313293501889870528L;

    private String LOG_TAG = "AuthMessage";

    private String mUserName;
    private String mPassword;
    private AuthMsgType mAuthType;

    public enum AuthMsgType {
        AUTH_LOGIN_MSG,
        AUTH_LOGOUT_MSG,
    }

    public AuthMessage() {
        super(System.currentTimeMillis());
    }


    public AuthMessage(String mUserName, String mPassword) {
        super(System.currentTimeMillis());
        this.mUserName = mUserName;
        this.mPassword = mPassword;
        super.mMessageType = MessageType.AUTH_MESSAGE;
    }


    public String getUserName() {
        return mUserName;
    }

    public String getPassword() {
        return mPassword;
    }

    public AuthMsgType getAuthType() {
        return mAuthType;
    }

    public void setUserName(String userName) {
        this.mUserName = userName;
    }

    public void setPassword(String password) {
        this.mPassword = password;
    }

    public void setAuthType(AuthMsgType authType) {
        this.mAuthType = authType;
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
