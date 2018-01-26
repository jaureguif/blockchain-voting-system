package com.epam.asset.tracking.domain;

import java.util.Objects;

/**
 * Provides/registers assets in the application.
 *
 * @author daniel_pedraza@epam.com
 */
public class BusinessProvider extends User {

  public enum Type { /* TODO: add business types. */ }

  private String name;
  private String rfc;
  private Type type;
  private Address address;

  @Override
  public void setRole(Role role) { }

  @Override
  public Role getRole() {
    return Role.BUSINESS_PROVIDER;
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
    BusinessProvider that = (BusinessProvider) o;
    return Objects.equals(name, that.name) &&
        Objects.equals(rfc, that.rfc) &&
        type == that.type &&
        Objects.equals(address, that.address);
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
