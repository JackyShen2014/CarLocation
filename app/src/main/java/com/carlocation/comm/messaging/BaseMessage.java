package com.carlocation.comm.messaging;

import android.util.JsonReader;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Basic message class
 * @author 28851274
 * @author Jacky Shen
 *
 */
public abstract class BaseMessage implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2385050163261203857L;

	
	/**
	 * Transaction id
	 */
	public long mTransactionID;
	public MessageType mMessageType;
    public String mSenderId;
    public TerminalType mSenderType;

    public BaseMessage() {
        super();
    }

    public BaseMessage(long mTransactionID) {
		super();
		this.mTransactionID = mTransactionID;
	}

    public BaseMessage(long mTransactionID, MessageType mMessageType) {
        this.mTransactionID = mTransactionID;
        this.mMessageType = mMessageType;
    }

    public BaseMessage(long mTransactionID, MessageType mMessageType, String mSenderId,
                          TerminalType mSenderType) {
        this.mTransactionID = mTransactionID;
        this.mMessageType = mMessageType;
        this.mSenderId = mSenderId;
        this.mSenderType = mSenderType;
    }

	public BaseMessage(JsonReader reader) {

	}

    /**
	 * Use to translate to network format
	 * @return
	 */
	public abstract String translate();
	
	
	/**
	 * 
	 * @return
	 */
	public JSONObject translateJsonObject() {
		try{
			JSONObject object = new JSONObject();
			object.put("mTransactionID",this.mTransactionID);
			object.put("mMessageType",this.mMessageType.ordinal());
			object.put("mSenderId",this.mSenderId);
			object.put("mSenderType",this.mSenderType.ordinal());

			return object;

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}


	public MessageType getMessageType() {
		return mMessageType;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (int) (mTransactionID ^ (mTransactionID >>> 32));
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
        BaseMessage other = (BaseMessage) obj;
		if (mTransactionID != other.mTransactionID)
			return false;
		return true;
	}



	@Override
	public String toString() {
		return "Message [mTransactionID=" + mTransactionID
                + ", mMessageType=" + mMessageType
                + ", mSenderId=" + mSenderId
                + ", mSenderType=" + mSenderType
                + "]";
	}

	
	
}
