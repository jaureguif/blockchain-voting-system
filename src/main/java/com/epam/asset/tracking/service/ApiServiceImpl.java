package com.epam.asset.tracking.service;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.SDKUtils;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.helper.Config;
import org.hyperledger.fabric.sdk.helper.Utils;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.epam.asset.tracking.annotation.CoverageIgnore;
import com.epam.asset.tracking.domain.Asset;
import com.epam.asset.tracking.exception.AssetNotFoundException;
import io.netty.util.internal.StringUtil;
import ma.glasnost.orika.MapperFacade;

@Service
public class ApiServiceImpl implements ApiService {

  private Logger log = LoggerFactory.getLogger(ApiServiceImpl.class);

  private final ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(CHAIN_CODE_NAME)
      .setVersion(CHAIN_CODE_VERSION).setPath(CHAIN_CODE_PATH).build();
  private HFClient client = null;
  private Channel channel = null;

  private final TestConfigHelper configHelper = new TestConfigHelper();
  private Collection<SampleOrg> testSampleOrgs;

  private static final TestConfig testConfig = TestConfig.getConfig();
  private static final String TEST_ADMIN_NAME = "admin";
  private static final String TESTUSER_1_NAME = "user1";

  private static final String CHAIN_CODE_NAME = "asset_t_smart_contract_go";
  private static final String CHAIN_CODE_PATH =
      "com.epam.blockchain.chaincode/asset_t_smart_contract";
  private static final String CHAIN_CODE_VERSION = "1";
  private static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
  private static final String EXPECTED_EVENT_NAME = "event";

  private static final String CHANNEL_NAME = "foo";

  @Autowired
  private MapperFacade mapper;

