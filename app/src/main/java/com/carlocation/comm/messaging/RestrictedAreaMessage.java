package com.carlocation.comm.messaging;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Jacky on 2015/4/21.
 * @author Jacky Shen
 */
public class RestrictedAreaMessage extends BaseMessage {
    private static final String LOG_TAG = "RestrictedAreaMessage";

    public ArrayList<Location> mLocationArea = new ArrayList<Location>();

    public RestrictedAreaMessage(long mTransactionID, ArrayList<Location> mLocationArea) {
        super(mTransactionID);
        this.mLocationArea = mLocationArea;
    }

    public RestrictedAreaMessage(long mTransactionID, MessageType mMessageType,
                                 ArrayList<Location> mLocationArea) {
        super(mTransactionID, mMessageType);
        this.mLocationArea = mLocationArea;
    }

    @Override
    public String translate() {
        //Define return result
        String jSonResult = "";
        try{
            JSONObject object = new JSONObject();
            object.put("mTransactionID",RestrictedAreaMessage.this.mTransactionID);
            object.put("mMessageType",RestrictedAreaMessage.this.mMessageType);

            JSONArray array = new JSONArray();
            for (Location location:mLocationArea){
                JSONObject locObj = new JSONObject();
                locObj.put("mLng",location.mLng);
                locObj.put("mLat",location.mLat);

                array.put(locObj);
            }

            object.put("mLocationArea",array);

            jSonResult = object.toString();

        }catch (JSONException e){
            Log.e(LOG_TAG, "JSONException accured!");
            e.printStackTrace();
        }
        Log.d(LOG_TAG,"Output json format is "+ jSonResult);
        return jSonResult;
    }

    @Override
    public String toString() {
        return "RestrictedAreaMessage ["
                + super.toString()
                + ", mLocationArea=" + mLocationArea.toString()
                + "]";
    }
}
