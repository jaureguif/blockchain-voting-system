package com.epam.asset.tracking.dto;

import com.epam.asset.tracking.domain.BUSINESS_TYPE;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

/**
 * Provides/registers assets in the application.
 *
 * @author daniel_pedraza@epam.com
 */

@Document
public class BusinessProviderDTO  {

	private String name;
	private String lastName;
	private String rfc;
	private BUSINESS_TYPE businessType;
	private AddressDTO address;
	private String username;
	private String email;

	public String getName() {
		return name;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public BUSINESS_TYPE getBusinessType() {
		return businessType;
	}

	public void setBusinessType(BUSINESS_TYPE businessType) {
		this.businessType = businessType;
	}

	public AddressDTO getAddress() {
		return address;
	}

	public void setAddress(AddressDTO address) {
		this.address = address;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BusinessProviderDTO that = (BusinessProviderDTO) o;
		return Objects.equals(name, that.name) &&
				Objects.equals(lastName, that.lastName) &&
				Objects.equals(rfc, that.rfc) &&
				businessType == that.businessType &&
				Objects.equals(address, that.address) &&
				Objects.equals(username, that.username) &&
				Objects.equals(email, that.email);
	}

	@Override
	public int hashCode() {

		return Objects.hash(name, lastName, rfc, businessType, address, username, email);
	}

	@Override
	public String toString() {
		return "{ " + "username:\"" + getUsername() + '"' + ", name:\"" + name + '"' + ", rfc:\"" + rfc + '"' + ", businessType:\"" + businessType
				+ '"' + ", address:" + address + " }";
	}
}
