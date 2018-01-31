package com.epam.asset.tracking.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.annotation.NumberFormat.Style;

import com.epam.asset.tracking.dto.validations.ValidationsUtil;

public class EntityDTO {

	// Name --> Text, not allowed numbers or symbols
	@NotEmpty
	@Length(max = 140)
	@Pattern(message = "Not allowed numbers or symbols", regexp = ValidationsUtil.LETTERS_WITH_SPACE)
	private String name;

	// Last name --> Text, not allowed numbers or symbols
	@NotEmpty
	@Length(max = 140)
	@Pattern(message = "Not allowed numbers or symbols", regexp = ValidationsUtil.LETTERS_WITH_SPACE)
	private String lastName;

	// User Name--> Text, not allowed numbers or symbols --> validation (user name
	// unique)
	@NotEmpty
	@Length(max = 30)
	@Pattern(message = "Not allowed numbers or symbols", regexp = ValidationsUtil.LETTERS_AND_NUMBERS)
	private String username;

	// Password --> text, numbers, symbols
	@NotEmpty
	@Length(min = 8, max = 10)
	private String password;

	// Type of business --> List (Computer sellers, computer repairers, house
	// sellers, house brokers, car sellers, mechanics)
	@NotEmpty
	@Length(min = 3, max = 60)
	@Pattern(message = "Not allowed numbers or symbols", regexp = ValidationsUtil.LETTERS_WITH_SPACE)
	private String businessType;

	// Address --> text and numbers
	@NotEmpty
	@Length(min = 3, max = 256)
	@Pattern(message = "Not allowed symbols", regexp = ValidationsUtil.LETTERS_AND_NUMBERS_WITH_SPACE_COMMA_AND_PERIOD)
	private String address;

	// City --> text, not allowed numbers or symbols
	@NotEmpty
	@Length(min = 2, max = 256)
	@Pattern(message = "Not allowed numbers or symbols", regexp = ValidationsUtil.LETTERS_WITH_SPACE)
	private String city;

	// State --> text, not allowed numbers or symbols
	@NotEmpty
	@Length(min = 2, max = 256)
	@Pattern(message = "Not allowed numbers or symbols", regexp = ValidationsUtil.LETTERS_WITH_SPACE)
	private String state;

	// ZipCode --> just numbers validated just 5
	@NotEmpty
	@Length(min = 5, max = 5)
	@NumberFormat(style = Style.NUMBER)
	@Min(0)
	@Max(99999)
	private String zipCode;

	// RFC --> Text and numbers not symbols
	@NotEmpty
	@Length(max = 13)
	@Pattern(message = "Not allowed numbers or symbols", regexp = "^[A-Za-z0-9]*$")
	private String rfc;

	@Email
	private String email;

	//@NotEmpty
	String role;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}
	
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public String getEmail() {
		return email;
	  }

	  public void setEmail(String email) {
		this.email = email;
	  }

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}
