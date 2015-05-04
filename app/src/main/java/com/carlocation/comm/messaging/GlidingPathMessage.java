package com.carlocation.comm.messaging;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Jacky on 2015/4/21.
 *
 * @author Jacky Shen
 */
public class GlidingPathMessage extends BaseMessage {
    private static final String LOG_TAG = "GlidingPathMessage";

    public ActionType mActionType;
    public long mTerminalId;
    public String mTitle;
    public int mGlidePathId;
    public ArrayList<Location> mLocationArray = new ArrayList<Location>();


    public GlidingPathMessage(long mTransactionID, ActionType mActionType,
                              long mTerminalId, String mTitle, int mGlidePathId,
                              ArrayList<Location> mLocationArray) {
        super(mTransactionID, MessageType.GLIDE_MESSAGE);
        this.mActionType = mActionType;
        this.mTerminalId = mTerminalId;
        this.mTitle = mTitle;
        this.mGlidePathId = mGlidePathId;
        this.mLocationArray = mLocationArray;
    }

    @Override
    public String translate() {
        //Define return result
        String jSonResult = "";
        try {
            JSONObject object = new JSONObject();
            object.put("mTransactionID", GlidingPathMessage.this.mTransactionID);
            object.put("mMessageType", GlidingPathMessage.this.mMessageType.ordinal());
            object.put("mActionType", mActionType.ordinal());
            object.put("mTerminalId", mTerminalId);
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


            jSonResult = object.toString();

        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSONException accured!");
            e.printStackTrace();
        }
        Log.d(LOG_TAG, "Output json format is " + jSonResult);
        return jSonResult;
    }


    @Override
    public String toString() {
        return "GlidingPathMessage ["
                + super.toString()
                + ", mActionType=" + mActionType
                + ", mTerminalId=" + mTerminalId
                + ", mTitle=" + mTitle
                + ", mGlidePathId=" + mGlidePathId
                + ", mLocationArray=" + (mLocationArray != null ? mLocationArray.toString() : null)
                + "]";
    }
}
