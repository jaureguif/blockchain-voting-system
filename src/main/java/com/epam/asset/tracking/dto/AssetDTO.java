package com.epam.asset.tracking.dto;

import java.util.Set;

import org.hibernate.validator.constraints.NotEmpty;

import com.epam.asset.tracking.domain.Event;

public class AssetDTO {
	
	@NotEmpty
	private String id;
	@NotEmpty
	private String serialNumber;
	@NotEmpty
	private String assetType;
	@NotEmpty
	private Set<Event> events;
	@NotEmpty
	private String ownerName;
	@NotEmpty
	private String description;
	
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
