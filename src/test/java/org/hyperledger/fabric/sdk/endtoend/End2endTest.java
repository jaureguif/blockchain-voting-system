/*
 *  Copyright 2016, 2017 DTCC, Fujitsu Australia Software Technology, IBM - All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.hyperledger.fabric.sdk.endtoend;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ChaincodeEndorsementPolicy;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.ChannelConfiguration;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.InstallProposalRequest;
import org.hyperledger.fabric.sdk.InstantiateProposalRequest;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.SDKUtils;
import org.hyperledger.fabric.sdk.TestConfigHelper;
import org.hyperledger.fabric.sdk.exception.TransactionEventException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.testutils.TestConfig;
import org.hyperledger.fabric.sdkintegration.SampleOrg;
import org.hyperledger.fabric.sdkintegration.SampleStore;
import org.hyperledger.fabric.sdkintegration.SampleUser;
import org.hyperledger.fabric.sdkintegration.Util;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * Test end to end scenario
 */
public class End2endTest {

  private static final TestConfig testConfig = TestConfig.getConfig();
  private static final String TEST_ADMIN_NAME = "admin";
  private static final String TESTUSER_1_NAME = "user1";
  private static final String TEST_FIXTURES_PATH = "src/test/fixture";

  private static final String CHAIN_CODE_NAME = "asset_t_smart_contract_go";
  private static final String CHAIN_CODE_PATH = "com.epam.blockchain.chaincode/asset_t_smart_contract";
  private static final String CHAIN_CODE_VERSION = "1";

  private static final String FOO_CHANNEL_NAME = "foo";
  private static final String BAR_CHANNEL_NAME = "bar";
  private static final Map<String, String> TX_EXPECTED;

  static {
    TX_EXPECTED = new HashMap<>();
    TX_EXPECTED.put("readset1", "Missing readset for channel bar block 1");
    TX_EXPECTED.put("writeset1", "Missing writeset for channel bar block 1");
  }

  private final TestConfigHelper configHelper = new TestConfigHelper();
  String testTxID = null;  // save the CC invoke TxID and use in queries
  private Collection<SampleOrg> testSampleOrgs;

  static void out(String format, Object... args) {

    System.err.flush();
    System.out.flush();

    System.out.println(format(format, args));
    System.err.flush();
    System.out.flush();

  }

  static String printableString(final String string) {
    int maxLogStringLength = 64;
    if (string == null || string.length() == 0) {
      return string;
    }

    String ret = string.replaceAll("[^\\p{Print}]", "?");

    ret = ret.substring(0, Math.min(ret.length(), maxLogStringLength)) + (
        ret.length() > maxLogStringLength ? "..." : "");

    return ret;

  }

  @Before
  public void checkConfig()
      throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, MalformedURLException {
    out("\n\n\nRUNNING: End2endIT.\n");
    configHelper.clearConfig();
    configHelper.customizeConfig();

    testSampleOrgs = testConfig.getIntegrationTestsSampleOrgs();
    //Set up hfca for each sample org

    for (SampleOrg sampleOrg : testSampleOrgs) {
      sampleOrg.setCAClient(
          HFCAClient.createNewInstance(sampleOrg.getCALocation(), sampleOrg.getCAProperties()));
    }
  }

  @After
  public void clearConfig() {
    try {
      configHelper.clearConfig();
    } catch (Exception e) {
    }
  }

