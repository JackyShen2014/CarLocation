package com.carlocation.comm.messaging;

public class AssignmentMessage extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3013175663486838455L;

	private long mAssignId;
	
	private String mCarId;
	
	private OperationType mOpt;
	
	
	public AssignmentMessage(long mAssignId, String mCarId,
			OperationType mOpt) {
		super(System.currentTimeMillis());
		this.mAssignId = mAssignId;
		this.mCarId = mCarId;
		this.mOpt = mOpt;
		this.mMessageType = MessageType.TASK_MESSAGE;
	}
	
	
	


	public long getAssignId() {
		return mAssignId;
	}





	public void setAssignId(long assignId) {
		this.mAssignId = assignId;
	}





	public String getCarId() {
		return mCarId;
	}





	public void setCarId(String carId) {
		this.mCarId = carId;
	}





	public OperationType getOpt() {
		return mOpt;
	}





	public void setOpt(OperationType opt) {
		this.mOpt = opt;
	}





	@Override
	public String translate() {
		return null;
	}

	
	public enum OperationType {
		BEGIN,
		END,
		QUERY
	}
}
