package com.carlocation.comm.messaging;

import android.util.JsonReader;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by 28851620 on 4/22/2015.
 * @author Jacky Shen
 */
public class StatusMessage extends BaseMessage {
	private final String LOG_TAG = "StatusMessage";

	public StatusMsgType mStatus;

    public StatusMessage() {

    }

    public StatusMessage(long mTransactionID,String mSenderId,StatusMsgType mStatus) {
        super(mTransactionID, MessageType.STATUS_MESSAGE, mSenderId, TerminalType.TERMINAL_CAR);
        this.mStatus = mStatus;
    }


    /**
     * Used for indicate current status
     */
    public enum StatusMsgType {
        STATUS_ONLINE(0),
        STATUS_OFFLINE(1),
        STATUS_LEAVE(2),
        STATUS_UNKNOWN(-1);

        private int code;

        StatusMsgType(int code) {
            this.code = code;
        }

        public static StatusMsgType valueOf(int code){
            switch (code){
                case 0: return STATUS_ONLINE;
                case 1: return STATUS_OFFLINE;
                case 2: return STATUS_LEAVE;
                default:return STATUS_UNKNOWN;
            }
        }
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
			JSONObject object = super.translateJsonObject();

			object.put("mStatus", mStatus.ordinal());

			return object;

		} catch (JSONException e) {
			Log.e(LOG_TAG, "JSONException accured!");
			e.printStackTrace();
		}
		return null;
	}

	public static BaseMessage parseJsonObject(JsonReader reader){
        StatusMessage statusMsg = new StatusMessage();

        try {
            reader.beginObject();
            while (reader.hasNext()){
                String tagName = reader.nextName();
                if (tagName.equals("mTransactionID")) {
                    statusMsg.mTransactionID = reader.nextLong();
                } else if (tagName.equals("mMessageType")) {
                    statusMsg.mMessageType = MessageType.valueOf(reader.nextInt());
                } else if (tagName.equals("mSenderId")) {
                    statusMsg.mSenderId = reader.nextString();
                } else if (tagName.equals("mSenderType")) {
                    statusMsg.mSenderType = TerminalType.valueOf(reader.nextInt());
                } else if (tagName.equals("mStatus")) {
                    statusMsg.mStatus = StatusMsgType.valueOf(reader.nextInt());
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            return statusMsg;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
	}

	@Override
	public String toString() {
		return "StatusMessage [" + super.toString()+ ", mStatus=" + mStatus + "]";
	}
}