  @Override
  public String saveAsset(Asset asset) {
    String result = null;
    try {
      try {
        result = saveAssetToFabric(asset);
      } catch (AssetNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } catch (InvalidArgumentException | ProposalException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return result;
  }

  @Override
  public Asset getAssetById(UUID id) throws AssetNotFoundException {

    String jsonStr;
    try {
      jsonStr = getAssetFromFabric(id);
    } catch (InvalidArgumentException | ProposalException e) {
      log.error("Error when trying to retrieve asset with id " + id, e);
      return null;
    }

    return mapper.map(jsonStr, Asset.class);

  }

  private String saveAssetToFabric(Asset asset)
      throws InvalidArgumentException, ProposalException, AssetNotFoundException {

    Collection<ProposalResponse> successful = new LinkedList<>();
    Collection<ProposalResponse> failed = new LinkedList<>();

    try {
      setup();
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Error in setup: {}", e);
    }

    // client.setUserContext(testConfig.getIntegrationTestsSampleOrg("peerOrg1").getUser(TEST_ADMIN_NAME));

    ///////////////
    /// Send transaction proposal to all peers
    TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
    String[] args = {"create", asset.getUuid(), "T1000", "TERMINATOR2", "Cyberdyne Systems",
        "Model 101 Series 1000 Terminator", "SKYNET_ID",
        "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eGlmOkNvbXByZXNzaW9uPgogIDxleGlmOlhSZXNvbHV0aW9uPjcyPC9leGlmOlhSZXNvbHV0aW9uPgogIDxleGlmOllSZXNvbHV0aW9uPjcyPC9leGlmOllSZXNvbHV0aW9uPgogIDxleGlmOlJlc29sdXRpb25Vbml0PkluY2g8L2V4aWY6UmVzb2x1dGlvblVuaXQ+CiAgPGV4aWY6Rmxhc2hQaXhWZXJzaW9uPkZsYXNoUGl4IFZlcnNpb24gMS4wPC9leGlmOkZsYXNoUGl4VmVyc2lvbj4KICA8ZXhpZjpPcmllbnRhdGlvbj5Ub3AtbGVmdDwvZXhpZjpPcmllbnRhdGlvbj4KICA8ZXhpZjpYUmVzb2x1dGlvbj43MjwvZXhpZjpYUmVzb2x1dGlvbj4KICA8ZXhpZjpZUmVzb2x1dGlvbj43MjwvZXhpZjpZUmVzb2x1dGlvbj4KICA8ZXhpZjpSZXNvbHV0aW9uVW5pdD5JbmNoPC9leGlmOlJlc29sdXRpb25Vbml0PgogIDxleGlmOlNvZnR3YXJlPkdJTVAgMi44LjIyPC9leGlmOlNvZnR3YXJlPgogIDxleGlmOkRhdGVUaW1lPjIwMTg6MDI6MjIgMTU6NTg6NTg8L2V4aWY6RGF0ZVRpbWU+CiAgPGV4aWY6Q29tcHJlc3Npb24+SlBFRyBjb21wcmVzc2lvbjwvZXhpZjpDb21wcmVzc2lvbj4KICA8ZXhpZjpYUmVzb2x1dGlvbj43MjwvZXhpZjpYUmVzb2x1dGlvbj4KICA8ZXhpZjpZUmVzb2x1dGlvbj43MjwvZXhpZjpZUmVzb2x1dGlvbj4KICA8ZXhpZjpSZXNvbHV0aW9uVW5pdD5JbmNoPC9leGlmOlJlc29sdXRpb25Vbml0PgogIDxleGlmOkZsYXNoUGl4VmVyc2lvbj5GbGFzaFBpeCBWZXJzaW9uIDEuMDwvZXhpZjpGbGFzaFBpeFZlcnNpb24+CiAgPGV4aWY6T3JpZW50YXRpb24+VG9wLWxlZnQ8L2V4aWY6T3JpZW50YXRpb24+CiAgPGV4aWY6WFJlc29sdXRpb24+NzIuMDAwMDwvZXhpZjpYUmVzb2x1dGlvbj4KICA8ZXhpZjpZUmVzb2x1dGlvbj43Mi4wMDAwPC9leGlmOllSZXNvbHV0aW9uPgogIDxleGlmOlJlc29sdXRpb25Vbml0PkluY2g8L2V4aWY6UmVzb2x1dGlvblVuaXQ+CiAgPGV4aWY6U29mdHdhcmU+QWRvYmUgUGhvdG9zaG9wIENTNSBXaW5kb3dzPC9leGlmOlNvZnR3YXJlPgogIDxleGlmOkRhdGVUaW1lPjIwMTE6MDU6MTggMDA6MDU6MTI8L2V4aWY6RGF0ZVRpbWU+CiAgPGV4aWY6Q29tcHJlc3Npb24+SlBFRyBjb21wcmVzc2lvbjwvZXhpZjpDb21wcmVzc2lvbj4KICA8ZXhpZjpYUmVzb2x1dGlvbj43MjwvZXhpZjpYUmVzb2x1dGlvbj4KICA8ZXhpZjpZUmVzb2x1dGlvbj43MjwvZXhpZjpZUmVzb2x1dGlvbj4KICA8ZXhpZjpSZXNvbHV0aW9uVW5pdD5JbmNoPC9leGlmOlJlc29sdXRpb25Vbml0PgogIDxleGlmOkV4aWZWZXJzaW9uPkV4aWYgVmVyc2lvbiAyLjE8L2V4aWY6RXhpZlZlcnNpb24+CiAgPGV4aWY6Rmxhc2hQaXhWZXJzaW9uPkZsYXNoUGl4IFZlcnNpb24gMS4wPC9leGlmOkZsYXNoUGl4VmVyc2lvbj4KICA8ZXhpZjpDb2xvclNwYWNlPnNSR0I8L2V4aWY6Q29sb3JTcGFjZT4KICA8ZXhpZjpQaXhlbFhEaW1lbnNpb24+MTAyNDwvZXhpZjpQaXhlbFhEaW1lbnNpb24+CiAgPGV4aWY6UGl4ZWxZRGltZW5zaW9uPjc0MjwvZXhpZjpQaXhlbFlEaW1lbnNpb24+CiA8L3JkZjpEZXNjcmlwdGlvbj4KCjwvcmRmOlJERj4KPC94OnhtcG1ldGE+Cjw/eHBhY2tldCBlbmQ9J3InPz4K/+IMWElDQ19QUk9GSUxFAAEBAAAMSExpbm8CEAAAbW50clJHQiBYWVogB84AAgAJAAYAMQAAYWNzcE1TRlQAAAAASUVDIHNSR0IAAAAAAAAAAAAAAAEAAPbWAAEAAAAA0y1IUCAgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARY3BydAAAAVAAAAAzZGVzYwAAAYQAAABsd3RwdAAAAfAAAAAUYmtwdAAAAgQAAAAUclhZWgAAAhgAAAAUZ1hZ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",};
    transactionProposalRequest.setArgs(args);
    transactionProposalRequest.setFcn("invoke");
    transactionProposalRequest.setProposalWaitTime(testConfig.getProposalWaitTime());
    transactionProposalRequest.setChaincodeID(chaincodeID);

    Map<String, byte[]> tm2 = new HashMap<>();
    tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
    tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));
    tm2.put(EXPECTED_EVENT_NAME, EXPECTED_EVENT_DATA); // This should trigger an event see chaincode
                                                       // why.
    transactionProposalRequest.setTransientMap(tm2);

    log.warn("sending transactionProposal to all peers with arguments: create()");

    Collection<ProposalResponse> transactionPropResp =
        channel.sendTransactionProposal(transactionProposalRequest, channel.getPeers());
    for (ProposalResponse response : transactionPropResp) {
      if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
        log.warn("Successful transaction proposal response Txid: %s from peer %s",
            response.getTransactionID(), response.getPeer().getName());
        successful.add(response);
      } else {
        failed.add(response);
      }
    }

