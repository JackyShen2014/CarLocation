package com.carlocation.comm.messaging;

import android.util.JsonReader;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by 28851620 on 5/5/2015.
 * This class describes a complete location info of one terminal(car, plane);
 * @author Jacky Shen
 */
public class LocationCell {
    private static final long serialVersionUID = -1427264506745698504L;
    private final String LOG_TAG = "LocationMessage";

    public String mTerminalId;
    public TerminalType mTerminalType;
    public Location mLocation;
    public float mSpeed;

    public LocationCell() {

    }

    public LocationCell(String mTerminalId, TerminalType mTerminalType,
                        Location mLocation, float mSpeed) {
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


    public JSONObject translateJsonObject() {
        try {
            JSONObject object = new JSONObject();
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

    public static LocationCell parseJsonObject(JsonReader reader){
        LocationCell locationCell = new LocationCell();

        try {
            reader.beginObject();
            while (reader.hasNext()){
                String tagName = reader.nextName();
                if (tagName.equals("mTerminalId")) {
                    locationCell.mTerminalId = reader.nextString();
                }else if (tagName.equals("mTerminalType")) {
                    locationCell.mTerminalType = TerminalType.valueOf(reader.nextInt());
                }else if (tagName.equals("mLocation")) {
                    locationCell.mLocation = Location.parseLocation(reader);
                }else if (tagName.equals("mSpeed")) {
                    locationCell.mSpeed = Float.parseFloat(reader.nextString());
                }else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            return locationCell;
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
        return "LocationMessage [" + "mTerminalId="
                + mTerminalId + ", mTerminalType=" + mTerminalType + ", mLng="
                + mLocation.mLng + ", mLat=" + mLocation.mLat + ", mSpeed="
                + mSpeed + "]";
    }

}
