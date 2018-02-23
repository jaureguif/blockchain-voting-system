package com.epam.asset.tracking.domain;

import java.time.ZonedDateTime;

public class Event {

  private String summary;
  private String description;
  private ZonedDateTime date;
  private String businessProviderId;
  private String encodedImage;
  private String encodedFiles;

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ZonedDateTime getDate() {
    return date;
  }

  public void setDate(ZonedDateTime date) {
    this.date = date;
  }

  public String getBusinessProviderId() {
    return businessProviderId;
  }

  public void setBusinessProviderId(String businessProviderId) {
    this.businessProviderId = businessProviderId;
  }

  public String getEncodedImage() {
    return encodedImage;
  }

  public void setEncodedImage(String encodedImage) {
    this.encodedImage = encodedImage;
  }

  public String getEncodedFiles() {
    return encodedFiles;
  }

  public void setEncodedFiles(String encodedFiles) {
    this.encodedFiles = encodedFiles;
  }



}
