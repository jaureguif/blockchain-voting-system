package com.epam.asset.tracking.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

/**
 * A place somewhere in a city/location where where a person or organization is located.
 *
 * @author daniel_pedraza@epam.com
 */
public class AddressDTO {

  private String street;
  private String zipCode;
  private String state;
  private String city;


  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String getZipCode() {
    return zipCode;
  }

  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (getClass() != o.getClass()) return false;
    AddressDTO address = (AddressDTO) o;
    return Objects.equals(street, address.street) &&
        Objects.equals(zipCode, address.zipCode) &&
        Objects.equals(state, address.state) &&
        Objects.equals(city, address.city);
  }

  @Override
  public int hashCode() {
    return Objects.hash(street, zipCode, state, city);
  }

  @Override
  public String toString() {
    return "{ " +
        "street:\"" + street + '"' +
        ", zipCode:\"" + zipCode + '"' +
        ", state:\"" + state + '"' +
        ", city:\"" + city + '"'+
        " }";
  }
}
