package com.carlocation.comm.messaging;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 28851620 on 4/22/2015.
 * @author Jacky Shen
 */
public class StatusMessage extends BaseMessage {
	private final String LOG_TAG = "StatusMessage";

	public long mTerminalId;
	public StatusMsgType mStatus;
	public UserType mUserType;


    public StatusMessage(long mTransactionID, long mTerminalId,
                         StatusMsgType mStatus, UserType mUserType) {
        super(mTransactionID, MessageType.STATUS_MESSAGE);
        this.mTerminalId = mTerminalId;
        this.mStatus = mStatus;
        this.mUserType = mUserType;
    }

    /**
     * Used for indicate where the msg comes from
     */
    public static enum UserType {
        MOBILE_PAD,
        CONTROL_PC,
    }

    /**
     * Used for indicate current status
     */
    public static enum StatusMsgType {
        STATUS_ONLINE,
        STATUS_OFFLINE,
        STATUS_LEAVE,
    }

	/**
	 * Use to translate to network format
	 * 
	 * @return
	 */
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
			object.put("mTransactionID", StatusMessage.this.mTransactionID);
			object.put("mMessageType", StatusMessage.this.mMessageType.ordinal());
			object.put("mTerminalId", mTerminalId);
			object.put("mStatus", mStatus.ordinal());
			object.put("mUserType", mUserType.ordinal());
			return object;

		} catch (JSONException e) {
			Log.e(LOG_TAG, "JSONException accured!");
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String toString() {
		return "StatusMessage [" + super.toString() + "mTerminalId="
				+ mTerminalId + ", mStatus=" + mStatus + ", mUserType="
				+ mUserType + "]";
	}
}
