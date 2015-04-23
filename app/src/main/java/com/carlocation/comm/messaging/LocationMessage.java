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
 */
public class LocationMessage extends Message {

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

    public LocationMessage(long terminalId, TerminalType terminalType, double longitude,
                           double latitude, float speed) {
        super(System.currentTimeMillis());
        this.mTerminalId = terminalId;
        this.mTerminalType = terminalType;
        this.mLocation = new Location(longitude,latitude);
        this.mSpeed = speed;
        this.mMessageType = MessageType.LOCATION_MESSAGE;
    }


    /**
     * Translate Class attributes to json format for network transmit.
     *
     * @return
     */
    @Override
    public String translate() {

        //Define return result
        String jSonResult = "";

        try{
            JSONObject object = new JSONObject();
            object.put("mTerminalId", mTerminalId);
            object.put("mTerminalType", mTerminalType);

            JSONObject jSonObj = new JSONObject();
            jSonObj.put("mLng", mLocation.mLng);
            jSonObj.put("mLat", mLocation.mLat);

            object.put("mLocation",jSonObj);
            object.put("mSpeed", mSpeed);

            jSonResult = object.toString();

        }catch (JSONException e){
            Log.e(LOG_TAG,"JSONException accured!");
            e.printStackTrace();
        }
        Log.d(LOG_TAG,"Output json format is "+ jSonResult);
        return jSonResult;
    }

    /**
     * Used for logging
     *
     * @return
     */
    @Override
    public String toString() {
        return "LocationMessage [mVehicleId=" + mTerminalId
                + ", mVehicleType=" + mTerminalType
                + ", mLng=" + mLocation.mLng
                + ", mLat=" + mLocation.mLat
                + ", mSpeed=" + mSpeed
                + "]";
    }

}
