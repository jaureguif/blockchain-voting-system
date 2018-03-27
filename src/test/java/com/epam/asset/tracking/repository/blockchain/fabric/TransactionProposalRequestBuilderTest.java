package com.epam.asset.tracking.repository.blockchain.fabric;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.AbstractMap.SimpleEntry;

import com.epam.asset.tracking.repository.blockchain.fabric.TransactionProposalRequestBuilder.Defaults;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.TransactionRequest;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class TransactionProposalRequestBuilderTest {

  private static final HFClient CLIENT = HFClient.createNewInstance();

  @Test
  @Parameters(method = "buildArgs")
  public void shouldBuildQueryByChaincodeRequestWithValues(Long proposalWaitTime, String fcn, String[] args, ChaincodeID chaincodeID) {
    QueryByChaincodeRequest request = new TransactionProposalRequestBuilder(CLIENT)
          .proposalWaitTime(proposalWaitTime)
          .fnc(fcn)
          .args(args)
          .chaincodeId(chaincodeID)
          .buildQueryByChaincodeRequest();

    assertTransactionRequest(request, proposalWaitTime, fcn, args, chaincodeID);
    assertThat(request.getTransientMap()).isNotNull().containsOnly(
      new SimpleEntry<>("HyperLedgerFabric", "QueryByChaincodeRequest:JavaSDK".getBytes(UTF_8)),
      new SimpleEntry<>("method", "QueryByChaincodeRequest".getBytes(UTF_8))
    );
  }

  @Test
  @Parameters(method = "buildArgs")
  public void shouldBuildTransactionProposalRequestWithValues(Long proposalWaitTime, String fcn, String[] args, ChaincodeID chaincodeID) {
    TransactionProposalRequest request = new TransactionProposalRequestBuilder(CLIENT)
        .proposalWaitTime(proposalWaitTime)
        .fnc(fcn)
        .args(args)
        .chaincodeId(chaincodeID)
        .buildTransactionProposalRequest();

    assertTransactionRequest(request, proposalWaitTime, fcn, args, chaincodeID);
    assertThat(request.getTransientMap()).isNotNull().containsOnly(
        new SimpleEntry<>("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8)),
        new SimpleEntry<>("method", "TransactionProposalRequest".getBytes(UTF_8)),
        new SimpleEntry<>("event", "!".getBytes(UTF_8))
    );
  }

  private void assertTransactionRequest(TransactionRequest request, Long proposalWaitTime, String fcn, String[] args, ChaincodeID chaincodeID) {
    assertThat(request).isNotNull();
    assertThat(request.getArgs()).isNotNull();
    if (args == null) assertThat(request.getArgs()).isEmpty();
    else assertThat(request.getArgs()).hasSameSizeAs(args).containsExactly(args);

    assertThat(request.getChaincodeID()).isNotNull();
    if (chaincodeID == null) assertThat(request.getChaincodeID()).isEqualTo(Defaults.CHAINCODE_ID);
    else assertThat(request.getChaincodeID()).isEqualTo(chaincodeID);

    if (proposalWaitTime == null) assertThat(request.getProposalWaitTime()).isEqualTo(Defaults.PROPOSAL_WAIT_TIME);
    else assertThat(request.getProposalWaitTime()).isEqualTo(proposalWaitTime);

    assertThat(request.getFcn()).isNotBlank();
    if (fcn == null) assertThat(request.getFcn()).isEqualTo(Defaults.FCN);
    else assertThat(request.getFcn()).isEqualTo(fcn);
  }

  public Object[] buildArgs() {
    return new Object[][] {
        { 5000L, "fcn0", new String[]{"arg0", "arg1"}, ChaincodeID.newBuilder().setName("some_chaincode").setVersion("0.1").setPath("com.example.chaincode/some_chaincode").build() },
        { 5000L, "fcn0", new String[]{"arg0", "arg1"}, null },
        { 5000L, "fcn0", null, ChaincodeID.newBuilder().setName("some_chaincode").setVersion("0.1").setPath("com.example.chaincode/some_chaincode").build() },
        { 5000L, "fcn0", null, null },
        { 5000L, null, new String[]{"arg0", "arg1"}, ChaincodeID.newBuilder().setName("some_chaincode").setVersion("0.1").setPath("com.example.chaincode/some_chaincode").build() },
        { 5000L, null, new String[]{"arg0", "arg1"}, null },
        { 5000L, null, null, ChaincodeID.newBuilder().setName("some_chaincode").setVersion("0.1").setPath("com.example.chaincode/some_chaincode").build() },
        { 5000L, null, null, null },
        { null, "fcn0", new String[]{"arg0", "arg1"}, ChaincodeID.newBuilder().setName("some_chaincode").setVersion("0.1").setPath("com.example.chaincode/some_chaincode").build() },
        { null, "fcn0", new String[]{"arg0", "arg1"}, null },
        { null, "fcn0", null, ChaincodeID.newBuilder().setName("some_chaincode").setVersion("0.1").setPath("com.example.chaincode/some_chaincode").build() },
        { null, "fcn0", null, null },
        { null, null, new String[]{"arg0", "arg1"}, ChaincodeID.newBuilder().setName("some_chaincode").setVersion("0.1").setPath("com.example.chaincode/some_chaincode").build() },
        { null, null, new String[]{"arg0", "arg1"}, null },
        { null, null, null, ChaincodeID.newBuilder().setName("some_chaincode").setVersion("0.1").setPath("com.example.chaincode/some_chaincode").build() },
        { null, null, null, null },
    };
  }
}
