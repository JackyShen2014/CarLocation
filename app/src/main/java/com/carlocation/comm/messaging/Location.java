package com.carlocation.comm.messaging;

public class Location {

	public String id;
	
	public double lat;
	
	public double lng;
	
	public LocationTerminalType type;

	public Location(String id, LocationTerminalType type, double lat, double lng) {
		super();
		this.id = id;
		this.lat = lat;
		this.lng = lng;
		this.type = type;
	}
	
	
}
