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
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
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
  private static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
  private static final String EXPECTED_EVENT_NAME = "event";
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

  @Before
  public void checkConfig() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, MalformedURLException {
    out("\n\n\nRUNNING: End2endIT.\n");
    configHelper.clearConfig();
    configHelper.customizeConfig();

    testSampleOrgs = testConfig.getIntegrationTestsSampleOrgs();
    //Set up hfca for each sample org

    for (SampleOrg sampleOrg : testSampleOrgs) {
      sampleOrg.setCAClient(HFCAClient.createNewInstance(sampleOrg.getCALocation(), sampleOrg.getCAProperties()));
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
      File sampleStoreFile = new File(System.getProperty("java.io.tmpdir") + "/HFCSampletest.properties");
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
        if (!admin.isEnrolled()) {  //Preregistered admin only needs to be enrolled with Fabric caClient.
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

        sampleOrg.setPeerAdmin(peerOrgAdmin); //A special user that can create channels, join peers and install chaincode

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

      @SuppressWarnings("unused")
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
          queryByChaincodeRequest
              .setArgs(new String[]{"query", "14a12ef0-9409-4872-9341-9ab003059ce9"});
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
      }).thenApply(transactionEvent -> {
         try {

           waitOnFabric(0);

           successful.clear();
           failed.clear();

           client.setUserContext(sampleOrg.getUser(TESTUSER_1_NAME));

           ///////////////
           /// Send transaction proposal to all peers
           TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
           String[] args = {
             "create",
                "3aecedb1-a20b-4d38-bb77-db8444790f39",
                "T800",
                "TERMINATOR",
                "Cyberdyne Systems", 
                "Model 101 Series 800 Terminator",
                "SKYNET_ID",
                "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eGlmOkNvbXByZXNzaW9uPgogIDxleGlmOlhSZXNvbHV0aW9uPjcyPC9leGlmOlhSZXNvbHV0aW9uPgogIDxleGlmOllSZXNvbHV0aW9uPjcyPC9leGlmOllSZXNvbHV0aW9uPgogIDxleGlmOlJlc29sdXRpb25Vbml0PkluY2g8L2V4aWY6UmVzb2x1dGlvblVuaXQ+CiAgPGV4aWY6Rmxhc2hQaXhWZXJzaW9uPkZsYXNoUGl4IFZlcnNpb24gMS4wPC9leGlmOkZsYXNoUGl4VmVyc2lvbj4KICA8ZXhpZjpPcmllbnRhdGlvbj5Ub3AtbGVmdDwvZXhpZjpPcmllbnRhdGlvbj4KICA8ZXhpZjpYUmVzb2x1dGlvbj43MjwvZXhpZjpYUmVzb2x1dGlvbj4KICA8ZXhpZjpZUmVzb2x1dGlvbj43MjwvZXhpZjpZUmVzb2x1dGlvbj4KICA8ZXhpZjpSZXNvbHV0aW9uVW5pdD5JbmNoPC9leGlmOlJlc29sdXRpb25Vbml0PgogIDxleGlmOlNvZnR3YXJlPkdJTVAgMi44LjIyPC9leGlmOlNvZnR3YXJlPgogIDxleGlmOkRhdGVUaW1lPjIwMTg6MDI6MjIgMTU6NTg6NTg8L2V4aWY6RGF0ZVRpbWU+CiAgPGV4aWY6Q29tcHJlc3Npb24+SlBFRyBjb21wcmVzc2lvbjwvZXhpZjpDb21wcmVzc2lvbj4KICA8ZXhpZjpYUmVzb2x1dGlvbj43MjwvZXhpZjpYUmVzb2x1dGlvbj4KICA8ZXhpZjpZUmVzb2x1dGlvbj43MjwvZXhpZjpZUmVzb2x1dGlvbj4KICA8ZXhpZjpSZXNvbHV0aW9uVW5pdD5JbmNoPC9leGlmOlJlc29sdXRpb25Vbml0PgogIDxleGlmOkZsYXNoUGl4VmVyc2lvbj5GbGFzaFBpeCBWZXJzaW9uIDEuMDwvZXhpZjpGbGFzaFBpeFZlcnNpb24+CiAgPGV4aWY6T3JpZW50YXRpb24+VG9wLWxlZnQ8L2V4aWY6T3JpZW50YXRpb24+CiAgPGV4aWY6WFJlc29sdXRpb24+NzIuMDAwMDwvZXhpZjpYUmVzb2x1dGlvbj4KICA8ZXhpZjpZUmVzb2x1dGlvbj43Mi4wMDAwPC9leGlmOllSZXNvbHV0aW9uPgogIDxleGlmOlJlc29sdXRpb25Vbml0PkluY2g8L2V4aWY6UmVzb2x1dGlvblVuaXQ+CiAgPGV4aWY6U29mdHdhcmU+QWRvYmUgUGhvdG9zaG9wIENTNSBXaW5kb3dzPC9leGlmOlNvZnR3YXJlPgogIDxleGlmOkRhdGVUaW1lPjIwMTE6MDU6MTggMDA6MDU6MTI8L2V4aWY6RGF0ZVRpbWU+CiAgPGV4aWY6Q29tcHJlc3Npb24+SlBFRyBjb21wcmVzc2lvbjwvZXhpZjpDb21wcmVzc2lvbj4KICA8ZXhpZjpYUmVzb2x1dGlvbj43MjwvZXhpZjpYUmVzb2x1dGlvbj4KICA8ZXhpZjpZUmVzb2x1dGlvbj43MjwvZXhpZjpZUmVzb2x1dGlvbj4KICA8ZXhpZjpSZXNvbHV0aW9uVW5pdD5JbmNoPC9leGlmOlJlc29sdXRpb25Vbml0PgogIDxleGlmOkV4aWZWZXJzaW9uPkV4aWYgVmVyc2lvbiAyLjE8L2V4aWY6RXhpZlZlcnNpb24+CiAgPGV4aWY6Rmxhc2hQaXhWZXJzaW9uPkZsYXNoUGl4IFZlcnNpb24gMS4wPC9leGlmOkZsYXNoUGl4VmVyc2lvbj4KICA8ZXhpZjpDb2xvclNwYWNlPnNSR0I8L2V4aWY6Q29sb3JTcGFjZT4KICA8ZXhpZjpQaXhlbFhEaW1lbnNpb24+MTAyNDwvZXhpZjpQaXhlbFhEaW1lbnNpb24+CiAgPGV4aWY6UGl4ZWxZRGltZW5zaW9uPjc0MjwvZXhpZjpQaXhlbFlEaW1lbnNpb24+CiA8L3JkZjpEZXNjcmlwdGlvbj4KCjwvcmRmOlJERj4KPC94OnhtcG1ldGE+Cjw/eHBhY2tldCBlbmQ9J3InPz4K/+IMWElDQ19QUk9GSUxFAAEBAAAMSExpbm8CEAAAbW50clJHQiBYWVogB84AAgAJAAYAMQAAYWNzcE1TRlQAAAAASUVDIHNSR0IAAAAAAAAAAAAAAAEAAPbWAAEAAAAA0y1IUCAgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARY3BydAAAAVAAAAAzZGVzYwAAAYQAAABsd3RwdAAAAfAAAAAUYmtwdAAAAgQAAAAUclhZWgAAAhgAAAAUZ1hZ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",
                "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",
                "CREATED",
                "CREATED",
           };
           transactionProposalRequest.setArgs(args);
           transactionProposalRequest.setFcn("invoke");
           transactionProposalRequest.setProposalWaitTime(testConfig.getProposalWaitTime());
           transactionProposalRequest.setChaincodeID(chaincodeID);

           Map<String, byte[]> tm2 = new HashMap<>();
           tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
           tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));
           tm2.put(EXPECTED_EVENT_NAME, EXPECTED_EVENT_DATA);  //This should trigger an event see chaincode why.
           transactionProposalRequest.setTransientMap(tm2);

           out("sending transactionProposal to all peers with arguments: create()");

           Collection<ProposalResponse> transactionPropResp = channel.sendTransactionProposal(transactionProposalRequest, channel.getPeers());
           for (ProposalResponse response : transactionPropResp) {
             if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
               out("Successful transaction proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName());
               successful.add(response);
             } else {
               failed.add(response);
             }
           }

           // Check that all the proposals are consistent with each other. We should have only one set
           // where all the proposals above are consistent. Note the when sending to Orderer this is done automatically.
           //  Shown here as an example that applications can invoke and select.
           // See org.hyperledger.fabric.sdk.proposal.consistency_validation config property.
           Collection<Set<ProposalResponse>> proposalConsistencySets = SDKUtils.getProposalConsistencySets(transactionPropResp);
           if (proposalConsistencySets.size() != 1) {
             fail(format("Expected only one set of consistent proposal responses but got %d",
                 proposalConsistencySets.size()));
           }

           out("Received %d transaction proposal responses. Successful+verified: %d . Failed: %d",
               transactionPropResp.size(), successful.size(), failed.size());
           if (failed.size() > 0) {
             ProposalResponse firstTransactionProposalResponse = failed.iterator().next();
             fail("Not enough endorsers for create():" + failed.size() + " endorser error: " +
                 firstTransactionProposalResponse.getMessage() + ". Was verified: " + firstTransactionProposalResponse.isVerified());
           }
           out("Successfully received transaction proposal responses.");

           ProposalResponse resp = transactionPropResp.iterator().next();
           assertThat(resp.getChaincodeActionResponseStatus()).isEqualTo(200);

           ////////////////////////////
           // Send Transaction Transaction to orderer
           out("Sending chaincode transaction(create) to orderer.");
           return channel.sendTransaction(successful).get(testConfig.getTransactionWaitTime(), TimeUnit.SECONDS);

         } catch (Exception e) {
           out("Caught an exception while invoking chaincode");
           e.printStackTrace();
           fail("Failed invoking chaincode with error : " + e.getMessage());
         }

         return null;
       }).thenApply(transactionEvent -> {

         try {

           waitOnFabric(0);

           out("Finished transaction with transaction id %s",
               transactionEvent.getTransactionID());
           testTxID = transactionEvent
               .getTransactionID(); // used in the channel queries later

           out("Query chaincode and see if the Terminator was added");
           QueryByChaincodeRequest queryByChaincodeRequest = client.newQueryProposalRequest();
           queryByChaincodeRequest.setArgs(new String[]{"query", "3aecedb1-a20b-4d38-bb77-db8444790f39"});
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
               String payload = proposalResponse.getProposalResponse().getResponse()
                                                .getPayload()
                                                .toStringUtf8();
               out("Query payload from peer %s returned %s",
                   proposalResponse.getPeer().getName(),
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
           BlockEvent.TransactionEvent te = ((TransactionEventException) e)
               .getTransactionEvent();
           if (te != null) {
             fail(format("Transaction with txid %s failed. %s", te.getTransactionID(),
                 e.getMessage()));
           }
         }
         fail(format("Test failed with %s exception %s", e.getClass().getName(),
             e.getMessage()));

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