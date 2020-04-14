package com.epam.asset.tracking.domain;

import java.util.Objects;

/**
 * User of this application.
 *
 * @author daniel_pedraza@epam.com
 */
public class User extends BaseEntity<String> {

  public enum Role { ADMIN, BUSINESS_PROVIDER, USER; }

  private String username;

  private String password;

  private Role role;
  private String email;
  
  public User(Role role){
	  super();
	  this.role = role;
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

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
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
    if (o == null) return false;
    if (getClass() != o.getClass()) return false;
    User user = (User) o;
    return isUserEqualTo(user);
  }

  protected boolean isUserEqualTo(User other) {
    return isEntityEqualTo(other) &&
        Objects.equals(username, other.username) &&
        Objects.equals(password, other.password) &&
        role == other.role &&
        Objects.equals(email, other.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), username, password, role);
  }

  protected String hiddenPassword() {
    if (password == null) return null;
    return "<hidden>";
  }

  @Override
  public String toString() {
    return "{ " +
        "id:\"" + getId() + '"' +
        ", username:\"" + username + '"' +
        ", password:" + hiddenPassword() +
        ", role:\"" + role +
        ", email:\"" + email +
        " }";
  }
}