    // Check that all the proposals are consistent with each other. We should have only one set
    // where all the proposals above are consistent. Note the when sending to Orderer this is done
    // automatically.
    // Shown here as an example that applications can invoke and select.
    // See org.hyperledger.fabric.sdk.proposal.consistency_validation config property.
    Collection<Set<ProposalResponse>> proposalConsistencySets =
        SDKUtils.getProposalConsistencySets(transactionPropResp);
    if (proposalConsistencySets.size() != 1) {
      log.error(format("Expected only one set of consistent proposal responses but got %d",
          proposalConsistencySets.size()));
    }

    log.warn("Received %d transaction proposal responses. Successful+verified: %d . Failed: %d",
        transactionPropResp.size(), successful.size(), failed.size());
    if (failed.size() > 0) {
      ProposalResponse firstTransactionProposalResponse = failed.iterator().next();
      log.error("Not enough endorsers for create():" + failed.size() + " endorser error: "
          + firstTransactionProposalResponse.getMessage() + ". Was verified: "
          + firstTransactionProposalResponse.isVerified());
    }
    log.warn("Successfully received transaction proposal responses.");

    ProposalResponse resp = transactionPropResp.iterator().next();
    
    //TODO validate response
    log.warn("RESPONSE ACTION RESPONSE STATUS: " + resp.getChaincodeActionResponseStatus());

    ////////////////////////////
    // Send Transaction Transaction to orderer
    log.warn("Sending chaincode transaction(create) to orderer.");
    try {
      TransactionEvent event = channel.sendTransaction(successful).get(testConfig.getTransactionWaitTime(),
          TimeUnit.SECONDS);
      log.warn("Chaincode transaction(create) completed with transaction ID: " + event.getTransactionID());
    } catch (Exception e) {
      log.warn("Caught an exception while invoking chaincode");
      e.printStackTrace();
      log.error("Failed invoking chaincode with error : " + e.getMessage());
    }


