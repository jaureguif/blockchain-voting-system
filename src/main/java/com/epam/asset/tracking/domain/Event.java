package com.epam.asset.tracking.domain;

import java.time.ZonedDateTime;

import com.epam.asset.tracking.mapper.converter.serialize.ZonedDateTimeDeserializer;
import com.epam.asset.tracking.mapper.converter.serialize.ZonedDateTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class Event {

  private String summary;
  private String description;
  @JsonSerialize(using = ZonedDateTimeSerializer.class)
  @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
  private ZonedDateTime date;
  private String businessProviderId;
  private String encodedImage;
  private String attachment;

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

  public String getAttachment() {
    return attachment;
  }

  public void setAttachment(String attachment) {
    this.attachment = attachment;
  }



}
