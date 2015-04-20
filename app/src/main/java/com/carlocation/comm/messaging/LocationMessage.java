package com.carlocation.comm.messaging;

/**
 * Location message.<br>
 * Maybe contain one location or multi locations.
 * @author 28851274
 *
 */
public class LocationMessage extends Message {

	private static final long serialVersionUID = -1427264506745698504L;

    private long mTerminalId;
    private TerminalType mTerminalType;
    private Location mLocation;
    private float mSpeed;

    public LocationMessage(){
        super(System.currentTimeMillis());
        this.mMessageType = MessageType.LOCATION_MESSAGE;
    }

	public LocationMessage(long terminalId, TerminalType terminalType, double longitude,
                           double latitude, float speed) {
        super(System.currentTimeMillis());
        this.mTerminalId = terminalId;
        this.mTerminalType = terminalType;
        this.mLocation.mLng = longitude;
        this.mLocation.mLat = latitude;
        this.mSpeed = speed;
        this.mMessageType = MessageType.LOCATION_MESSAGE;
	}



	@Override
	public String translate() {
		return null;
	}

    /**
     * Used for logging
     * @return
     */
    @Override
    public String toString() {
        return "LocationMessage [mVehicleId=" + mTerminalId
                + ", mVehicleType=" + mTerminalType
                + ", mLng=" + mLocation.mLng
                + ", mLat=" + mLocation.mLat
                + ", mSpeed=" + mSpeed
                + "]";
    }

}
