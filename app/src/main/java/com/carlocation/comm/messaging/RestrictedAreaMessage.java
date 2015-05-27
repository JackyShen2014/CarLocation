package com.carlocation.comm.messaging;

import android.util.JsonReader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jacky on 2015/4/21.
 *
 * 
 * @author Jacky Shen
 */
public class RestrictedAreaMessage extends BaseMessage {
	private static final String LOG_TAG = "RestrictedAreaMessage";

	public ActionType mActionType;
	public int mWarnAreaId;
	public List<Location> mLocationArea;

	public RestrictedAreaMessage() {

	}

    public RestrictedAreaMessage(long mTransactionID, String mSenderId, ActionType mActionType,
                                 int mWarnAreaId, List<Location> mLocationArea) {
        super(mTransactionID, MessageType.WARN_MESSAGE, mSenderId, TerminalType.TERMINAL_CAR);
        this.mActionType = mActionType;
        this.mWarnAreaId = mWarnAreaId;
        this.mLocationArea = mLocationArea;
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
			object.put("mActionType", mActionType.ordinal());
			object.put("mWarnAreaId", mWarnAreaId);

			if (mLocationArea != null) {
				JSONArray array = new JSONArray();
				for (Location location : mLocationArea) {
					JSONObject locObj = new JSONObject();
					locObj.put("mLng", location.mLng);
					locObj.put("mLat", location.mLat);
					array.put(locObj);
				}

			}

			return object;

		} catch (JSONException e) {
			Log.e(LOG_TAG, "JSONException accured!");
			e.printStackTrace();
		}
		return null;
	}

	public static BaseMessage parseJsonObject(JsonReader reader){
		RestrictedAreaMessage warnMsg = new RestrictedAreaMessage();

		try {
			reader.beginObject();
			while (reader.hasNext()){
				String tagName = reader.nextName();
				if (tagName.equals("mTransactionID")) {
					warnMsg.mTransactionID = reader.nextLong();
				} else if (tagName.equals("mMessageType")) {
					warnMsg.mMessageType = MessageType.valueOf(reader.nextInt());
				} else if (tagName.equals("mSenderId")) {
					warnMsg.mSenderId = reader.nextString();
				} else if (tagName.equals("mSenderType")) {
					warnMsg.mSenderType = TerminalType.valueOf(reader.nextInt());
				} else if (tagName.equals("mActionType")) {
					warnMsg.mActionType = ActionType.valueOf(reader.nextInt());
				} else if (tagName.equals("mWarnAreaId")) {
					warnMsg.mWarnAreaId = reader.nextInt();
				} else if (tagName.equals("mLocationArea")) {
					warnMsg.mLocationArea = GlidingPathMessage.readLocationArray(reader);
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
			return warnMsg;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public String toString() {
		return "RestrictedAreaMessage [" + super.toString() + ", mActionType="
				+ mActionType + ", mWarnAreaId=" + mWarnAreaId
				+ ", mLocationArea="
				+ (mLocationArea != null ? mLocationArea.toString() : null)
				+ "]";
	}
}
