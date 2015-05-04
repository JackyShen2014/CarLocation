package com.carlocation.comm.messaging;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public long mTerminalId;
    public TerminalType mTerminalType;
    public Location mLocation;
    public float mSpeed;

    public LocationMessage() {
        super(System.currentTimeMillis());
        this.mMessageType = MessageType.LOCATION_MESSAGE;
    }

    public LocationMessage(long mTransactionID, long mTerminalId,
                           TerminalType mTerminalType, Location mLocation, float mSpeed) {
        super(mTransactionID, MessageType.LOCATION_MESSAGE);
        this.mTerminalId = mTerminalId;
        this.mTerminalType = mTerminalType;
        this.mLocation = mLocation;
        this.mSpeed = mSpeed;
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
			object.put("mTerminalId", mTerminalId);
			object.put("mTerminalType", mTerminalType.ordinal());

			JSONObject jSonObj = new JSONObject();
			jSonObj.put("mLng", mLocation.mLng);
			jSonObj.put("mLat", mLocation.mLat);

			object.put("mLocation", jSonObj);
			object.put("mSpeed", mSpeed);

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
		return "LocationMessage [" + super.toString() + "mTerminalId="
				+ mTerminalId + ", mTerminalType=" + mTerminalType + ", mLng="
				+ mLocation.mLng + ", mLat=" + mLocation.mLat + ", mSpeed="
				+ mSpeed + "]";
	}

}
