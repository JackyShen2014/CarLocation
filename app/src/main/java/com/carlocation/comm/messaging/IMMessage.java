package com.carlocation.comm.messaging;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jacky on 2015/4/21.
 *
 * @author Jacky Shen
 */
public class IMMessage extends BaseMessage {
    private static final String LOG_TAG = "IMMessage";

    /**
     * mToTerminalId used to indicate destination of this IM message.
     * 0 element indicates broadcast.
     * 1 element indicates one to one.
     * others indicates one to multiples.
     */
    public List<String> mToTerminalId;


	public IMMsgType mImMsgType;

	public enum IMMsgType {
		IM_TXT_MSG(0), IM_VOICE_MSG(1),UNKONWN(-1);
		private int code;

		IMMsgType(int code) {
			this.code = code;
		}

		public static IMMsgType valueOf(int code){
			switch (code){
				case 0: return IM_TXT_MSG;
				case 1: return IM_VOICE_MSG;
				default:return UNKONWN;
			}

		}
	}

	public IMMessage() {
	}

	public IMMessage(long mTransactionID,  String mSenderId,
                     List<String> mToTerminalId, IMMsgType mImMsgType) {
        super(mTransactionID, MessageType.IM_MESSAGE, mSenderId, TerminalType.TERMINAL_CAR);

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
			JSONObject object = super.translateJsonObject();

            if (mToTerminalId != null) {
                JSONArray array = new JSONArray();
                for (String terminalId : mToTerminalId) {
                    array.put(terminalId);
                }

                object.put("mToTerminalId", array);
            }
			object.put("mImMsgType", mImMsgType.ordinal());

			return object;
		} catch (JSONException e) {
			Log.e(LOG_TAG, "JSONException accured!");
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String toString() {
		return "IMMessage [" + super.toString()
                + (mToTerminalId != null ? mToTerminalId.toString() : null)
				+ ", mImMsgType=" + mImMsgType + "]";
	}
}
