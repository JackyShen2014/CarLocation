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
    ACTION_ASSIGN,

    /**
     * Used for mobile pad to query dedicated content from server.
     * Such as content of Task, GlidePath, RestrictedArea.
     */
    ACTION_QUERY,

    /**
     * Used for mobile pad to indicate server that it begin a task.
     */
    ACTION_START,

    /**
     * Used for mobile pad to indicate server that it have finished a task.
     */
    ACTION_FINISH,
}
