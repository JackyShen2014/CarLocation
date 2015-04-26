package com.carlocation.comm.messaging;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 28851620 on 4/22/2015.
 * @author Jacky Shen
 */

public class TaskAssignmentMessage extends Message {

    private static final long serialVersionUID = 3013175663486838455L;
    private final String LOG_TAG = "TaskAssignmentMessage";

    public long mTerminalId;
    public short mTaskId;
    public TaskMsgType mTaskType;

    public static enum TaskMsgType{
        TASK_INITIAL_MSG,
        TASK_BEGIN_MSG,
        TASK_FINISH_MSG,
        TASK_CLEAN_MSG,
        TASK_QUERY_MSG,
    }

    public TaskAssignmentMessage (long mTransactionID, long mTerminalId, short mTaskId, TaskMsgType mTaskType) {
        super(mTransactionID);
        this.mTerminalId = mTerminalId;
        this.mTaskId = mTaskId;
        this.mTaskType = mTaskType;
    }

    public TaskAssignmentMessage(long mTransactionID, MessageType mMessageType, long mTerminalId,
                                 short mTaskId, TaskMsgType mTaskType) {
        super(mTransactionID, mMessageType);
        this.mTerminalId = mTerminalId;
        this.mTaskId = mTaskId;
        this.mTaskType = mTaskType;
    }

    @Override
    public String translate() {
        //Define return result
        String jSonResult = "";
        try{
            JSONObject object = new JSONObject();
            object.put("mTransactionID",TaskAssignmentMessage.this.mTransactionID);
            object.put("mMessageType",TaskAssignmentMessage.this.mMessageType);
            object.put("mTerminalId",mTerminalId);
            object.put("mTaskId",mTaskId);
            object.put("mTaskType",mTaskType);

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
        return "TaskAssignmentMessage ["
                + super.toString()
                + "mTerminalId=" + mTerminalId
                + ", mTaskId=" + mTaskId
                + ", mTaskType=" + mTaskType
                + "]";
    }
}
