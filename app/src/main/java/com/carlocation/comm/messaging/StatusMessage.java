package com.carlocation.comm.messaging;

/**
 * Created by 28851620 on 4/22/2015.
 */
public class StatusMessage extends Message {

    public long mTerminalId;
    public StatusMsgType mStatus;
    public UserType mUserType;

    public StatusMessage(long mTransactionID, long mTerminalId, StatusMsgType mStatus, UserType mUserType) {
        super(mTransactionID);
        this.mTerminalId = mTerminalId;
        this.mStatus = mStatus;
        this.mUserType = mUserType;
    }

    /**
     * Used for indicate where the msg comes from
     */
    public static enum UserType{
        MOBILE_PAD,
        CONTROL_PC,
    }

    /**
     * Used for indicate current status
     */
    public static enum StatusMsgType{
        STATUS_ONLINE,
        STATUS_OFFLINE,
        STATUS_LEAVE,
    }

    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * Use to translate to network format
     *
     * @return
     */
    @Override
    public String translate() {
        return null;
    }
}
