package com.epam.asset.tracking.domain;

import java.util.Date;

public class Event {
	
	private String summary;
	private String Description;
	private Date date;
	private String businessProviderId;
	
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getDescription() {
		return Description;
	}
	public void setDescription(String description) {
		Description = description;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getBusinessProviderId() {
		return businessProviderId;
	}
	public void setBusinessProviderId(String businessProviderId) {
		this.businessProviderId = businessProviderId;
	}
	
	
	

}
