package com.epam.asset.tracking.repository.blockchain.fabric.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Set;

import com.epam.asset.tracking.annotation.CoverageIgnore;
import io.netty.util.internal.StringUtil;
import org.apache.commons.codec.binary.Hex;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleUser implements User, Serializable {

  private static final long serialVersionUID = 8077132186383604355L;
  private static final Logger log = LoggerFactory.getLogger(
      SampleUser.class);

  private Enrollment enrollment; // need access in test env.
  private String mspId;
  private String name;
  private Set<String> roles;
  private String account;
  private String affiliation;
  private String organization;
  private String enrollmentSecret;
  private transient SampleStore keyValStore;
  private String keyValStoreName;

  SampleUser(String name, String org, SampleStore fs) {
    this.name = name;
    this.keyValStore = fs;
    this.organization = org;
    this.keyValStoreName = toKeyValStoreName(this.name, org);
    String memberStr = keyValStore.getValue(keyValStoreName);
    if (null == memberStr) saveState();
  }

  public static String toKeyValStoreName(String name, String org) {
    return "user." + name + org;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public Set<String> getRoles() {
    return this.roles;
  }

  public void setRoles(Set<String> roles) {
    this.roles = roles;
    saveState();
  }

  @Override
  public String getAccount() {
    return this.account;
  }

  /**
   * Set the account.
   *
   * @param account The account.
   */
  public void setAccount(String account) {
    this.account = account;
    saveState();
  }

  @Override
  public String getAffiliation() {
    return this.affiliation;
  }

  /**
   * Set the affiliation.
   *
   * @param affiliation the affiliation.
   */
  public void setAffiliation(String affiliation) {
    this.affiliation = affiliation;
    saveState();
  }

  @Override
  public Enrollment getEnrollment() {
    return this.enrollment;
  }

  public void setEnrollment(Enrollment enrollment) {
    this.enrollment = enrollment;
    saveState();

  }

  /**
   * Determine if this name has been registered.
   *
   * @return {@code true} if registered; otherwise {@code false}.
   */
  public boolean isRegistered() {
    return !StringUtil.isNullOrEmpty(enrollmentSecret);
  }

  /**
   * Determine if this name has been enrolled.
   *
   * @return {@code true} if enrolled; otherwise {@code false}.
   */
  public boolean isEnrolled() {
    return this.enrollment != null;
  }

  /**
   * Save the state of this user to the key value store.
   */
  void saveState() {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(this);
      oos.flush();
      keyValStore.setValue(keyValStoreName, Hex.encodeHexString(bos.toByteArray()));
      bos.close();
    } catch (IOException e) {
      log.warn("Error saving state", e);
    }
  }

  /**
   * Restore the state of this user from the key value store (if found). If not found, do nothing.
   */
  @CoverageIgnore
  SampleUser restoreState() {
    String memberStr = keyValStore.getValue(keyValStoreName);
    if (null != memberStr) {
      // The user was found in the key value store, so restore the state.
      try {
        byte[] serialized = Hex.decodeHex(memberStr.toCharArray());
        ByteArrayInputStream bis = new ByteArrayInputStream(serialized);
        ObjectInputStream ois = new ObjectInputStream(bis);
        SampleUser state = (SampleUser) ois.readObject();
        if (state != null) {
          this.name = state.name;
          this.roles = state.roles;
          this.account = state.account;
          this.affiliation = state.affiliation;
          this.organization = state.organization;
          this.enrollmentSecret = state.enrollmentSecret;
          this.enrollment = state.enrollment;
          this.mspId = state.mspId;
          return this;
        }
      } catch (Exception e) {
        log.warn("Could not restore state of member {}", name, e);
        throw new RuntimeException(String.format("Could not restore state of member %s", name), e);
      }
    }
    return null;
  }

  public String getEnrollmentSecret() {
    return enrollmentSecret;
  }

  public void setEnrollmentSecret(String enrollmentSecret) {
    this.enrollmentSecret = enrollmentSecret;
    saveState();
  }

  @Override
  public String getMspId() {
    return mspId;
  }

  public void setMspId(String mspID) {
    this.mspId = mspID;
    saveState();
  }
}
