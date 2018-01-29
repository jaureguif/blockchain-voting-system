package com.epam.asset.tracking.entities;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.format.annotation.NumberFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

/**
 * Created on 1/25/2018.
 */
public class AnEntity {
    // Name --> Text, not allowed numbers or symbols
    String name;

    // Last name --> Text, not allowed numbers or symbols

    String lastName;

    // User Name--> Text, not allowed numbers or symbols --> validation (user name
    // unique)

    String userName;

    // Password --> text, numbers, symbols

    String password;

    // Type of business --> List (Computer sellers, computer repairers, house
    // sellers, house brokers, car sellers, mechanics)

    String businessType;

    // Address --> text and numbers

    String address;

    // City --> text, not allowed numbers or symbols

    String city;

    // ZipCode --> just numbers validated just 5

    String zipCode;

    // RFC --> Text and numbers not symbols

    String rfc;

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
}
