package com.carlocation.comm.messaging;

/**
 * Created by Jacky on 2015/5/1.
 * @author Jacky Shen
 */
public enum  ActionType {
    /**
     * Used for server send assign msg to mobile Pad.
     * Such as TaskAssignmentMsg, GlidePathMsg, RestrictedAreaMsg.
     */
    ACTION_ASSIGN(0),

    /**
     * Used for mobile pad to query dedicated content from server.
     * Such as content of Task, GlidePath, RestrictedArea.
     */
    ACTION_QUERY(1),

    /**
     * Used for mobile pad to indicate server that it begin a task.
     */
    ACTION_START(2),

    /**
     * Used for mobile pad to indicate server that it have finished a task.
     */
    ACTION_FINISH(3),
    /**
     * Unknown action.
     */
    ACTION_UNKNOWN(-1);

    private int code;

    ActionType(int code) {
        this.code = code;
    }

    public static ActionType valueOf(int code){
        switch (code){
            case 0:
                return ACTION_ASSIGN;
            case 1:
                return ACTION_QUERY;
            case 2:
                return ACTION_START;
            case 3:
                return ACTION_FINISH;
            default:return ACTION_UNKNOWN;

        }
    }
}