    String result = successful.size() == channel.getPeers().size() ? "OK" : "FAIL";
    channel.shutdown(true);
    return result;
  }

  private String getAssetFromFabric(UUID id)
      throws InvalidArgumentException, ProposalException, AssetNotFoundException {
    String payload = null;

    try {
      setup();
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Error in setup: {}", e);
    }

    QueryByChaincodeRequest queryByChaincodeRequest = client.newQueryProposalRequest();
    queryByChaincodeRequest.setArgs(new String[] {"query", id.toString()});
    queryByChaincodeRequest.setFcn("invoke");
    queryByChaincodeRequest.setChaincodeID(chaincodeID);

    Map<String, byte[]> transientMap = new HashMap<>();
    transientMap.put("HyperLedgerFabric", "QueryByChaincodeRequest:JavaSDK".getBytes(UTF_8));
    transientMap.put("method", "QueryByChaincodeRequest".getBytes(UTF_8));
    queryByChaincodeRequest.setTransientMap(transientMap);

    Collection<ProposalResponse> queryProposals =
        channel.queryByChaincode(queryByChaincodeRequest, channel.getPeers());
    for (ProposalResponse proposalResponse : queryProposals) {
      if (!proposalResponse.isVerified()
          || proposalResponse.getStatus() != ProposalResponse.Status.SUCCESS) {

        if (proposalResponse.getMessage().toUpperCase().contains("NOT FOUND")) {
          throw new AssetNotFoundException("Failed query proposal from peer "
              + proposalResponse.getPeer().getName() + " status: " + proposalResponse.getStatus()
              + ". Messages: " + proposalResponse.getMessage() + ". Was verified : "
              + proposalResponse.isVerified());
        } else {
          throw new ProposalException("Failed query proposal from peer "
              + proposalResponse.getPeer().getName() + " status: " + proposalResponse.getStatus()
              + ". Messages: " + proposalResponse.getMessage() + ". Was verified : "
              + proposalResponse.isVerified());
        }

      } else {
        payload = proposalResponse.getProposalResponse().getResponse().getPayload().toStringUtf8();
        log.info("Query payload of {} from peer {} returned {}", id,
            proposalResponse.getPeer().getName(), payload);

      }
    }
    channel.shutdown(true);
    return payload;
  }

  private void setup() throws MalformedURLException, NoSuchFieldException, SecurityException,
      IllegalArgumentException, IllegalAccessException {

    log.info("RUNNING: setup.");

    configHelper.clearConfig();
    configHelper.customizeConfig();

    testSampleOrgs = testConfig.getIntegrationTestsSampleOrgs();
    // Set up hfca for each sample org
    for (SampleOrg sampleOrg : testSampleOrgs) {
      sampleOrg.setCAClient(
          HFCAClient.createNewInstance(sampleOrg.getCALocation(), sampleOrg.getCAProperties()));
    }

    try {

      ////////////////////////////
      // Setup client

      // Create instance of client.
      client = HFClient.createNewInstance();
      client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());

      ////////////////////////////
      // Set up USERS

      // Persistence is not part of SDK. Sample file store is for demonstration
      // purposes only!
      // MUST be replaced with more robust application implementation (Database, LDAP)
      File sampleStoreFile =
          new File(System.getProperty("java.io.tmpdir") + "/HFCSampletest.properties");

      final SampleStore sampleStore = new SampleStore(sampleStoreFile);

      // SampleUser can be any implementation that implements
      // org.hyperledger.fabric.sdk.User Interface

      ////////////////////////////
      // get users for all orgs

      for (SampleOrg sampleOrg : testSampleOrgs) {

        HFCAClient ca = sampleOrg.getCAClient();
        final String orgName = sampleOrg.getName();
        final String mspid = sampleOrg.getMSPID();
        ca.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        SampleUser admin = sampleStore.getMember(TEST_ADMIN_NAME, orgName);
        if (!admin.isEnrolled()) { // Preregistered admin only needs to be enrolled with Fabric
                                   // caClient.
          admin.setEnrollment(ca.enroll(admin.getName(), "adminpw"));
          admin.setMspId(mspid);
        }

        sampleOrg.setAdmin(admin); // The admin of this org --

        SampleUser user = sampleStore.getMember(TESTUSER_1_NAME, sampleOrg.getName());
        if (!user.isRegistered()) { // users need to be registered AND enrolled
          new RegistrationRequest(user.getName(), "org1.department1");
        }
        if (!user.isEnrolled()) {
          user.setMspId(mspid);
        }
        sampleOrg.addUser(user); // Remember user belongs to this Org

        final String sampleOrgName = sampleOrg.getName();
        final String sampleOrgDomainName = sampleOrg.getDomainName();

        SampleUser peerOrgAdmin =
            sampleStore.getMember(sampleOrgName + "Admin", sampleOrgName, sampleOrg.getMSPID(),
                findFileSk(Paths.get(testConfig.getTestChannlePath(),
                    "crypto-config/peerOrganizations/", sampleOrgDomainName,
                    format("/users/Admin@%s/msp/keystore", sampleOrgDomainName)).toFile()),
                Paths.get(testConfig.getTestChannlePath(), "crypto-config/peerOrganizations/",
                    sampleOrgDomainName, format("/users/Admin@%s/msp/signcerts/Admin@%s-cert.pem",
                        sampleOrgDomainName, sampleOrgDomainName))
                    .toFile());

        sampleOrg.setPeerAdmin(peerOrgAdmin); // A special user that can create channels, join peers
                                              // and install
                                              // chaincode

      }

      ////////////////////////////
      // Construct and run the channels
      SampleOrg sampleOrg = testConfig.getIntegrationTestsSampleOrg("peerOrg1");
      Channel fooChannel = constructChannel(CHANNEL_NAME, client, sampleOrg);

      log.info("That's all folks!");
      // client.setUserContext(sampleOrg.getUser(TESTUSER_1_NAME));
      channel = fooChannel;

    } catch (Exception e) {
      e.printStackTrace();

      log.error(e.getMessage());
    }
  }

  private File findFileSk(File directory) {

    File[] matches = directory.listFiles((dir, name) -> name.endsWith("_sk"));

    if (null == matches) {
      throw new RuntimeException(format("Matches returned null does %s directory exist?",
          directory.getAbsoluteFile().getName()));
    }

    if (matches.length != 1) {
      throw new RuntimeException(format("Expected in %s only 1 sk file but found %d",
          directory.getAbsoluteFile().getName(), matches.length));
    }

    return matches[0];

  }

  private Channel constructChannel(String name, HFClient client, SampleOrg sampleOrg)
      throws Exception {
    ////////////////////////////
    // Construct the channel
    //

    log.info("Constructing channel {}", name);

    // Only peer Admin org
    client.setUserContext(sampleOrg.getPeerAdmin());

    Collection<Orderer> orderers = new LinkedList<>();

    for (String orderName : sampleOrg.getOrdererNames()) {

      Properties ordererProperties = testConfig.getOrdererProperties(orderName);

      // example of setting keepAlive to avoid timeouts on inactive http2 connections.
      // Under 5 minutes would require changes to server side to accept faster ping
      // rates.
      ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime",
          new Object[] {5L, TimeUnit.MINUTES});
      ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout",
          new Object[] {8L, TimeUnit.SECONDS});

      orderers.add(
          client.newOrderer(orderName, sampleOrg.getOrdererLocation(orderName), ordererProperties));
    }

    // Just pick the first orderer in the list to create the channel.

    Orderer anOrderer = orderers.iterator().next();
    orderers.remove(anOrderer);

    // Create channel that has only one signer that is this orgs peer admin. If
    // channel creation policy needed more signature they would need to be added
    // too.
    // Channel newChannel = client.newChannel(name, anOrderer, channelConfiguration,
    // client.getChannelConfigurationSignature(channelConfiguration,
    // sampleOrg.getPeerAdmin()));

    Channel newChannel = client.newChannel(name);
    newChannel.addOrderer(anOrderer);

    log.info("Created channel {}", name);

    for (String peerName : sampleOrg.getPeerNames()) {
      String peerLocation = sampleOrg.getPeerLocation(peerName);

      Properties peerProperties = testConfig.getPeerProperties(peerName); // test properties for
                                                                          // peer.. if any.
      if (peerProperties == null) {
        peerProperties = new Properties();
      }
      // Example of setting specific options on grpc's NettyChannelBuilder
      peerProperties.put("grpc.NettyChannelBuilderOption.maxInboundMessageSize", 9000000);

      Peer peer = client.newPeer(peerName, peerLocation, peerProperties);
      newChannel.addPeer(peer);
      // log.info("Peer %s joined channel %s", peerName, name);
      sampleOrg.addPeer(peer);
    }

    for (Orderer orderer : orderers) { // add remaining orderers if any.
      newChannel.addOrderer(orderer);
    }

    for (String eventHubName : sampleOrg.getEventHubNames()) {

      final Properties eventHubProperties = testConfig.getEventHubProperties(eventHubName);

      eventHubProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime",
          new Object[] {5L, TimeUnit.MINUTES});
      eventHubProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout",
          new Object[] {8L, TimeUnit.SECONDS});

      EventHub eventHub = client.newEventHub(eventHubName,
          sampleOrg.getEventHubLocation(eventHubName), eventHubProperties);
      newChannel.addEventHub(eventHub);
    }

    newChannel.initialize();
    newChannel.setTransactionWaitTime(testConfig.getTransactionWaitTime());
    newChannel.setDeployWaitTime(testConfig.getDeployWaitTime());

    log.info("Finished initialization channel {}", name);

    return newChannel;

  }
}


