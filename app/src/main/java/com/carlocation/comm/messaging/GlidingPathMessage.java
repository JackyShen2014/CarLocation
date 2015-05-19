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
public class GlidingPathMessage extends BaseMessage {
    private static final String LOG_TAG = "GlidingPathMessage";

    public ActionType mActionType;
    public String mTitle;
    public int mGlidePathId;
    public List<Location> mLocationArray;

    public GlidingPathMessage(long mTransactionID, ActionType mActionType, String mSenderId,
                              String mTitle, int mGlidePathId, List<Location> mLocationArray) {
        super(mTransactionID, MessageType.GLIDE_MESSAGE, mSenderId, TerminalType.TERMINAL_CAR);
        this.mActionType = mActionType;
        this.mTitle = mTitle;
        this.mGlidePathId = mGlidePathId;
        this.mLocationArray = mLocationArray;
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
			object.put("mTransactionID", GlidingPathMessage.this.mTransactionID);
			object.put("mMessageType", GlidingPathMessage.this.mMessageType.ordinal());
            object.put("mSenderId", GlidingPathMessage.this.mSenderId);
            object.put("mSenderType", GlidingPathMessage.this.mSenderType.ordinal());

			object.put("mActionType", mActionType.ordinal());
			object.put("mTitle", mTitle);
			object.put("mGlidePathId", mGlidePathId);

			if (mLocationArray != null) {
				JSONArray array = new JSONArray();

				for (Location location : mLocationArray) {
					JSONObject locObj = new JSONObject();
					locObj.put("mLng", location.mLng);
					locObj.put("mLat", location.mLat);

					array.put(locObj);
				}

				object.put("mLocationArray", array);
			}

			return object;

		} catch (JSONException e) {
			Log.e(LOG_TAG, "JSONException accured!");
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String toString() {
		return "GlidingPathMessage [" + super.toString() + ", mActionType="
				+ mActionType + ", mTitle="
				+ mTitle + ", mGlidePathId=" + mGlidePathId
				+ ", mLocationArray="
				+ (mLocationArray != null ? mLocationArray.toString() : null)
				+ "]";
	}
}
