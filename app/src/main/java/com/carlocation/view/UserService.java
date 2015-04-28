package com.carlocation.view;

import android.content.Context;
import android.util.Log;

import com.carlocation.comm.IMessageService;
import com.carlocation.comm.NotificationListener;
import com.carlocation.comm.ResponseListener;
import com.carlocation.comm.messaging.AuthMessage;
import com.carlocation.comm.messaging.Location;
import com.carlocation.comm.messaging.LocationMessage;
import com.carlocation.comm.messaging.MessageType;
import com.carlocation.comm.messaging.Notification;
import com.carlocation.comm.messaging.TerminalType;

import java.util.Random;

/**
 * Created by 28851620 on 4/21/2015.
 * @author Jacky Shen
 */
public class UserService{

    private final String LOG_TAG = "UserService";

    //Native Service
    private IMessageService mNativeService;
    public ResponseListener mRspListener;

    public UserService(IMessageService service, ResponseListener rspListener) {
        this.mRspListener = rspListener;
        if(service != null){
            this.mNativeService = service;
        }else {
            Log.e(LOG_TAG,"UserService constructor service is null");
        }
    }

    /**
     * Used to send login MSG.
     * @param username
     * @param pwd
     */
    public void logIn(String username, String pwd) {
        if (username==null || username.equals("") || pwd==null || pwd.equals("")){
            Log.e(LOG_TAG, "Invalid username: "+ username +"or pwd:" + pwd);
            return;
        }

        //FIXME Get TransactionID
        Random rand = new Random();
        long transactionID = rand.nextLong();

        //FIXME Get Terminal ID
        long terminalId = rand.nextLong();

        //Construct a new Login MSG
        AuthMessage authMsg = new AuthMessage(transactionID,MessageType.AUTH_MESSAGE,terminalId,
                username,pwd, AuthMessage.AuthMsgType.AUTH_LOGIN_MSG);


        // Invoke native service to send message
        Log.d(LOG_TAG,"Start invoke native service to send message login.");
        if(mNativeService!= null){
            mNativeService.sendMessage(authMsg,mRspListener);
        }else {
            Log.e(LOG_TAG,"It seems failed to bind service mNativeService = "+mNativeService);
        }

    }

    /**
     * Used to send logout MSG.
     * @param username
     * @param pwd
     */
    public void logOut(String username, String pwd){
        if (username==null || username.equals("") || pwd==null || pwd.equals("")){
            Log.e(LOG_TAG, "Invalid username: "+ username +"or pwd:" + pwd);
            return;
        }

        //FIXME Get TransactionID
        Random rand = new Random();
        long transactionID = rand.nextLong();

        //FIXME Get Terminal ID from property
        long terminalId = rand.nextLong();

        //Construct a new AUTH LOGOUT MSG
        AuthMessage authMsg = new AuthMessage(transactionID,MessageType.AUTH_MESSAGE,terminalId,
                username,pwd, AuthMessage.AuthMsgType.AUTH_LOGOUT_MSG);

        // Invoke native service to send message
        Log.d(LOG_TAG, "Start invoke native service to send message logout.");
        if(mNativeService!= null){
            mNativeService.sendMessage(authMsg,mRspListener);
        }else {
            Log.e(LOG_TAG,"It seems failed to bind service mNativeService = "+mNativeService);
        }

    }

    /**
     * Used to update my locaiton to server.
     */
    public void updateMyLocation (){

        //FIXME Get TransactionID
        Random rand = new Random();
        long transactionID = rand.nextLong();

        //FIXME Get Terminal ID from property
        long terminalId = rand.nextLong();

        //FIXME Get Terminal Type from property
        TerminalType terminalType = TerminalType.TERMINAL_CAR;

        LocationMessage myLocationMsg = new LocationMessage(transactionID,MessageType.LOCATION_MESSAGE,terminalId,
                terminalType,getMyLocation(),getMySpeed());

        // Invoke native service to send message
        Log.d(LOG_TAG, "updateMyLocation(): Start invoke native service to send LocationMessage.");
        if(mNativeService!= null){
            mNativeService.sendMessage(myLocationMsg);
        }else {
            Log.e(LOG_TAG,"updateMyLocation():It seems failed to bind service mNativeService = "+mNativeService);
        }

    }

    /**
     * Used to retrieve my location from BeiDou positioning system
     * @return
     */
    public Location getMyLocation(){
        //FIXME Get Location from BeiDou positioning system
        Location myLocation = new Location(111.111,222.222);
        return myLocation;
    }


    /**
     * Used to retrieve my speed from BeiDou positioning system
     * @return
     */
    public float getMySpeed(){
        float mySpeed = 12.23f;
        return mySpeed;
    }

}
