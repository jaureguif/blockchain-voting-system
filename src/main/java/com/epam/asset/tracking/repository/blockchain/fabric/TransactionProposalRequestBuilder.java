package com.epam.asset.tracking.repository.blockchain.fabric;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import static org.apache.commons.lang.ArrayUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.HashMap;
import java.util.Map;

import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.TransactionRequest;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;

public class TransactionProposalRequestBuilder {

  private final HFClient hfClient;
  private ChaincodeID chaincodeId;
  private Long proposalWaitTime;
  private String fnc;
  private String[] args;

  public TransactionProposalRequestBuilder(HFClient hfClient) {
    this.hfClient = requireNonNull(hfClient);
    chaincodeId = Defaults.CHAINCODE_ID;
    fnc = Defaults.FCN;
    args = Defaults.ARGS;
  }

  public TransactionProposalRequestBuilder args(String[] args) {
    this.args = isEmpty(args) ? Defaults.ARGS : args;
    return this;
  }

  public TransactionProposalRequestBuilder proposalWaitTime(Long proposalWaitTime) {
    this.proposalWaitTime = proposalWaitTime == null ? Defaults.PROPOSAL_WAIT_TIME : proposalWaitTime;
    return this;
  }

  public TransactionProposalRequestBuilder chaincodeId(ChaincodeID chaincodeId) {
    this.chaincodeId = chaincodeId == null ? Defaults.CHAINCODE_ID : chaincodeId;
    return this;
  }

  public TransactionProposalRequestBuilder fnc(String fnc) {
    this.fnc = isBlank(fnc) ? Defaults.FCN : fnc;
    return this;
  }

  public QueryByChaincodeRequest buildQueryByChaincodeRequest() {
    QueryByChaincodeRequest queryByChaincodeRequest = hfClient.newQueryProposalRequest();
    initTransactionProposalRequest(queryByChaincodeRequest);
    try {
      queryByChaincodeRequest.setTransientMap(TransientMaps.mapForQueries());
    } catch (InvalidArgumentException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
    return queryByChaincodeRequest;
  }

  public TransactionProposalRequest buildTransactionProposalRequest() {
    TransactionProposalRequest transactionProposalRequest = hfClient.newTransactionProposalRequest();
    initTransactionProposalRequest(transactionProposalRequest);
    try {
      transactionProposalRequest.setTransientMap(TransientMaps.mapForTransactions());
    } catch (InvalidArgumentException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
    return transactionProposalRequest;
  }

  private void initTransactionProposalRequest(TransactionRequest transactionRequest) {
    transactionRequest.setFcn(fnc);
    transactionRequest.setChaincodeID(chaincodeId);
    if (proposalWaitTime != null) transactionRequest.setProposalWaitTime(proposalWaitTime);
    transactionRequest.setArgs(args);
  }

  public static final class Defaults {
    public static final String FCN = "invoke";
    public static final String[] ARGS = new String[0];
    public static final ChaincodeID CHAINCODE_ID;
    public static final long PROPOSAL_WAIT_TIME = 20000L;

    static {
      CHAINCODE_ID = ChaincodeID.newBuilder()
                                .setName("asset_t_smart_contract_go")
                                .setVersion("1")
                                .setPath("com.epam.blockchain.chaincode/asset_t_smart_contract")
                                .build();
    }
  }

  private static final class TransientMaps {
    private static final HashMap<String, byte[]> QUERY_MAP_PROTOTYPE;
    private static final HashMap<String, byte[]> TRANSACTION_MAP_PROTOTYPE;

    static {
      QUERY_MAP_PROTOTYPE = new HashMap<>();
      QUERY_MAP_PROTOTYPE.put("HyperLedgerFabric", "QueryByChaincodeRequest:JavaSDK".getBytes(UTF_8));
      QUERY_MAP_PROTOTYPE.put("method", "QueryByChaincodeRequest".getBytes(UTF_8));

      TRANSACTION_MAP_PROTOTYPE = new HashMap<>();
      TRANSACTION_MAP_PROTOTYPE.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
      TRANSACTION_MAP_PROTOTYPE.put("method", "TransactionProposalRequest".getBytes(UTF_8));
      TRANSACTION_MAP_PROTOTYPE.put("event", "!".getBytes(UTF_8));
    }

    private static Map<String, byte[]> mapForQueries() {
      return (Map<String, byte[]>) QUERY_MAP_PROTOTYPE.clone();
    }

    private static Map<String, byte[]> mapForTransactions() {
      return (Map<String, byte[]>) TRANSACTION_MAP_PROTOTYPE.clone();
    }
  }
}