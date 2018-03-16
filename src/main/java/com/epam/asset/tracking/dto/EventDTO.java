package com.epam.asset.tracking.dto;

import java.time.ZonedDateTime;

import org.hibernate.validator.constraints.NotBlank;

public class EventDTO {

  private @NotBlank String summary;
  private @NotBlank String description;

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
}
