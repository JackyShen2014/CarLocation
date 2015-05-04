package com.carlocation.comm.messaging;

import java.io.Serializable;

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
	protected long mTransactionID;
	
	
	protected MessageType mMessageType;

    protected BaseMessage() {
        super();
    }

    public BaseMessage(long mTransactionID) {
		super();
		this.mTransactionID = mTransactionID;
	}

    protected BaseMessage(long mTransactionID, MessageType mMessageType) {
        this.mTransactionID = mTransactionID;
        this.mMessageType = mMessageType;
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
	public abstract JSONObject translateJsonObject();
	
	

	public long getTransationID() {
		return mTransactionID;
	}


	public void setTransationID(long transactionID) {
		this.mTransactionID = transactionID;
	}


	public MessageType getMessageType() {
		return mMessageType;
	}


	public void setMessageType(MessageType messageType) {
		this.mMessageType = messageType;
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
                + "]";
	}

	
	
}
