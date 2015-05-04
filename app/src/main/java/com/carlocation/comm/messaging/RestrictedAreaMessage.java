package com.carlocation.comm.messaging;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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
	public ArrayList<Location> mLocationArea = new ArrayList<Location>();

    public RestrictedAreaMessage(long mTransactionID, ActionType mActionType,
                                 int mWarnAreaId, ArrayList<Location> mLocationArea) {
        super(mTransactionID, MessageType.WARN_MESSAGE);
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
			JSONObject object = new JSONObject();
			object.put("mTransactionID",
					RestrictedAreaMessage.this.mTransactionID);
			object.put("mMessageType", RestrictedAreaMessage.this.mMessageType);
			object.put("mActionType", mActionType);
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

	@Override
	public String toString() {
		return "RestrictedAreaMessage [" + super.toString() + ", mActionType="
				+ mActionType + ", mWarnAreaId=" + mWarnAreaId
				+ ", mLocationArea="
				+ (mLocationArea != null ? mLocationArea.toString() : null)
				+ "]";
	}
}
