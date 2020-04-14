package com.epam.asset.tracking.repository.blockchain.fabric;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.epam.asset.tracking.annotation.CoverageIgnore;
import com.epam.asset.tracking.exception.BlockchainInitializationException;
import com.epam.asset.tracking.repository.blockchain.fabric.internal.SampleOrg;
import com.epam.asset.tracking.repository.blockchain.fabric.internal.SampleStore;
import com.epam.asset.tracking.repository.blockchain.fabric.internal.SampleUser;
import com.epam.asset.tracking.repository.blockchain.fabric.internal.TestConfig;
import com.epam.asset.tracking.repository.blockchain.fabric.internal.TestConfigHelper;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FabricInitializerHelper {

  private static final TestConfig testConfig = TestConfig.getConfig();
  private static final String CHANNEL_NAME = "foo";
  private static final String TEST_ADMIN_NAME = "admin";
  private static final String TESTUSER_1_NAME = "user1";
  private static final Logger log = LoggerFactory.getLogger(FabricInitializerHelper.class);

  private final TestConfigHelper configHelper = new TestConfigHelper();
  private HFClient client;
  private Channel channel;
  private Collection<SampleOrg> testSampleOrgs;

  public HFClient getClient() {
    return client;
  }

  public Channel getChannel() {
    return channel;
  }

  public Collection<SampleOrg> getTestSampleOrgs() {
    return testSampleOrgs;
  }

  public void init() {
    log.info("RUNNING: setup.");
    configHelper.clearConfig();
    configHelper.customizeConfig();
    testSampleOrgs = testConfig.getIntegrationTestsSampleOrgs();
    // Set up hfca for each sample org
    for (SampleOrg sampleOrg : testSampleOrgs) {
      try {
        sampleOrg.setCAClient(HFCAClient.createNewInstance(sampleOrg.getCALocation(), sampleOrg.getCAProperties()));
      } catch (MalformedURLException e) {
        throw new IllegalArgumentException(e.getMessage());
      }
    }

    ////////////////////////////
    // Setup client

    // Create instance of client.
    client = HFClient.createNewInstance();
    try {
      client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
    } catch (CryptoException e) {
      throw new BlockchainInitializationException("Unable to set crypto site in client", e);
    } catch (InvalidArgumentException e) {
      throw new IllegalArgumentException(e.getMessage());
    }

    ////////////////////////////
    // Set up USERS

    // Persistence is not part of SDK. Sample file store is for demonstration purposes only!
    // MUST be replaced with more robust application implementation (Database, LDAP)
    File sampleStoreFile = new File(System.getProperty("java.io.tmpdir") + "/HFCSampletest.properties");
    SampleStore sampleStore = new SampleStore(sampleStoreFile);

    // SampleUser can be any implementation that implements
    // org.hyperledger.fabric.sdk.User Interface

    for (SampleOrg sampleOrg : testSampleOrgs) {
      setUsersForOrg(sampleOrg, sampleStore);
    }

    ////////////////////////////
    // Construct and run the channels
    SampleOrg sampleOrg = testConfig.getIntegrationTestsSampleOrg("peerOrg1");
    Channel fooChannel = constructChannel(CHANNEL_NAME, client, sampleOrg);
    log.info("That's all folks!");
    channel = fooChannel;
  }

  private void setUsersForOrg(SampleOrg sampleOrg, SampleStore sampleStore) {
    HFCAClient ca = sampleOrg.getCAClient();
    String orgName = sampleOrg.getName();
    String mspid = sampleOrg.getMSPID();
    ca.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
    SampleUser admin = sampleStore.getMember(TEST_ADMIN_NAME, orgName);
    if (!admin.isEnrolled()) { // Preregistered admin only needs to be enrolled with Fabric caClient.
      try {
        admin.setEnrollment(ca.enroll(admin.getName(), "adminpw"));
      } catch (EnrollmentException e) {
        throw new BlockchainInitializationException(format("Unable to enroll admin %s", admin.getName()), e);
      } catch (org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException e) {
        throw new IllegalArgumentException(e.getMessage());
      }
      admin.setMspId(mspid);
    }

    sampleOrg.setAdmin(admin); // The admin of this org --
    SampleUser user = sampleStore.getMember(TESTUSER_1_NAME, sampleOrg.getName());
    // users need to be registered AND enrolled
    if (!user.isRegistered()) {
      try {
        new RegistrationRequest(user.getName(), "org1.department1");
      } catch (Exception e) {
        throw new BlockchainInitializationException(format("Unable to enroll user %s", user.getName()), e);
      }
    }

    if (!user.isEnrolled()) user.setMspId(mspid);
    sampleOrg.addUser(user); // Remember user belongs to this Org

    String sampleOrgName = sampleOrg.getName();
    String sampleOrgDomainName = sampleOrg.getDomainName();
    SampleUser peerOrgAdmin;
    try {
      peerOrgAdmin = sampleStore
          .getMember(sampleOrgName + "Admin", sampleOrgName, sampleOrg.getMSPID(),
              findFileSk(privateKeyFileOf(sampleOrgDomainName)),
              certificateFileOf(sampleOrgDomainName));
    } catch (IOException e) {
      throw new BlockchainInitializationException("Unable to read private private key file or certificate file", e);
    }
    // A special user that can create channels, join peers and install chaincode
    sampleOrg.setPeerAdmin(peerOrgAdmin);
  }

  private File privateKeyFileOf(String orgDomainName) {
    return Paths.get(testConfig.getTestChannlePath(),
        "crypto-config/peerOrganizations/", orgDomainName,
        format("/users/Admin@%s/msp/keystore", orgDomainName))
                .toFile();
  }

  private File certificateFileOf(String orgDomainName) {
    return Paths.get(testConfig.getTestChannlePath(),
        "crypto-config/peerOrganizations/",
        orgDomainName,
        format("/users/Admin@%s/msp/signcerts/Admin@%s-cert.pem", orgDomainName, orgDomainName))
                .toFile();
  }

  @CoverageIgnore
  private File findFileSk(File directory) {
    File[] matches = directory.listFiles((dir, name) -> name.endsWith("_sk"));
    if (null == matches) throw new IllegalArgumentException(
        format("Matches returned null does %s directory exist?",
            directory.getAbsoluteFile().getName()));
    if (matches.length != 1) throw new IllegalArgumentException(
        format("Expected in %s only 1 sk file but found %d", directory.getAbsoluteFile().getName(),
            matches.length));
    return matches[0];
  }

  @CoverageIgnore
  private Channel constructChannel(String name, HFClient client, SampleOrg sampleOrg) {
    ////////////////////////////
    // Construct the channel
    //
    log.info("Constructing channel {}", name);

    // Only peer Admin org
    try {
      client.setUserContext(sampleOrg.getPeerAdmin());
    } catch (InvalidArgumentException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
    Collection<Orderer> orderers = new LinkedList<>();

    for (String orderName : sampleOrg.getOrdererNames()) {
      Properties ordererProperties = testConfig.getOrdererProperties(orderName);
      // example of setting keepAlive to avoid timeouts on inactive http2 connections.
      // Under 5 minutes would require changes to server side to accept faster ping
      // rates.
      ordererProperties
          .put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[]{5L, TimeUnit.MINUTES});
      ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout",
          new Object[]{8L, TimeUnit.SECONDS});

      try {
        orderers.add(client.newOrderer(orderName, sampleOrg.getOrdererLocation(orderName), ordererProperties));
      } catch (InvalidArgumentException e) {
        throw new IllegalArgumentException(e.getMessage());
      }
    }

    // Just pick the first orderer in the list to create the channel.
    Orderer anOrderer = orderers.iterator().next();
    orderers.remove(anOrderer);

    // Create channel that has only one signer that is this orgs peer admin. If
    // channel creation policy needed more signature they would need to be added too.
    // Channel newChannel = client.newChannel(name, anOrderer, channelConfiguration,
    // client.getChannelConfigurationSignature(channelConfiguration, sampleOrg.getPeerAdmin()));
    Channel newChannel;
    try {
      newChannel = client.newChannel(name);
    } catch (InvalidArgumentException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
    try {
      newChannel.addOrderer(anOrderer);
    } catch (InvalidArgumentException e) {
      throw new IllegalArgumentException(e.getMessage());
    }

    log.info("Created channel {}", name);

    for (String peerName : sampleOrg.getPeerNames()) {
      String peerLocation = sampleOrg.getPeerLocation(peerName);
      Properties peerProperties = testConfig.getPeerProperties(peerName); // test properties for peer.. if any.
      if (peerProperties == null) peerProperties = new Properties();
      // Example of setting specific options on grpc's NettyChannelBuilder
      peerProperties.put("grpc.NettyChannelBuilderOption.maxInboundMessageSize", 9000000);

      Peer peer;
      try {
        peer = client.newPeer(peerName, peerLocation, peerProperties);
      } catch (InvalidArgumentException e) {
        throw new IllegalArgumentException(e.getMessage());
      }
      try {
        newChannel.addPeer(peer);
      } catch (InvalidArgumentException e) {
        throw new IllegalArgumentException(e.getMessage());
      }
      sampleOrg.addPeer(peer);
    }

    for (Orderer orderer : orderers) { // add remaining orderers if any.
      try {
        newChannel.addOrderer(orderer);
      } catch (InvalidArgumentException e) {
        throw new IllegalArgumentException(e.getMessage());
      }
    }

    for (String eventHubName : sampleOrg.getEventHubNames()) {
      Properties eventHubProperties = testConfig.getEventHubProperties(eventHubName);
      eventHubProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[]{5L, TimeUnit.MINUTES});
      eventHubProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[]{8L, TimeUnit.SECONDS});

      EventHub eventHub;
      try {
        eventHub = client.newEventHub(eventHubName, sampleOrg.getEventHubLocation(eventHubName), eventHubProperties);
      } catch (InvalidArgumentException e) {
        throw new IllegalArgumentException(e.getMessage());
      }
      try {
        newChannel.addEventHub(eventHub);
      } catch (InvalidArgumentException e) {
        throw new IllegalArgumentException(e.getMessage());
      }
    }

    try {
      newChannel.initialize();
    } catch (InvalidArgumentException e) {
      throw new IllegalArgumentException(e.getMessage());
    } catch (TransactionException e) {
      throw new BlockchainInitializationException(format("Unnable to initialize channel %s", name), e);
    }
    newChannel.setTransactionWaitTime(testConfig.getTransactionWaitTime());
    newChannel.setDeployWaitTime(testConfig.getDeployWaitTime());

    log.info("Finished initialization channel {}", name);
    return newChannel;
  }
}
