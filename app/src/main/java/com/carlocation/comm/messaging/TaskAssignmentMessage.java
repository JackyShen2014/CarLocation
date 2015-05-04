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
	public long mTerminalId;
	public short mTaskId;
	public String mTaskContent;

    public TaskAssignmentMessage(long mTransactionID,
                                 ActionType mActionType, long mTerminalId,
                                 short mTaskId, String mTaskContent) {
        super(mTransactionID, MessageType.TASK_MESSAGE);
        this.mActionType = mActionType;
        this.mTerminalId = mTerminalId;
        this.mTaskId = mTaskId;
        this.mTaskContent = mTaskContent;
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
			JSONObject object = new JSONObject();
			object.put("mTransactionID",
					TaskAssignmentMessage.this.mTransactionID);
			object.put("mMessageType", TaskAssignmentMessage.this.mMessageType.ordinal());
			object.put("mActionType", mActionType.ordinal());
			object.put("mTerminalId", mTerminalId);
			object.put("mTaskId", mTaskId);
			object.put("mTaskContent", mTaskContent);

            return object;

		} catch (JSONException e) {
			Log.e(LOG_TAG, "JSONException accured!");
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String toString() {
		return "TaskAssignmentMessage [" + super.toString() + ", mActionType="
				+ mActionType + ", mTerminalId=" + mTerminalId + ", mTaskId="
				+ mTaskId + ", mTaskContent=" + mTaskContent + "]";
	}
}
