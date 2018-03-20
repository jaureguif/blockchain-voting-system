package com.epam.asset.tracking.repository.blockchain.fabric.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

public class SampleOrg {

  private final String name;
  private final String mspid;

  private Map<String, User> userMap = new HashMap<>();
  private Map<String, String> peerLocations = new HashMap<>();
  private Map<String, String> ordererLocations = new HashMap<>();
  private Map<String, String> eventHubLocations = new HashMap<>();
  private Set<Peer> peers = new HashSet<>();
  private HFCAClient caClient;
  private SampleUser admin;
  private String caLocation;
  private Properties caProperties;
  private SampleUser peerAdmin;
  private String domainName;

  public SampleOrg(String name, String mspid) {
    this.name = name;
    this.mspid = mspid;
  }

  public SampleUser getAdmin() {
    return admin;
  }

  public void setAdmin(SampleUser admin) {
    this.admin = admin;
  }

  public String getMSPID() {
    return mspid;
  }

  public String getCALocation() {
    return this.caLocation;
  }

  public void setCALocation(String caLocation) {
    this.caLocation = caLocation;
  }

  public void addPeerLocation(String name, String location) {
    peerLocations.put(name, location);
  }

  public void addOrdererLocation(String name, String location) {
    ordererLocations.put(name, location);
  }

  public void addEventHubLocation(String name, String location) {
    eventHubLocations.put(name, location);
  }

  public String getPeerLocation(String name) {
    return peerLocations.get(name);
  }

  public String getOrdererLocation(String name) {
    return ordererLocations.get(name);
  }

  public String getEventHubLocation(String name) {
    return eventHubLocations.get(name);
  }

  public Set<String> getPeerNames() {
    return Collections.unmodifiableSet(peerLocations.keySet());
  }

  public Set<String> getOrdererNames() {
    return Collections.unmodifiableSet(ordererLocations.keySet());
  }

  public Set<String> getEventHubNames() {
    return Collections.unmodifiableSet(eventHubLocations.keySet());
  }

  public HFCAClient getCAClient() {
    return caClient;
  }

  public void setCAClient(HFCAClient caClient) {
    this.caClient = caClient;
  }

  public String getName() {
    return name;
  }

  public void addUser(SampleUser user) {
    userMap.put(user.getName(), user);
  }

  public User getUser(String name) {
    return userMap.get(name);
  }

  public Collection<String> getOrdererLocations() {
    return Collections.unmodifiableCollection(ordererLocations.values());
  }

  public Collection<String> getEventHubLocations() {
    return Collections.unmodifiableCollection(eventHubLocations.values());
  }

  public Set<Peer> getPeers() {
    return Collections.unmodifiableSet(peers);
  }

  public void addPeer(Peer peer) {
    peers.add(peer);
  }

  public Properties getCAProperties() {
    return caProperties;
  }

  public void setCAProperties(Properties caProperties) {
    this.caProperties = caProperties;
  }

  public SampleUser getPeerAdmin() {
    return peerAdmin;
  }

  public void setPeerAdmin(SampleUser peerAdmin) {
    this.peerAdmin = peerAdmin;
  }

  public String getDomainName() {
    return domainName;
  }

  public void setDomainName(String domainName) {
    this.domainName = domainName;
  }
}
