package com.epam.asset.tracking.domain;

import java.util.Objects;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Provides/registers assets in the application.
 *
 * @author daniel_pedraza@epam.com
 */

@Document
public class BusinessProvider extends User {

  public enum Type { HARDWARE, CAR, HOUSE }

  private String name;
  private String rfc;
  private Type type;
  private Address address;
  
  public BusinessProvider(){
	  super(Role.BUSINESS_PROVIDER);
  }

  @Override
  public void setRole(Role role) {
	  if(Role.BUSINESS_PROVIDER.equals(role))
		  super.setRole(role);
	  else
		  throw new IllegalArgumentException("Business provider only accepts BusinessProvider Role");
  }


  public String getName() {
    return name;
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

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (getClass() != o.getClass()) return false;
    BusinessProvider bp = (BusinessProvider) o;
    return isBusinessProviderEqualTo(bp);
  }

  protected boolean isBusinessProviderEqualTo(BusinessProvider other) {
    return isUserEqualTo(other) &&
        Objects.equals(name, other.name) &&
        Objects.equals(rfc, other.rfc) &&
        Objects.equals(address, other.address) &&
        type == other.type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), name, rfc, type, address);
  }

  @Override
  public String toString() {
    return "{ " +
        "id:\"" + getId() + '"' +
        ", username:\"" + getUsername() + '"' +
        ", password:" + hiddenPassword() +
        ", role:\"" + getRole() + '"' +
        ", name:\"" + name + '"' +
        ", rfc:\"" + rfc + '"' +
        ", type:\"" + type + '"' +
        ", address:" + address +
        " }";
  }
}
