package com.carlocation.comm;

import java.io.Serializable;

public enum ConnectionState implements Serializable {

	CONNECTED, NO_CONNECTION, SERVER_REJECT, CONNECTING, SERVER_FAILED, CONNECT_FAILED
}
