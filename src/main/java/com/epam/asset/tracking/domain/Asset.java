package com.epam.asset.tracking.domain;


import java.util.Set;

public class Asset {
	
	private String uuid;
	private String serialNumber;
	private String assetType;
	private Set<Event> events;
	private String ownerName;
	private String description;
	
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String id) {
		this.uuid = id;
	}
	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	public String getAssetType() {
		return assetType;
	}
	public void setAssetType(String assetType) {
		this.assetType = assetType;
	}
	public Set<Event> getEvents() {
		return events;
	}
	public void setEvents(Set<Event> events) {
		this.events = events;
	}
	public String getOwnerName() {
		return ownerName;
	}
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	

}
