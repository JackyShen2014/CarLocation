package com.carlocation.view;

import android.util.Log;

import com.carlocation.comm.IMessageService;
import com.carlocation.comm.ResponseListener;
import com.carlocation.comm.messaging.ActionType;
import com.carlocation.comm.messaging.AuthMessage;
import com.carlocation.comm.messaging.BaseMessage;
import com.carlocation.comm.messaging.GlidingPathMessage;
import com.carlocation.comm.messaging.IMMessage;
import com.carlocation.comm.messaging.IMTxtMessage;
import com.carlocation.comm.messaging.IMVoiceMessage;
import com.carlocation.comm.messaging.Location;
import com.carlocation.comm.messaging.LocationMessage;
import com.carlocation.comm.messaging.MessageResponseStatus;
import com.carlocation.comm.messaging.MessageType;
import com.carlocation.comm.messaging.RestrictedAreaMessage;
import com.carlocation.comm.messaging.StatusMessage;
import com.carlocation.comm.messaging.TaskAssignmentMessage;
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
     * Used to send login MSG. The response result will be handled by mRspListener.
     * @param username
     * @param pwd
     */
    public void logIn(String username, String pwd) {
        if (username==null || username.equals("") || pwd==null || pwd.equals("")){
            Log.e(LOG_TAG, "Invalid username: "+ username +"or pwd:" + pwd);
            return;
        }

        //Construct a new Login MSG
        AuthMessage authMsg = new AuthMessage(getTransactionId(),MessageType.AUTH_MESSAGE,getTerminalId(),
                username,pwd, AuthMessage.AuthMsgType.AUTH_LOGIN_MSG);


        // Invoke native service to send message
        Log.d(LOG_TAG,"logIn():Start invoke native service to send message login.");
        if(mNativeService!= null){
            mNativeService.sendMessage(authMsg,mRspListener);
        }else {
            Log.e(LOG_TAG,"logIn():It seems failed to bind service mNativeService = "+mNativeService);
        }

    }

    /**
     * Used to send logout MSG. The response result will be handled by mRspListener.
     * @param username
     * @param pwd
     */
    public void logOut(String username, String pwd){
        if (username==null || username.equals("") || pwd==null || pwd.equals("")){
            Log.e(LOG_TAG, "Invalid username: "+ username +"or pwd:" + pwd);
            return;
        }

        //Construct a new AUTH LOGOUT MSG
        AuthMessage authMsg = new AuthMessage(getTransactionId(),MessageType.AUTH_MESSAGE,getTerminalId(),
                username,pwd, AuthMessage.AuthMsgType.AUTH_LOGOUT_MSG);

        // Invoke native service to send message
        Log.d(LOG_TAG, "logOut():Start invoke native service to send message logout.");
        if(mNativeService!= null){
            mNativeService.sendMessage(authMsg,mRspListener);
        }else {
            Log.e(LOG_TAG,"logOut():It seems failed to bind service mNativeService = "+mNativeService);
        }

    }

    /**
     * Used to send my current status(online, leave, offline) to server.
     * @param status
     */
    void sendMyStatus(StatusMessage.StatusMsgType status){
        StatusMessage statMsg = new StatusMessage(getTransactionId(),MessageType.STATUS_MESSAGE,getTerminalId(),status, StatusMessage.UserType.MOBILE_PAD);

        // Invoke native service to send message
        Log.d(LOG_TAG, "sendMyStatus():Start invoke native service to send status message.");
        if(mNativeService!= null){
            mNativeService.sendMessage(statMsg);
        }else {
            Log.e(LOG_TAG,"sendMyStatus():It seems failed to bind service mNativeService = "+mNativeService);
        }

    }

    /**
     * Used to send my location to server.
     */
    public void sendMyLocation (){
        LocationMessage myLocationMsg = new LocationMessage(getTransactionId(),getTerminalId(),
                getTerminalType(),getMyLocation(),getMySpeed());

        // Invoke native service to send message
        Log.d(LOG_TAG, "sendMyLocation(): Start invoke native service to send LocationMessage.");
        if(mNativeService!= null){
            mNativeService.sendMessage(myLocationMsg);
        }else {
            Log.e(LOG_TAG,"sendMyLocation():It seems failed to bind service mNativeService = "+mNativeService);
        }

    }

    /**
     * Send IM txt msg to another terminal.
     * @param toTerminal    Terminal ID of destination
     * @param rank          Rank of Msg
     * @param content       Content of msg
     */
    public void sendImTxtMsg(long toTerminal, byte rank, String content){
        IMTxtMessage txtMessage = new IMTxtMessage(getTransactionId(),MessageType.IM_MESSAGE,
                getTerminalId(),toTerminal, IMMessage.IMMsgType.IM_TXT_MSG,rank,content);

        // Invoke native service to send message
        Log.d(LOG_TAG, "sendImTxtMsg(): Start invoke native service to send IM txt msg.");
        if(mNativeService!= null){
            mNativeService.sendMessage(txtMessage);
        }else {
            Log.e(LOG_TAG,"sendImTxtMsg():It seems failed to bind service mNativeService = "+mNativeService);
        }

    }

    /**
     * Send IM voice msg to another terminal.
     * @param toTerminal    Terminal ID of destination
     * @param voiceData     Context of voice data
     */
    public void sendImVoiceMsg(long toTerminal, byte[] voiceData){
        IMVoiceMessage voiceMessage = new IMVoiceMessage(getTransactionId(),MessageType.IM_MESSAGE,
                getTerminalId(),toTerminal, IMMessage.IMMsgType.IM_VOICE_MSG,voiceData);

        // Invoke native service to send message
        Log.d(LOG_TAG, "sendImVoiceMsg(): Start invoke native service to send IM voice msg.");
        if(mNativeService!= null){
            mNativeService.sendMessage(voiceMessage);
        }else {
            Log.e(LOG_TAG,"sendImVoiceMsg():It seems failed to bind service mNativeService = "+mNativeService);
        }

    }

    /**
     * send to schedule server to indicate starting to execute given <>taskID</>
     * @param taskId
     */
    public void startWorkMsg(short taskId){
        TaskAssignmentMessage startWorkMsg = new TaskAssignmentMessage(getTransactionId(),
                MessageType.TASK_MESSAGE,ActionType.ACTION_START,getTerminalId(),taskId,null);

        //TODO add to sent queue and removed until success response received.

        // Invoke native service to send message
        Log.d(LOG_TAG, "startWork(): Start invoke native service to send start work msg.");
        if(mNativeService!= null){
            mNativeService.sendMessage(startWorkMsg,mRspListener);
        }else {
            Log.e(LOG_TAG,"startWork():It seems failed to bind service mNativeService = "+mNativeService);
        }


    }

    /**
     * Send to schedule server to indicate having finished the given <>taskID</>
     * @param taskId
     */
    public void finishWorkMsg(short taskId){
        TaskAssignmentMessage finishWorkMsg = new TaskAssignmentMessage(getTransactionId(),
                MessageType.TASK_MESSAGE, ActionType.ACTION_FINISH,getTerminalId(),taskId,null);

        //TODO add to sent queue and removed until success response received.

        // Invoke native service to send message
        Log.d(LOG_TAG, "finishWork(): Start invoke native service to send finish work msg.");
        if(mNativeService!= null){
            mNativeService.sendMessage(finishWorkMsg,mRspListener);
        }else {
            Log.e(LOG_TAG,"finishWork():It seems failed to bind service mNativeService = "+mNativeService);
        }

    }

    /**
     * Send to schedule server to query work ID
     * @param taskId
     */
    public void queryWorkById(short taskId){
        TaskAssignmentMessage queryWorkMsg = new TaskAssignmentMessage(getTransactionId(),
                MessageType.TASK_MESSAGE,ActionType.ACTION_QUERY,getTerminalId(),taskId,null);

        //TODO add to sent queue and removed until success response received.

        // Invoke native service to send message
        Log.d(LOG_TAG, "queryWorkMsg(): Start invoke native service to query work by id msg.");
        if(mNativeService!= null){
            mNativeService.sendMessage(queryWorkMsg,mRspListener);
        }else {
            Log.e(LOG_TAG,"queryWorkMsg():It seems failed to bind service mNativeService = "+mNativeService);
        }

    }

    /**
     * Used to query gliding path by ID from server.
     * @param glidePathId
     */
    void queryGlidePathById(int glidePathId){
        GlidingPathMessage queryPathMsg = new GlidingPathMessage(getTransactionId(),
                MessageType.GLIDE_MESSAGE,ActionType.ACTION_QUERY,getTerminalId(),null,glidePathId,null);

        //TODO add to sent queue and removed until success response received.

        // Invoke native service to send message
        Log.d(LOG_TAG, "queryPathMsg(): Start invoke native service to query glide path by id msg.");
        if(mNativeService!= null){
            mNativeService.sendMessage(queryPathMsg,mRspListener);
        }else {
            Log.e(LOG_TAG,"queryPathMsg():It seems failed to bind service mNativeService = "+mNativeService);
        }
    }

    /**
     * Used to query restricted area by ID from server.
     * @param warnAreaId
     */
    void queryWarnAreaById(int warnAreaId){
        RestrictedAreaMessage queryWarnMsg  = new RestrictedAreaMessage(getTransactionId(),MessageType.WARN_MESSAGE,ActionType.ACTION_QUERY,warnAreaId,null);

        //TODO add to sent queue and removed until success response received.

        // Invoke native service to send message
        Log.d(LOG_TAG, "queryWarnMsg(): Start invoke native service to query warn area by id msg.");
        if(mNativeService!= null){
            mNativeService.sendMessage(queryWarnMsg,mRspListener);
        }else {
            Log.e(LOG_TAG,"queryWarnMsg():It seems failed to bind service mNativeService = "+mNativeService);
        }
    }

    void responActionAssign(BaseMessage message, MessageResponseStatus status){

    }


    /**
     * Used to produce transaction id for send msg.
     * @return
     */
    public long getTransactionId(){
        //FIXME Get TransactionID
        return (new Random().nextLong());
    }

    /**
     * Used to get terminal ID from property.
     * @return
     */
    public long getTerminalId (){
        //FIXME Get Terminal ID from property
        return 1234567L;
    }

    /**
     * Used to get terminal type from property.
     * @return
     */
    public TerminalType getTerminalType(){
        //FIXME Get terminal ID from property
        return TerminalType.TERMINAL_CAR;
    }

    /**
     * Used to retrieve my location from BeiDou positioning system.
     * @return
     */
    public Location getMyLocation(){
        //FIXME Get Location from BeiDou positioning system
        Location myLocation = new Location(111.111,222.222);
        return myLocation;
    }


    /**
     * Used to retrieve my speed from BeiDou positioning system.
     * @return
     */
    public float getMySpeed(){
        //FIXME Get my speed from BeiDou positioning system
        float mySpeed = 12.23f;
        return mySpeed;
    }

}
