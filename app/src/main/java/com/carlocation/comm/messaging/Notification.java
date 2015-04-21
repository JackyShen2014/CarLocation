package com.carlocation.comm.messaging;

public class Notification {

    public Message message;

    public NotificationType notiType;

    public Result result;

    public enum NotificationType {
        REQUEST,
        RESPONSE,
        UNSOLICITED,
    }


    public enum Result {
        SUCCESS,
        FAILED,
        TIME_OUT,
        SERVER_RJECT,
    }


    public Notification(Message message, NotificationType notiType, Result result) {
        super();
        this.message = message;
        this.notiType = notiType;
        this.result = result;
    }




}