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
 * Location message.<br>
 * Maybe contain one location or multi locations.
 * 
 * @author 28851274
 * @author Jacky Shen
 */
public class LocationMessage extends BaseMessage {

    private static final long serialVersionUID = -1427264506745698504L;
    private final String LOG_TAG = "LocationMessage";

    public List<LocationCell> mLocationArray = new ArrayList<>();

	public LocationMessage() {

	}

	public LocationMessage(long mTransactionID,String mSenderId,List<LocationCell> mLocationArray) {
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
			JSONObject object = super.translateJsonObject();

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

	public static BaseMessage parseJsonObject(JsonReader reader){
		LocationMessage loctMsg = new LocationMessage();

		try {
			reader.beginObject();
			while (reader.hasNext()){
				String tagName = reader.nextName();
				if (tagName.equals("mTransactionID")) {
					loctMsg.mTransactionID = reader.nextLong();
				} else if (tagName.equals("mMessageType")) {
					loctMsg.mMessageType = MessageType.valueOf(reader.nextInt());
				} else if (tagName.equals("mSenderId")) {
					loctMsg.mSenderId = reader.nextString();
				} else if (tagName.equals("mSenderType")) {
					loctMsg.mSenderType = TerminalType.valueOf(reader.nextInt());
				} else if (tagName.equals("mLocationArray")) {
					loctMsg.mLocationArray = getLocCellArray(reader);
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
			return loctMsg;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	private static List<LocationCell> getLocCellArray(JsonReader reader) {
		List<LocationCell> locCellArray = new ArrayList<>();

		try {
			reader.beginArray();
			while (reader.hasNext()){
				locCellArray.add(LocationCell.parseJsonObject(reader));
			}
			reader.endArray();
			return locCellArray;
		} catch (IOException e) {
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
