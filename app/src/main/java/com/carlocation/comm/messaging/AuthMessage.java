package com.carlocation.comm.messaging;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.carlocation.R;

import java.util.Random;

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

    public AuthMessage(String mUserName, String mPassword, AuthMsgType mAuthType) {
        super();
        Random rand = new Random();
        //FIXME how to implement mTransactionID?
        this.mTransactionID = rand.nextLong();

        //FIXME how to get Terminal ID?
        this.mTerminalId = rand.nextLong();

        this.mMessageType = MessageType.AUTH_MESSAGE;
        this.mUserName = mUserName;
        this.mPassword = mPassword;
        this.mAuthType = mAuthType;
    }

    public AuthMessage(long mTransactionID,long mTerminalId, String mUserName, String mPassword, AuthMsgType mAuthType) {
        super(mTransactionID);
        this.mMessageType = MessageType.AUTH_MESSAGE;
        this.mTerminalId = mTerminalId;
        this.mUserName = mUserName;
        this.mPassword = mPassword;
        this.mAuthType = mAuthType;
    }

    @Override
    public String translate() {
        return null;
    }

}
