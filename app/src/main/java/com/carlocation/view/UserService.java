package com.carlocation.view;

import com.carlocation.comm.IMessageService;
import com.carlocation.comm.ResponseListener;
import com.carlocation.comm.messaging.AuthMessage;
import com.carlocation.comm.messaging.Notification;

/**
 * Created by 28851620 on 4/21/2015.
 */
public class UserService {

    //Native Service
    private IMessageService mNativeService;

    public UserService(IMessageService service) {
        this.mNativeService = service;
    }

    /**
     * This is used to send LOGIN MSG to Server from UI
     * @param username
     * @param pwd
     */
    public void logIn(String username, String pwd) {
        //Construct a new AUTH LOGIN MSG
        final AuthMessage authMsg = new AuthMessage(username, pwd);
        authMsg.setAuthType(AuthMessage.AuthMsgType.AUTH_LOGIN_MSG);

        // Invoke native service to send message
        mNativeService.sendMessage(authMsg,new ResponseListener() {
            @Override
            public void onResponse(Notification noti) {
                //TODO add call back for handling　Rsp
                authMsg.onResponseHandler(noti);

            }
        });

    }

    public void logOut(String username, String pwd){
        //Construct a new AUTH LOGOUT MSG
        final AuthMessage authMsg = new AuthMessage(username, pwd);
        authMsg.setAuthType(AuthMessage.AuthMsgType.AUTH_LOGOUT_MSG);

        // Invoke native service to send message
        mNativeService.sendMessage(authMsg,new ResponseListener() {
            @Override
            public void onResponse(Notification noti) {
                //TODO add call back for handling LOGOUT　Rsp
                authMsg.onResponseHandler(noti);
            }
        });

    }

}
