package com.carlocation.comm.messaging;

import java.util.ArrayList;
import java.util.List;


/**
 * Location message.<br>
 * Maybe contain one location or multi locations.
 * @author 28851274
 *
 */
public class LocationMessage extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1427264506745698504L;
	private List<Location> mLocations;
	
	
	public LocationMessage(List<Location> mLocations) {
		super(System.currentTimeMillis());
		this.mLocations = mLocations;
		this.mMessageType = MessageType.LOCATION_MESSAGE;
	}



	public LocationMessage(Location location) {
		this(new ArrayList<Location>());
		this.mLocations.add(location);
	}
	
	public List<Location> getLocations() {
		return this.mLocations;
	}
	
	public void addLocation(Location loc) {
		this.mLocations.add(loc);
	}

	@Override
	public String translate() {
		return null;
	}

}
