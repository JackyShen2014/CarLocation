package com.carlocation.comm;

import android.os.Message;

/**
 * Created by 28851620 on 5/21/2015.
 * @author Jacky Shen
 */
public interface NativeServInterface {

    int REGISTER_NOTIFICATION = 0;
    int HANDLE_NOTIFICATION = 1;

    int NOTIFY_TYPE_REQUEST = 0;
    int NOTIFY_TYPE_RESPONSE = 1;
    int NOTIFY_TYPE_UNSOLICITED = 2;

    int AUTH_MESSAGE = 0;
    int LOCATION_MESSAGE = 1;
    int IM_MESSAGE = 2;
    int TASK_MESSAGE = 3;
    int GLIDE_MESSAGE = 4;
    int WARN_MESSAGE = 5;
    int STATUS_MESSAGE = 6;

    int ACTION_ASSIGN = 0;
    int ACTION_QUERY = 1;
    int ACTION_START = 2;
    int ACTION_FINISH = 3;


    void getUserService();
    void regNotifictListenner();
    void handleResponseMessage(Message msg);
    void handleUnsolicitedMessage(Message msg);

}
