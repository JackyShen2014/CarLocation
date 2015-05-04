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


    public AuthMessage(long mTransactionID, long mTerminalId,
                       String mUserName, String mPassword, AuthMsgType mAuthType) {
        super(mTransactionID, MessageType.AUTH_MESSAGE);
        this.mTerminalId = mTerminalId;
        this.mUserName = mUserName;
        this.mPassword = mPassword;
        this.mAuthType = mAuthType;
    }


	@Override
	public String translate() {
		// Define return result
		String jSonResult = "";
		JSONObject object = translateJsonObject();
		if (object != null) {
			jSonResult = object.toString();
		}
		Log.d(LOG_TAG, "Output json format is " + jSonResult);
		return jSonResult;
	}

	@Override
	public JSONObject translateJsonObject() {
		try {
			JSONObject object = new JSONObject();
			object.put("mTransactionID", AuthMessage.this.mTransactionID);
			object.put("mMessageType", AuthMessage.this.mMessageType.ordinal());
			object.put("mTerminalId", mTerminalId);
			object.put("mUserName", mUserName);
			object.put("mPassword", mPassword);
			object.put("mAuthType", mAuthType.ordinal());

			return object;

		} catch (JSONException e) {
			Log.e(LOG_TAG, "JSONException accured!");
			e.printStackTrace();
		}
		return null;
	}


}
