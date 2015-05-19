package com.carlocation.comm.messaging;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Location message.<br>
 * Maybe contain one location or multi locations.
 * 
 * @author 28851274
 * @author Jacky Shen
 */
public class LocationMessage extends BaseMessage {

    private static final long serialVersionUID = -1427264506745698504L;
    private final String LOG_TAG = "LocationMessage";

    public ArrayList<LocationCell> mLocationArray = new ArrayList<LocationCell>();

    public LocationMessage(long mTransactionID,String mSenderId,ArrayList<LocationCell> mLocationArray) {
        super(mTransactionID, MessageType.LOCATION_MESSAGE, mSenderId, TerminalType.TERMINAL_CAR);
        this.mLocationArray = mLocationArray;
    }

    /**
	 * Translate Class attributes to json format for network transmit.
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
			object.put("mTransactionID", LocationMessage.this.mTransactionID);
			object.put("mMessageType", LocationMessage.this.mMessageType.ordinal());
            object.put("mSenderId", LocationMessage.this.mSenderId);
            object.put("mSenderType", LocationMessage.this.mSenderType.ordinal());

            if (mLocationArray != null) {
                JSONArray array = new JSONArray();

                for (LocationCell locations : mLocationArray) {
                    JSONObject locObj = new JSONObject();
                    locObj.put("mTerminalId", locations.mTerminalId);
                    locObj.put("mTerminalType", locations.mTerminalType.ordinal());
                    locObj.put("mSpeed", locations.mSpeed);

                    JSONObject local = new JSONObject();
                    local.put("mLng",locations.mLocation.mLng);
                    local.put("mLat",locations.mLocation.mLat);

                    locObj.put("mLocation",local);

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

	/**
	 * Used for logging
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return "LocationMessage [" + super.toString() + "mLocationArray=" +
                (mLocationArray != null ? mLocationArray.toString() : null)
				+ "]";
	}

}
