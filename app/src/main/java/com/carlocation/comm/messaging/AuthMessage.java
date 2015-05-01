package com.carlocation.comm.messaging;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;

/**
 * Used for authentication
 *
 * @author Jacky Shen
 */
public class AuthMessage extends BaseMessage {
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
    }


    public AuthMessage(long mTransactionID, MessageType mMessageType, long mTerminalId,
                       String mUserName, String mPassword, AuthMsgType mAuthType) {
        super(mTransactionID, mMessageType);
        this.mTerminalId = mTerminalId;
        this.mUserName = mUserName;
        this.mPassword = mPassword;
        this.mAuthType = mAuthType;
    }

    @Override
    public String translate() {
        //Define return result
        String jSonResult = "";
        try{
            JSONObject object = new JSONObject();
            object.put("mTransactionID",AuthMessage.this.mTransactionID);
            object.put("mMessageType",AuthMessage.this.mMessageType);
            object.put("mTerminalId",mTerminalId);
            object.put("mUserName",mUserName);
            object.put("mPassword",mPassword);
            object.put("mAuthType",mAuthType);

            jSonResult = object.toString();

        }catch (JSONException e){
            Log.e(LOG_TAG,"JSONException accured!");
            e.printStackTrace();
        }
        Log.d(LOG_TAG,"Output json format is "+ jSonResult);
        return jSonResult;
    }


    @Override
    public String toString() {
        return "AuthMessage ["
                + super.toString()
                + "mTerminalId=" + mTerminalId
                + ", mUserName=" + mUserName
                + ", mPassword=" + mPassword
                + ", mAuthType=" + mAuthType
                + "]";
    }


}