  @Test
  public void setup() {

    try {

      ////////////////////////////
      // Setup client

      //Create instance of client.
      HFClient client = HFClient.createNewInstance();

      client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());

      // client.setMemberServices(peerOrg1FabricCA);

      ////////////////////////////
      //Set up USERS

      //Persistence is not part of SDK. Sample file store is for demonstration purposes only!
      //   MUST be replaced with more robust application implementation  (Database, LDAP)
      File sampleStoreFile = new File(
          System.getProperty("java.io.tmpdir") + "/HFCSampletest.properties");
      if (sampleStoreFile.exists()) { //For testing start fresh
        sampleStoreFile.delete();
      }

      final SampleStore sampleStore = new SampleStore(sampleStoreFile);
      //  sampleStoreFile.deleteOnExit();

      //SampleUser can be any implementation that implements org.hyperledger.fabric.sdk.User Interface

      ////////////////////////////
      // get users for all orgs

      for (SampleOrg sampleOrg : testSampleOrgs) {

        HFCAClient ca = sampleOrg.getCAClient();
        final String orgName = sampleOrg.getName();
        final String mspid = sampleOrg.getMSPID();
        ca.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        SampleUser admin = sampleStore.getMember(TEST_ADMIN_NAME, orgName);
        if (!admin
            .isEnrolled()) {  //Preregistered admin only needs to be enrolled with Fabric caClient.
          admin.setEnrollment(ca.enroll(admin.getName(), "adminpw"));
          admin.setMspId(mspid);
        }

        sampleOrg.setAdmin(admin); // The admin of this org --

        SampleUser user = sampleStore.getMember(TESTUSER_1_NAME, sampleOrg.getName());
        if (!user.isRegistered()) {  // users need to be registered AND enrolled
          RegistrationRequest rr = new RegistrationRequest(user.getName(), "org1.department1");
          user.setEnrollmentSecret(ca.register(rr, admin));
        }
        if (!user.isEnrolled()) {
          user.setEnrollment(ca.enroll(user.getName(), user.getEnrollmentSecret()));
          user.setMspId(mspid);
        }
        sampleOrg.addUser(user); //Remember user belongs to this Org

        final String sampleOrgName = sampleOrg.getName();
        final String sampleOrgDomainName = sampleOrg.getDomainName();

        // src/test/fixture/sdkintegration/e2e-2Orgs/channel/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/keystore/

        SampleUser peerOrgAdmin = sampleStore
            .getMember(sampleOrgName + "Admin", sampleOrgName, sampleOrg.getMSPID(),
                findFileSk(
                    Paths.get(testConfig.getTestChannlePath(), "crypto-config/peerOrganizations/",
                        sampleOrgDomainName,
                        format("/users/Admin@%s/msp/keystore", sampleOrgDomainName)).toFile()),
                Paths.get(testConfig.getTestChannlePath(), "crypto-config/peerOrganizations/",
                    sampleOrgDomainName,
                    format("/users/Admin@%s/msp/signcerts/Admin@%s-cert.pem", sampleOrgDomainName,
                        sampleOrgDomainName)).toFile());

        sampleOrg.setPeerAdmin(
            peerOrgAdmin); //A special user that can create channels, join peers and install chaincode

      }

      ////////////////////////////
      //Construct and run the channels
      SampleOrg sampleOrg = testConfig.getIntegrationTestsSampleOrg("peerOrg1");
      Channel fooChannel = constructChannel(FOO_CHANNEL_NAME, client, sampleOrg);
      runChannel(client, fooChannel, true, sampleOrg, 0);
      fooChannel.shutdown(true); // Force foo channel to shutdown clean up resources.
      out("\n");

      sampleOrg = testConfig.getIntegrationTestsSampleOrg("peerOrg2");
      Channel barChannel = constructChannel(BAR_CHANNEL_NAME, client, sampleOrg);
      runChannel(client, barChannel, true, sampleOrg,
          100); //run a newly constructed bar channel with different b value!
      //let bar channel just shutdown so we have both scenarios.

      out("That's all folks!");

    } catch (Exception e) {
      e.printStackTrace();

      fail(e.getMessage());
    }

  }

  void runChannel(HFClient client, Channel channel, boolean installChaincode, SampleOrg sampleOrg,
      int delta) {

    try {

      final String channelName = channel.getName();
      boolean isFooChain = FOO_CHANNEL_NAME.equals(channelName);
      out("Running channel %s", channelName);
      channel.setTransactionWaitTime(testConfig.getTransactionWaitTime());
      channel.setDeployWaitTime(testConfig.getDeployWaitTime());

      Collection<Peer> channelPeers = channel.getPeers();
      Collection<Orderer> orderers = channel.getOrderers();
      final ChaincodeID chaincodeID;
      Collection<ProposalResponse> responses;
      Collection<ProposalResponse> successful = new LinkedList<>();
      Collection<ProposalResponse> failed = new LinkedList<>();

      chaincodeID = ChaincodeID.newBuilder().setName(CHAIN_CODE_NAME)
                               .setVersion(CHAIN_CODE_VERSION)
                               .setPath(CHAIN_CODE_PATH).build();

      if (installChaincode) {
        ////////////////////////////
        // Install Proposal Request
        //

        client.setUserContext(sampleOrg.getPeerAdmin());

        out("Creating install proposal");

        InstallProposalRequest installProposalRequest = client.newInstallProposalRequest();
        installProposalRequest.setChaincodeID(chaincodeID);

        if (isFooChain) {
          // on foo chain install from directory.

          ////For GO language and serving just a single user, chaincodeSource is mostly likely the users GOPATH
          installProposalRequest.setChaincodeSourceLocation(
              new File(TEST_FIXTURES_PATH + "/sdkintegration/gocc"));
        } else {
          // On bar chain install from an input stream.

          installProposalRequest.setChaincodeInputStream(Util.generateTarGzInputStream(
              (Paths.get(TEST_FIXTURES_PATH, "/sdkintegration/gocc", "src", CHAIN_CODE_PATH)
                    .toFile()),
              Paths.get("src", CHAIN_CODE_PATH).toString()));

        }

        installProposalRequest.setChaincodeVersion(CHAIN_CODE_VERSION);

        out("Sending install proposal");

        ////////////////////////////
        // only a client from the same org as the peer can issue an install request
        int numInstallProposal = 0;
        //    Set<String> orgs = orgPeers.keySet();
        //   for (SampleOrg org : testSampleOrgs) {

        Set<Peer> peersFromOrg = sampleOrg.getPeers();
        numInstallProposal = numInstallProposal + peersFromOrg.size();
        responses = client.sendInstallProposal(installProposalRequest, peersFromOrg);

        for (ProposalResponse response : responses) {
          if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
            out("Successful install proposal response Txid: %s from peer %s",
                response.getTransactionID(), response.getPeer().getName());
            successful.add(response);
          } else {
            failed.add(response);
          }
        }

        SDKUtils.getProposalConsistencySets(responses);
        //   }
        out("Received %d install proposal responses. Successful+verified: %d . Failed: %d",
            numInstallProposal, successful.size(), failed.size());

        if (failed.size() > 0) {
          ProposalResponse first = failed.iterator().next();
          fail("Not enough endorsers for install :" + successful.size() + ".  " + first
              .getMessage());
        }
      }

      //   client.setUserContext(sampleOrg.getUser(TEST_ADMIN_NAME));
      //  final ChaincodeID chaincodeID = firstInstallProposalResponse.getChaincodeID();
      // Note installing chaincode does not require transaction no need to
      // send to Orderers

      ///////////////
      //// Instantiate chaincode.
      InstantiateProposalRequest instantiateProposalRequest = client
          .newInstantiationProposalRequest();
      instantiateProposalRequest.setProposalWaitTime(testConfig.getProposalWaitTime());
      instantiateProposalRequest.setChaincodeID(chaincodeID);
      instantiateProposalRequest.setFcn("init");
      instantiateProposalRequest.setArgs(new String[0]);
      Map<String, byte[]> tm = new HashMap<>();
      tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
      tm.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
      instantiateProposalRequest.setTransientMap(tm);

            /*
              policy OR(Org1MSP.member, Org2MSP.member) meaning 1 signature from someone in either Org1 or Org2
              See README.md Chaincode endorsement policies section for more details.
            */
      ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
      chaincodeEndorsementPolicy.fromYamlFile(
          new File(TEST_FIXTURES_PATH + "/sdkintegration/chaincodeendorsementpolicy.yaml"));
      instantiateProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);

      out("Sending instantiateProposalRequest to all peers");
      successful.clear();
      failed.clear();

      if (isFooChain) {  //Send responses both ways with specifying peers and by using those on the channel.
        responses = channel
            .sendInstantiationProposal(instantiateProposalRequest, channel.getPeers());
      } else {
        responses = channel.sendInstantiationProposal(instantiateProposalRequest);

      }
      for (ProposalResponse response : responses) {
        if (response.isVerified() && response.getStatus() == ProposalResponse.Status.SUCCESS) {
          successful.add(response);
          out("Succesful instantiate proposal response Txid: %s from peer %s",
              response.getTransactionID(), response.getPeer().getName());
        } else {
          failed.add(response);
        }
      }
      out("Received %d instantiate proposal responses. Successful+verified: %d . Failed: %d",
          responses.size(), successful.size(), failed.size());
      if (failed.size() > 0) {
        ProposalResponse first = failed.iterator().next();
        fail("Not enough endorsers for instantiate :" + successful.size() + "endorser failed with "
            + first.getMessage() + ". Was verified:" + first.isVerified());
      }

      /*******/
      /*******/

      ///////////////
      /// Send instantiate transaction to orderer
      out("Sending instantiateTransaction to orderer ");
      channel.sendTransaction(successful, orderers).thenApply(transactionEvent -> {

        try {

          waitOnFabric(0);

          out("Finished transaction with transaction id %s", transactionEvent.getTransactionID());
          testTxID = transactionEvent.getTransactionID(); // used in the channel queries later

          out("Now query chaincode");
          QueryByChaincodeRequest queryByChaincodeRequest = client.newQueryProposalRequest();
          queryByChaincodeRequest.setArgs(new String[]{"query", "14a12ef0-9409-4872-9341-9ab003059ce9"});
          queryByChaincodeRequest.setFcn("invoke");
          queryByChaincodeRequest.setChaincodeID(chaincodeID);

          Map<String, byte[]> tm2 = new HashMap<>();
          tm2.put("HyperLedgerFabric", "QueryByChaincodeRequest:JavaSDK".getBytes(UTF_8));
          tm2.put("method", "QueryByChaincodeRequest".getBytes(UTF_8));
          queryByChaincodeRequest.setTransientMap(tm2);

          Collection<ProposalResponse> queryProposals = channel
              .queryByChaincode(queryByChaincodeRequest, channel.getPeers());
          for (ProposalResponse proposalResponse : queryProposals) {
            if (!proposalResponse.isVerified()
                || proposalResponse.getStatus() != ProposalResponse.Status.SUCCESS) {
              fail("Failed query proposal from peer " + proposalResponse.getPeer().getName()
                  + " status: " + proposalResponse.getStatus() +
                  ". Messages: " + proposalResponse.getMessage()
                  + ". Was verified : " + proposalResponse.isVerified());
            } else {
              String payload = proposalResponse.getProposalResponse().getResponse().getPayload()
                                               .toStringUtf8();
              out("Query payload from peer %s returned %s", proposalResponse.getPeer().getName(),
                  payload);
              assertThat(payload).isNotNull().isNotBlank();
            }
          }

          return null;
        } catch (Exception e) {
          out("Caught exception while running query");
          e.printStackTrace();
          fail("Failed during chaincode query with error : " + e.getMessage());
        }

        return null;
      }).exceptionally(e -> {
        if (e instanceof TransactionEventException) {
          BlockEvent.TransactionEvent te = ((TransactionEventException) e).getTransactionEvent();
          if (te != null) {
            fail(format("Transaction with txid %s failed. %s", te.getTransactionID(),
                e.getMessage()));
          }
        }
        fail(format("Test failed with %s exception %s", e.getClass().getName(), e.getMessage()));

        return null;
      }).get(testConfig.getTransactionWaitTime(), TimeUnit.SECONDS);

      out("Running for Channel %s done", channelName);

    } catch (Exception e) {
      out("Caught an exception running channel %s", channel.getName());
      e.printStackTrace();
      fail("Test failed with error : " + e.getMessage());
    }
  }

  private Channel constructChannel(String name, HFClient client, SampleOrg sampleOrg)
      throws Exception {
    ////////////////////////////
    //Construct the channel
    //

    out("Constructing channel %s", name);

    //Only peer Admin org
    client.setUserContext(sampleOrg.getPeerAdmin());

    Collection<Orderer> orderers = new LinkedList<>();

    for (String orderName : sampleOrg.getOrdererNames()) {

      Properties ordererProperties = testConfig.getOrdererProperties(orderName);

      //example of setting keepAlive to avoid timeouts on inactive http2 connections.
      // Under 5 minutes would require changes to server side to accept faster ping rates.
      ordererProperties
          .put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[]{5L, TimeUnit.MINUTES});
      ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout",
          new Object[]{8L, TimeUnit.SECONDS});

      orderers.add(client.newOrderer(orderName, sampleOrg.getOrdererLocation(orderName),
          ordererProperties));
    }

    //Just pick the first orderer in the list to create the channel.

    Orderer anOrderer = orderers.iterator().next();
    orderers.remove(anOrderer);

    ChannelConfiguration channelConfiguration = new ChannelConfiguration(
        new File(TEST_FIXTURES_PATH + "/sdkintegration/e2e-2Orgs/channel/" + name + ".tx"));

    //Create channel that has only one signer that is this orgs peer admin. If channel creation policy needed more signature they would need to be added too.
    Channel newChannel = client.newChannel(name, anOrderer, channelConfiguration,
        client.getChannelConfigurationSignature(channelConfiguration, sampleOrg.getPeerAdmin()));

    out("Created channel %s", name);

    for (String peerName : sampleOrg.getPeerNames()) {
      String peerLocation = sampleOrg.getPeerLocation(peerName);

      Properties peerProperties = testConfig
          .getPeerProperties(peerName); //test properties for peer.. if any.
      if (peerProperties == null) {
        peerProperties = new Properties();
      }
      //Example of setting specific options on grpc's NettyChannelBuilder
      peerProperties.put("grpc.NettyChannelBuilderOption.maxInboundMessageSize", 9000000);

      Peer peer = client.newPeer(peerName, peerLocation, peerProperties);
      newChannel.joinPeer(peer);
      out("Peer %s joined channel %s", peerName, name);
      sampleOrg.addPeer(peer);
    }

    for (Orderer orderer : orderers) { //add remaining orderers if any.
      newChannel.addOrderer(orderer);
    }

    for (String eventHubName : sampleOrg.getEventHubNames()) {

      final Properties eventHubProperties = testConfig.getEventHubProperties(eventHubName);

      eventHubProperties
          .put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[]{5L, TimeUnit.MINUTES});
      eventHubProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout",
          new Object[]{8L, TimeUnit.SECONDS});

      EventHub eventHub = client
          .newEventHub(eventHubName, sampleOrg.getEventHubLocation(eventHubName),
              eventHubProperties);
      newChannel.addEventHub(eventHub);
    }

    newChannel.initialize();

    out("Finished initialization channel %s", name);

    return newChannel;

  }

  private void waitOnFabric(int additional) {
    // wait a few seconds for the peers to catch up with each other via the gossip network.
    // Another way would be to wait on all the peers event hubs for the event containing the transaction TxID
//        try {
//            out("Wait %d milliseconds for peers to sync with each other", gossipWaitTime + additional);
//            TimeUnit.MILLISECONDS.sleep(gossipWaitTime + additional);
//        } catch (InterruptedException e) {
//            fail("should not have jumped out of sleep mode. No other threads should be running");
//        }
  }

  File findFileSk(File directory) {

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

}