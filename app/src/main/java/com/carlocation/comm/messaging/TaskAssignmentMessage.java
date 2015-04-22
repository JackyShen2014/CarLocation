package com.carlocation.comm.messaging;

/**
 * Created by 28851620 on 4/22/2015.
 */

public class TaskAssignmentMessage extends Message {

    private static final long serialVersionUID = 3013175663486838455L;

    private long mTerminalId;
    private short mTaskId;
    private TaskMsgType mTaskType;

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

    public short getTaskId() {
        return mTaskId;
    }
    public void setTaskId(short taskId) {
        this.mTaskId = taskId;
    }

    public long getTerminalId() {
        return mTerminalId;
    }
    public void setTerninalId(long ternialId) {
        this.mTerminalId = ternialId;
    }

    public TaskMsgType getTaskType() {
        return mTaskType;
    }
    public void setTaskType(TaskMsgType taskType) {
        this.mTaskType = taskType;
    }

    @Override
    public String translate() {
        return null;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