class SampleOrg {
  final String name;
  final String mspid;
  HFCAClient caClient;

  Map<String, User> userMap = new HashMap<>();
  Map<String, String> peerLocations = new HashMap<>();
  Map<String, String> ordererLocations = new HashMap<>();
  Map<String, String> eventHubLocations = new HashMap<>();
  Set<Peer> peers = new HashSet<>();
  private SampleUser admin;
  private String caLocation;
  private Properties caProperties = null;

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

  public void setCAProperties(Properties caProperties) {
    this.caProperties = caProperties;
  }

  public Properties getCAProperties() {
    return caProperties;
  }

  public SampleUser getPeerAdmin() {
    return peerAdmin;
  }

  public void setPeerAdmin(SampleUser peerAdmin) {
    this.peerAdmin = peerAdmin;
  }

  public void setDomainName(String domainName) {
    this.domainName = domainName;
  }

  public String getDomainName() {
    return domainName;
  }
}


class SampleUser implements User, Serializable {
  private static final long serialVersionUID = 8077132186383604355L;

  private String name;
  private Set<String> roles;
  private String account;
  private String affiliation;
  private String organization;
  private String enrollmentSecret;
  Enrollment enrollment = null; // need access in test env.

  private transient SampleStore keyValStore;
  private String keyValStoreName;

