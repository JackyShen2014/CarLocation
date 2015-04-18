package com.carlocation.comm.messaging;

/**
 * Use to authentication
 * @author 28851274
 *
 */
public class AuthMessage extends Message {

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7313293501889870528L;

	
	private String mUserName;
	
	private String mPassword;
	
	private AuthType mAuthType;
	
	
	public AuthMessage(String mUserName, String mPassword) {
		super(System.currentTimeMillis());
		this.mUserName = mUserName;
		this.mPassword = mPassword;
		super.mMessageType = MessageType.AUTH_MESSAGE;
	}


	
	
	
	
	public String getUserName() {
		return mUserName;
	}





	public void setUserName(String userName) {
		this.mUserName = userName;
	}





	public String getPassword() {
		return mPassword;
	}





	public void setPassword(String password) {
		this.mPassword = password;
	}
	
	
	

	
	public AuthType getAuthType() {
		return mAuthType;
	}






	public void setAuthType(AuthType authType) {
		this.mAuthType = authType;
	}





	public enum AuthType {
		LOGIN,
		LOGOUT
	}




	@Override
	public String translate() {
		return null;
	}

}
