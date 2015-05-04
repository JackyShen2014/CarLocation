package com.carlocation.comm.messaging;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Jacky on 2015/4/21.
 *
 * @author Jacky Shen
 */
public class IMMessage extends BaseMessage {
    private static final String LOG_TAG = "IMMessage";

	public long mFromTerminalId;
	public long mToTerminalId;
	public IMMsgType mImMsgType;

	public static enum IMMsgType {
		IM_TXT_MSG, IM_VOICE_MSG,
	}

	public IMMessage(long mTransactionID) {
		super(mTransactionID);
	}

    public IMMessage(long mTransactionID, long mFromTerminalId,
                     long mToTerminalId, IMMsgType mImMsgType) {
        super(mTransactionID, MessageType.IM_MESSAGE);
        this.mFromTerminalId = mFromTerminalId;
        this.mToTerminalId = mToTerminalId;
        this.mImMsgType = mImMsgType;
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
		// Define return result
		try {
			JSONObject object = new JSONObject();
			object.put("mTransactionID", IMMessage.this.mTransactionID);
			object.put("mMessageType", IMMessage.this.mMessageType);
			object.put("mFromTerminalId", mFromTerminalId);
			object.put("mToTerminalId", mToTerminalId);
			object.put("mImMsgType", mImMsgType);

			return object;
		} catch (JSONException e) {
			Log.e(LOG_TAG, "JSONException accured!");
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String toString() {
		return "IMMessage [" + super.toString() + "mFromTerminalId="
				+ mFromTerminalId + ", mToTerminalId=" + mToTerminalId
				+ ", mImMsgType=" + mImMsgType + "]";
	}
}