  SampleUser(String name, String org, SampleStore fs) {
    this.name = name;

    this.keyValStore = fs;
    this.organization = org;
    this.keyValStoreName = toKeyValStoreName(this.name, org);
    String memberStr = keyValStore.getValue(keyValStoreName);
    if (null == memberStr) {
      saveState();
    } else {
      // restoreState();
    }

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
      e.printStackTrace();
    }
  }

  /**
   * Restore the state of this user from the key value store (if found). If not found, do nothing.
   */
  @CoverageIgnore
  SampleUser restoreState() {
    String memberStr = keyValStore.getValue(keyValStoreName);
    if (null != memberStr) {
      // The user was found in the key value store, so restore the
      // state.
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
        e.printStackTrace();
        throw new RuntimeException(String.format("Could not restore state of member %s", this.name),
            e);
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

  public void setEnrollment(Enrollment enrollment) {

    this.enrollment = enrollment;
    saveState();

  }

  public static String toKeyValStoreName(String name, String org) {
    return "user." + name + org;
  }

  @Override
  public String getMspId() {
    return mspId;
  }

  String mspId;

  public void setMspId(String mspID) {
    this.mspId = mspID;
    saveState();

  }
}


class SampleStore {

  private String file;
  private Log logger = LogFactory.getLog(SampleStore.class);

  public SampleStore(File file) {

    this.file = file.getAbsolutePath();
  }

  /**
   * Get the value associated with name.
   *
   * @param name
   * @return value associated with the name
   */
  public String getValue(String name) {
    Properties properties = loadProperties();
    return properties.getProperty(name);
  }

  private Properties loadProperties() {
    Properties properties = new Properties();
    try (InputStream input = new FileInputStream(file)) {
      properties.load(input);
      input.close();
    } catch (FileNotFoundException e) {
      logger.warn(String.format("Could not find the file \"%s\"", file));
    } catch (IOException e) {
      logger.warn(String.format("Could not load keyvalue store from file \"%s\", reason:%s", file,
          e.getMessage()));
    }

    return properties;
  }

  /**
   * Set the value associated with name.
   *
   * @param name The name of the parameter
   * @param value Value for the parameter
   */
  public void setValue(String name, String value) {
    Properties properties = loadProperties();
    try (OutputStream output = new FileOutputStream(file)) {
      properties.setProperty(name, value);
      properties.store(output, "");
      output.close();

    } catch (IOException e) {
      logger.warn(String.format("Could not save the keyvalue store, reason:%s", e.getMessage()));
    }
  }

  private final Map<String, SampleUser> members = new HashMap<>();

  /**
   * Get the user with a given name
   * 
   * @param name
   * @param org
   * @return user
   */
  public SampleUser getMember(String name, String org) {

    // Try to get the SampleUser state from the cache
    SampleUser sampleUser = members.get(SampleUser.toKeyValStoreName(name, org));
    if (null != sampleUser) {
      return sampleUser;
    }

    // Create the SampleUser and try to restore it's state from the key value store
    // (if found).
    sampleUser = new SampleUser(name, org, this);

    return sampleUser;

  }

  /**
   * Get the user with a given name
   * 
   * @param name
   * @param org
   * @param mspId
   * @param privateKeyFile
   * @param certificateFile
   * @return user
   * @throws IOException
   * @throws NoSuchAlgorithmException
   * @throws NoSuchProviderException
   * @throws InvalidKeySpecException
   */
  public SampleUser getMember(String name, String org, String mspId, File privateKeyFile,
      File certificateFile) throws IOException, NoSuchAlgorithmException, NoSuchProviderException,
      InvalidKeySpecException {

    try {
      // Try to get the SampleUser state from the cache
      SampleUser sampleUser = members.get(SampleUser.toKeyValStoreName(name, org));
      if (null != sampleUser) {
        return sampleUser;
      }

      // Create the SampleUser and try to restore it's state from the key value store
      // (if found).
      sampleUser = new SampleUser(name, org, this);
      sampleUser.setMspId(mspId);

      String certificate =
          new String(IOUtils.toByteArray(new FileInputStream(certificateFile)), "UTF-8");

      PrivateKey privateKey =
          getPrivateKeyFromBytes(IOUtils.toByteArray(new FileInputStream(privateKeyFile)));

      sampleUser.setEnrollment(new SampleStoreEnrollement(privateKey, certificate));

      sampleUser.saveState();

      return sampleUser;
    } catch (IOException e) {
      e.printStackTrace();
      throw e;

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      throw e;
    } catch (NoSuchProviderException e) {
      e.printStackTrace();
      throw e;
    } catch (InvalidKeySpecException e) {
      e.printStackTrace();
      throw e;
    } catch (ClassCastException e) {
      e.printStackTrace();
      throw e;
    }

  }

  static {
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
  }

  static PrivateKey getPrivateKeyFromBytes(byte[] data) throws IOException, NoSuchProviderException,
      NoSuchAlgorithmException, InvalidKeySpecException {
    final Reader pemReader = new StringReader(new String(data));

    final PrivateKeyInfo pemPair;
    try (PEMParser pemParser = new PEMParser(pemReader)) {
      pemPair = (PrivateKeyInfo) pemParser.readObject();
    }

    PrivateKey privateKey = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
        .getPrivateKey(pemPair);

    return privateKey;
  }

  static final class SampleStoreEnrollement implements Enrollment, Serializable {

    private static final long serialVersionUID = -2784835212445309006L;
    private final PrivateKey privateKey;
    private final String certificate;

    SampleStoreEnrollement(PrivateKey privateKey, String certificate) {

      this.certificate = certificate;

      this.privateKey = privateKey;
    }

    @Override
    public PrivateKey getKey() {

      return privateKey;
    }

    @Override
    public String getCert() {
      return certificate;
    }

  }

}


class TestConfigHelper {

  public static final String CONFIG_OVERRIDES = "FABRICSDKOVERRIDES";

  /**
   * clearConfig "resets" Config so that the Config testcases can run without interference from
   * other test suites. Depending on what order JUnit decides to run the tests, Config could have
   * been instantiated earlier and could contain values that make the tests here fail.
   * 
   * @throws SecurityException
   * @throws NoSuchFieldException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   *
   */
  public void clearConfig() throws NoSuchFieldException, SecurityException,
      IllegalArgumentException, IllegalAccessException {
    Config config = Config.getConfig();
    java.lang.reflect.Field configInstance = config.getClass().getDeclaredField("config");
    configInstance.setAccessible(true);
    configInstance.set(null, null);
  }

  /**
   * customizeConfig() sets up the properties listed by env var CONFIG_OVERRIDES The value of the
   * env var is <i>property1=value1,property2=value2</i> and so on where each <i>property</i> is a
   * property from the SDK's config file.
   *
   * @throws NoSuchFieldException
   * @throws SecurityException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   */
  public void customizeConfig() throws NoSuchFieldException, SecurityException,
      IllegalArgumentException, IllegalAccessException {
    String fabricSdkConfig = System.getenv(CONFIG_OVERRIDES);
    if (fabricSdkConfig != null && fabricSdkConfig.length() > 0) {
      String[] configs = fabricSdkConfig.split(",");
      String[] configKeyValue;
      for (String config : configs) {
        configKeyValue = config.split("=");
        if (configKeyValue != null && configKeyValue.length == 2) {
          System.setProperty(configKeyValue[0], configKeyValue[1]);
        }
      }
    }
  }

}


class TestConfig {
  private static final Log logger = LogFactory.getLog(TestConfig.class);

  private static final String DEFAULT_CONFIG =
      "src/test/java/org/hyperledger/fabric/sdk/testutils.properties";
  private static final String ORG_HYPERLEDGER_FABRIC_SDK_CONFIGURATION =
      "org.hyperledger.fabric.sdktest.configuration";

  private static final String PROPBASE = "org.hyperledger.fabric.sdktest.";

  private static final String GOSSIPWAITTIME = PROPBASE + "GossipWaitTime";
  private static final String INVOKEWAITTIME = PROPBASE + "InvokeWaitTime";
  private static final String DEPLOYWAITTIME = PROPBASE + "DeployWaitTime";
  private static final String PROPOSALWAITTIME = PROPBASE + "ProposalWaitTime";

  private static final String INTEGRATIONTESTS_ORG = PROPBASE + "integrationTests.org.";
  private static final Pattern orgPat =
      Pattern.compile("^" + Pattern.quote(INTEGRATIONTESTS_ORG) + "([^\\.]+)\\.mspid$");

  private static final String INTEGRATIONTESTSTLS = PROPBASE + "integrationtests.tls";

  private static TestConfig config;
  private final static Properties sdkProperties = new Properties();
  private final boolean runningTLS;
  private final boolean runningFabricCATLS;
  private final boolean runningFabricTLS;
  private final static HashMap<String, SampleOrg> sampleOrgs = new HashMap<>();

  private TestConfig() {
    File loadFile;
    FileInputStream configProps;

    try {
      loadFile =
          new File(System.getProperty(ORG_HYPERLEDGER_FABRIC_SDK_CONFIGURATION, DEFAULT_CONFIG))
              .getAbsoluteFile();
      logger.debug(String.format("Loading configuration from %s and it is present: %b",
          loadFile.toString(), loadFile.exists()));
      configProps = new FileInputStream(loadFile);
      sdkProperties.load(configProps);

    } catch (IOException e) { // if not there no worries just use defaults
      // logger.warn(String.format("Failed to load any test configuration from: %s.
      // Using toolkit defaults",
      // DEFAULT_CONFIG));
    } finally {

      // Default values

      defaultProperty(GOSSIPWAITTIME, "5000");
      defaultProperty(INVOKEWAITTIME, "100000");
      defaultProperty(DEPLOYWAITTIME, "120000");
      defaultProperty(PROPOSALWAITTIME, "120000");

      //////
      defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg1.mspid", "Org1MSP");
      defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg1.domname", "org1.example.com");
      defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg1.ca_location", "http://localhost:7054");
      defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg1.peer_locations",
          "peer0.org1.example.com@grpc://localhost:7051, peer1.org1.example.com@grpc://localhost:7056");
      defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg1.orderer_locations",
          "orderer.example.com@grpc://localhost:7050");
      defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg1.eventhub_locations",
          "peer0.org1.example.com@grpc://localhost:7053,peer1.org1.example.com@grpc://localhost:7058");
      defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg2.mspid", "Org2MSP");
      defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg2.domname", "org2.example.com");
      defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg2.ca_location", "http://localhost:8054");
      defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg2.peer_locations",
          "peer0.org2.example.com@grpc://localhost:8051,peer1.org2.example.com@grpc://localhost:8056");
      defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg2.orderer_locations",
          "orderer.example.com@grpc://localhost:7050");
      defaultProperty(INTEGRATIONTESTS_ORG + "peerOrg2.eventhub_locations",
          "peer0.org2.example.com@grpc://localhost:8053, peer1.org2.example.com@grpc://localhost:8058");

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

        String peerNames =
            sdkProperties.getProperty(INTEGRATIONTESTS_ORG + orgName + ".peer_locations");
        String[] ps = peerNames.split("[ \t]*,[ \t]*");
        for (String peer : ps) {
          String[] nl = peer.split("[ \t]*@[ \t]*");
          sampleOrg.addPeerLocation(nl[0], grpcTLSify(nl[1]));
        }

        final String domainName =
            sdkProperties.getProperty(INTEGRATIONTESTS_ORG + orgName + ".domname");

        sampleOrg.setDomainName(domainName);

        String ordererNames =
            sdkProperties.getProperty(INTEGRATIONTESTS_ORG + orgName + ".orderer_locations");
        ps = ordererNames.split("[ \t]*,[ \t]*");
        for (String peer : ps) {
          String[] nl = peer.split("[ \t]*@[ \t]*");
          sampleOrg.addOrdererLocation(nl[0], grpcTLSify(nl[1]));
        }

        String eventHubNames =
            sdkProperties.getProperty(INTEGRATIONTESTS_ORG + orgName + ".eventhub_locations");
        ps = eventHubNames.split("[ \t]*,[ \t]*");
        for (String peer : ps) {
          String[] nl = peer.split("[ \t]*@[ \t]*");
          sampleOrg.addEventHubLocation(nl[0], grpcTLSify(nl[1]));
        }

        sampleOrg.setCALocation(httpTLSify(
            sdkProperties.getProperty((INTEGRATIONTESTS_ORG + org.getKey() + ".ca_location"))));

        if (runningFabricCATLS) {
          String cert =
              "src/test/fixture/sdkintegration/e2e-2Orgs/channel/crypto-config/peerOrganizations/DNAME/ca/ca.DNAME-cert.pem"
                  .replaceAll("DNAME", domainName);
          File cf = new File(cert);
          if (!cf.exists() || !cf.isFile()) {
            throw new RuntimeException("TEST is missing cert file " + cf.getAbsolutePath());
          }
          Properties properties = new Properties();
          properties.setProperty("pemFile", cf.getAbsolutePath());

          properties.setProperty("allowAllHostNames", "true");// testing environment only NOT FOR
                                                              // PRODUCTION!

          sampleOrg.setCAProperties(properties);
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
   * getConfig return back singleton for SDK configuration.
   *
   * @return Global configuration
   */
  public static TestConfig getConfig() {
    if (null == config) {
      config = new TestConfig();
    }
    return config;

  }

  /**
   * getProperty return back property for the given value.
   *
   * @param property
   * @return String value for the property
   */
  private String getProperty(String property) {

    String ret = sdkProperties.getProperty(property);

    if (null == ret) {
      logger.warn(String.format("No configuration value found for '%s'", property));
    }
    return ret;
  }

  static private void defaultProperty(String key, String value) {

    String ret = System.getProperty(key);
    if (ret != null) {
      sdkProperties.put(key, ret);
    } else {
      String envKey = key.toUpperCase().replaceAll("\\.", "_");
      ret = System.getenv(envKey);
      if (null != ret) {
        sdkProperties.put(key, ret);
      } else {
        if (null == sdkProperties.getProperty(key) && value != null) {
          sdkProperties.put(key, value);
        }

      }

    }
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
   *
   * @return
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

  // private final static String tlsbase =
  // "src/test/fixture/sdkintegration/e2e-2Orgs/tls/";

  public Properties getPeerProperties(String name) {

    return getEndPointProperties("peer", name);

  }

  public Properties getOrdererProperties(String name) {

    return getEndPointProperties("orderer", name);

  }

  private Properties getEndPointProperties(final String type, final String name) {

    final String domainName = getDomainName(name);

    File cert = Paths
        .get(getTestChannlePath(), "crypto-config/ordererOrganizations".replace("orderer", type),
            domainName, type + "s", name, "tls/server.crt")
        .toFile();
    if (!cert.exists()) {
      throw new RuntimeException(
          String.format("Missing cert file for: %s. Could not find at location: %s", name,
              cert.getAbsolutePath()));
    }

    Properties ret = new Properties();
    ret.setProperty("pemFile", cert.getAbsolutePath());
    // ret.setProperty("trustServerCertificate", "true"); //testing environment only
    // NOT FOR PRODUCTION!
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

  private String getDomainName(final String name) {
    int dot = name.indexOf(".");
    if (-1 == dot) {
      return null;
    } else {
      return name.substring(dot + 1);
    }

  }
}
