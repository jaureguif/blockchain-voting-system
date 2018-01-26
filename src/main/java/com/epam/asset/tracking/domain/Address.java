package com.epam.asset.tracking.domain;

import java.util.Objects;

/**
 * A place somewhere in a city/location where where a person or organization is located.
 *
 * @author daniel_pedraza@epam.com
 */
public class Address {

  private String street;
  private Integer number;
  private String zipCode;
  private String state;
  private String city;
  private String country;

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public Integer getNumber() {
    return number;
  }

  public void setNumber(Integer number) {
    this.number = number;
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

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (getClass() != o.getClass()) return false;
    Address address = (Address) o;
    return Objects.equals(street, address.street) &&
        Objects.equals(number, address.number) &&
        Objects.equals(zipCode, address.zipCode) &&
        Objects.equals(state, address.state) &&
        Objects.equals(city, address.city) &&
        Objects.equals(country, address.country);
  }

  @Override
  public int hashCode() {
    return Objects.hash(street, number, zipCode, state, city, country);
  }

  @Override
  public String toString() {
    return "{ " +
        "street:\"" + street + '"' +
        ", number:\"" + number +
        ", zipCode:\"" + zipCode + '"' +
        ", state:\"" + state + '"' +
        ", city:\"" + city + '"' +
        ", country:\"" + country + '"' +
        " }";
  }
}
