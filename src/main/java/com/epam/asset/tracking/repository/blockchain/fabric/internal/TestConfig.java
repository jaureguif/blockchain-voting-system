package com.epam.asset.tracking.repository.blockchain.fabric.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hyperledger.fabric.sdk.helper.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestConfig {

  private static final Logger log = LoggerFactory.getLogger(
      TestConfig.class);
  private static final String DEFAULT_CONFIG = "src/test/java/org/hyperledger/fabric/sdk/testutils.properties";
  private static final String ORG_HYPERLEDGER_FABRIC_SDK_CONFIGURATION = "org.hyperledger.fabric.sdktest.configuration";
  private static final String PROPBASE = "org.hyperledger.fabric.sdktest.";
  private static final String GOSSIPWAITTIME = PROPBASE + "GossipWaitTime";
  private static final String INVOKEWAITTIME = PROPBASE + "InvokeWaitTime";
  private static final String DEPLOYWAITTIME = PROPBASE + "DeployWaitTime";
  private static final String PROPOSALWAITTIME = PROPBASE + "ProposalWaitTime";
  private static final String INTEGRATIONTESTS_ORG = PROPBASE + "integrationTests.org.";
  private static final Pattern orgPat = Pattern.compile("^" + Pattern.quote(INTEGRATIONTESTS_ORG) + "([^\\.]+)\\.mspid$");
  private static final String INTEGRATIONTESTSTLS = PROPBASE + "integrationtests.tls";
  private static final Properties sdkProperties = new Properties();
  private static final HashMap<String, SampleOrg> sampleOrgs = new HashMap<>();

  private static TestConfig config;

  private final boolean runningTLS;
  private final boolean runningFabricCATLS;
  private final boolean runningFabricTLS;

  private TestConfig() {
    File loadFile;
    FileInputStream configProps;

    try {
      loadFile = new File(System.getProperty(ORG_HYPERLEDGER_FABRIC_SDK_CONFIGURATION, DEFAULT_CONFIG)).getAbsoluteFile();
      log.debug("Loading configuration from {} and it is present: {}", loadFile.toString(), loadFile.exists());
      configProps = new FileInputStream(loadFile);
      sdkProperties.load(configProps);
    } catch (IOException e) {
      // if not there no worries just use defaults
      // log.warn(String.format("Failed to load any test configuration from: %s.
      // Using toolkit defaults",
      // DEFAULT_CONFIG));
    } finally {
      // Default values
      defaultProperty(GOSSIPWAITTIME, "5000");
      defaultProperty(INVOKEWAITTIME, "100000");
      defaultProperty(DEPLOYWAITTIME, "120000");
      defaultProperty(PROPOSALWAITTIME, "120000");
      defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg1.mspid", "Org1MSP");
      defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg1.domname", "org1.example.com");
      defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg1.ca_location", "http://localhost:7054");
      defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg1.peer_locations", "peer0.org1.example.com@grpc://localhost:7051, peer1.org1.example.com@grpc://localhost:7056");
      defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg1.orderer_locations", "orderer.example.com@grpc://localhost:7050");
      defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg1.eventhub_locations", "peer0.org1.example.com@grpc://localhost:7053,peer1.org1.example.com@grpc://localhost:7058");
      defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg2.mspid", "Org2MSP");
      defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg2.domname", "org2.example.com");
      defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg2.ca_location", "http://localhost:8054");
      defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg2.peer_locations", "peer0.org2.example.com@grpc://localhost:8051,peer1.org2.example.com@grpc://localhost:8056");
      defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg2.orderer_locations", "orderer.example.com@grpc://localhost:7050");
      defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg2.eventhub_locations", "peer0.org2.example.com@grpc://localhost:8053, peer1.org2.example.com@grpc://localhost:8058");

      defaultProperty(INTEGRATIONTESTSTLS, null);
      runningTLS = null != sdkProperties.getProperty(INTEGRATIONTESTSTLS, null);
      runningFabricCATLS = runningTLS;
      runningFabricTLS = runningTLS;

      for (Map.Entry<Object, Object> x : sdkProperties.entrySet()) {
        final String key = x.getKey() + "";
        final String val = x.getValue() + "";
        if (key.startsWith(INTEGRATIONTESTS_ORG)) {
          Matcher match = orgPat.matcher(key);
          if (match.matches() && match.groupCount() == 1) {
            String orgName = match.group(1).trim();
            sampleOrgs.put(orgName, new SampleOrg(orgName, val.trim()));
          }
        }
      }

      for (Map.Entry<String, SampleOrg> org : sampleOrgs.entrySet()) {
        final SampleOrg sampleOrg = org.getValue();
        final String orgName = org.getKey();
        String peerNames = sdkProperties.getProperty(INTEGRATIONTESTS_ORG + orgName + ".peer_locations");
        String[] ps = peerNames.split("[ \t]*,[ \t]*");
        for (String peer : ps) {
          String[] nl = peer.split("[ \t]*@[ \t]*");
          sampleOrg.addPeerLocation(nl[0], grpcTLSify(nl[1]));
        }

        final String domainName = sdkProperties.getProperty(INTEGRATIONTESTS_ORG + orgName + ".domname");
        sampleOrg.setDomainName(domainName);
        String ordererNames = sdkProperties.getProperty(INTEGRATIONTESTS_ORG + orgName + ".orderer_locations");
        ps = ordererNames.split("[ \t]*,[ \t]*");
        for (String peer : ps) {
          String[] nl = peer.split("[ \t]*@[ \t]*");
          sampleOrg.addOrdererLocation(nl[0], grpcTLSify(nl[1]));
        }

        String eventHubNames = sdkProperties.getProperty(INTEGRATIONTESTS_ORG + orgName + ".eventhub_locations");
        ps = eventHubNames.split("[ \t]*,[ \t]*");
        for (String peer : ps) {
          String[] nl = peer.split("[ \t]*@[ \t]*");
          sampleOrg.addEventHubLocation(nl[0], grpcTLSify(nl[1]));
        }

        sampleOrg.setCALocation(httpTLSify(sdkProperties.getProperty((INTEGRATIONTESTS_ORG + org.getKey() + ".ca_location"))));

        if (runningFabricCATLS) {
          String cert = "src/test/fixture/sdkintegration/e2e-2Orgs/channel/crypto-config/peerOrganizations/DNAME/ca/ca.DNAME-cert.pem".replaceAll("DNAME", domainName);
          File cf = new File(cert);
          if (!cf.exists() || !cf.isFile()) {
            throw new RuntimeException("TEST is missing cert file " + cf.getAbsolutePath());
          }
          Properties properties = new Properties();
          properties.setProperty("pemFile", cf.getAbsolutePath());
          properties.setProperty("allowAllHostNames", "true");// testing environment only NOT FOR PRODUCTION!
          sampleOrg.setCAProperties(properties);
        }
      }
    }
  }

  /**
   * getConfig return back singleton for SDK configuration.
   *
   * @return Global configuration
   */
  public static TestConfig getConfig() {
    if (null == config) config = new TestConfig();
    return config;

  }

  static private void defaultProperty(String key, String value) {
    String ret = System.getProperty(key);
    if (ret != null) sdkProperties.put(key, ret);
    else {
      String envKey = key.toUpperCase().replaceAll("\\.", "_");
      ret = System.getenv(envKey);
      if (null != ret) sdkProperties.put(key, ret);
      else {
        if (null == sdkProperties.getProperty(key) && value != null) {
          sdkProperties.put(key, value);
        }
      }
    }
  }

  private String grpcTLSify(String location) {
    location = location.trim();
    Exception e = Utils.checkGrpcUrl(location);
    if (e != null) {
      throw new RuntimeException(String.format("Bad TEST parameters for grpc url %s", location), e);
    }
    return runningFabricTLS ? location.replaceFirst("^grpc://", "grpcs://") : location;
  }

  private String httpTLSify(String location) {
    location = location.trim();
    return runningFabricCATLS ? location.replaceFirst("^http://", "https://") : location;
  }

  /**
   * getProperty return back property for the given value.
   *
   * @return String value for the property
   */
  private String getProperty(String property) {
    String ret = sdkProperties.getProperty(property);
    if (null == ret) log.warn("No configuration value found for '{}'", property);
    return ret;
  }

  public int getTransactionWaitTime() {
    return Integer.parseInt(getProperty(INVOKEWAITTIME));
  }

  public int getDeployWaitTime() {
    return Integer.parseInt(getProperty(DEPLOYWAITTIME));
  }

  public int getGossipWaitTime() {
    return Integer.parseInt(getProperty(GOSSIPWAITTIME));
  }

  /**
   * Time to wait for proposal to complete
   */
  public long getProposalWaitTime() {
    return Integer.parseInt(getProperty(PROPOSALWAITTIME));
  }

  public Collection<SampleOrg> getIntegrationTestsSampleOrgs() {
    return Collections.unmodifiableCollection(sampleOrgs.values());
  }

  public SampleOrg getIntegrationTestsSampleOrg(String name) {
    return sampleOrgs.get(name);

  }

  public Properties getPeerProperties(String name) {
    return getEndPointProperties("peer", name);

  }

  public Properties getOrdererProperties(String name) {
    return getEndPointProperties("orderer", name);
  }

  private Properties getEndPointProperties(final String type, final String name) {
    final String domainName = getDomainName(name);
    File cert = Paths.get(getTestChannlePath(), "crypto-config/ordererOrganizations".replace("orderer", type), domainName, type + "s", name, "tls/server.crt").toFile();
    if (!cert.exists()) {
      throw new RuntimeException(String.format("Missing cert file for: %s. Could not find at location: %s", name, cert.getAbsolutePath()));
    }

    Properties ret = new Properties();
    ret.setProperty("pemFile", cert.getAbsolutePath());
    // ret.setProperty("trustServerCertificate", "true"); // testing environment only NOT FOR PRODUCTION!
    ret.setProperty("hostnameOverride", name);
    ret.setProperty("sslProvider", "openSSL");
    ret.setProperty("negotiationType", "TLS");
    return ret;
  }

  public Properties getEventHubProperties(String name) {
    return getEndPointProperties("peer", name); // uses same as named peer

  }

  public String getTestChannlePath() {
    return "src/test/fixture/sdkintegration/e2e-2Orgs/channel";

  }

  private String getDomainName(String name) {
    int dot = name.indexOf(".");
    if (-1 == dot) return null;
    return name.substring(dot + 1);
  }
}
