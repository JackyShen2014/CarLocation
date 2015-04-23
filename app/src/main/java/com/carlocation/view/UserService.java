package com.carlocation.view;

import android.util.Log;

import com.carlocation.comm.IMessageService;
import com.carlocation.comm.ResponseListener;
import com.carlocation.comm.messaging.AuthMessage;
import com.carlocation.comm.messaging.Notification;

/**
 * Created by 28851620 on 4/21/2015.
 */
public class UserService {

    private final String LOG_TAG = "UserService";

    //Native Service
    private IMessageService mNativeService;

    public UserService(IMessageService service) {
        if(service != null){
            this.mNativeService = service;
        }else {
            Log.e(LOG_TAG,"UserService constructor service is null");
        }

    }

    /**
     * This is used to send LOGIN MSG to Server from UI
     * @param username
     * @param pwd
     */
    public void logIn(String username, String pwd) {
        //Construct a new AUTH LOGIN MSG
        if (username==null || username.equals("") || pwd==null || pwd.equals("")){
            Log.e(LOG_TAG, "Invalid username: "+ username +"or pwd:" + pwd);
            return;
        }
        final AuthMessage authMsg = new AuthMessage(username,pwd, AuthMessage.AuthMsgType.AUTH_LOGIN_MSG);


        // Invoke native service to send message
        Log.d(LOG_TAG,"Start invoke native service to send message LOG IN.");
        if(mNativeService!= null){
            mNativeService.sendMessage(authMsg,new ResponseListener() {
                @Override
                public void onResponse(Notification noti) {
                    //TODO add call back for handling LOGIN Rsp
                    authMsg.onResponseHandler(noti);

                }
            });
        }else {
            Log.e(LOG_TAG,"It seems failed to bind service mNativeService = "+mNativeService);
        }


    }

    public void logOut(String username, String pwd){
        //Construct a new AUTH LOGOUT MSG
        final AuthMessage authMsg = new AuthMessage(username, pwd, AuthMessage.AuthMsgType.AUTH_LOGOUT_MSG);

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
