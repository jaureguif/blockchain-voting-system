package com.epam.asset.tracking.domain;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Asset {

  private UUID uuid;
  private String serialNumber;
  private String assetType;
  private List<Event> events;
  private String ownerName;
  private String description;

  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID id) {
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

  public List<Event> getEvents() {
    if (events == null) {
      events = new ArrayList<>();
    }
    return events;
  }

  public void setEvents(List<Event> events) {
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
