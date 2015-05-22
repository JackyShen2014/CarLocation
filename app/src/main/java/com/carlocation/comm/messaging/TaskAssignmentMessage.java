package com.carlocation.comm.messaging;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 28851620 on 4/22/2015.
 *
 * 
 * @author Jacky Shen
 */

public class TaskAssignmentMessage extends BaseMessage {

	private static final long serialVersionUID = 3013175663486838455L;
	private final String LOG_TAG = "TaskAssignmentMessage";

	public ActionType mActionType;
	public short mTaskId;
	public String mTaskContent;
    public RankType mRank;

    public TaskAssignmentMessage(long mTransactionID,String mSenderId,ActionType mActionType,
                                 short mTaskId, String mTaskContent) {
        super(mTransactionID, MessageType.TASK_MESSAGE, mSenderId, TerminalType.TERMINAL_CAR);
        this.mActionType = mActionType;
        this.mTaskId = mTaskId;
        this.mTaskContent = mTaskContent;
    }

    public TaskAssignmentMessage(long mTransactionID,String mSenderId,ActionType mActionType,
                                 short mTaskId, String mTaskContent, RankType mRank) {
        super(mTransactionID, MessageType.TASK_MESSAGE, mSenderId, TerminalType.TERMINAL_CAR);
        this.mActionType = mActionType;
        this.mTaskId = mTaskId;
        this.mTaskContent = mTaskContent;
        this.mRank = mRank;
    }

    @Override
    public String translate() {
        //Define return result
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

			object.put("mActionType", mActionType.ordinal());
			object.put("mTaskId", mTaskId);
			object.put("mTaskContent", mTaskContent);
            if(mRank!=null){
                object.put("mRank",mRank.ordinal());
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
		return "TaskAssignmentMessage [" + super.toString()
                + ", mActionType=" + mActionType
                + ", mTaskId=" + mActionType
                + ", mTaskId=" + mTaskId
                + ", mRank=" + mRank
                + ", mTaskContent=" + mTaskContent + "]";
	}
}
