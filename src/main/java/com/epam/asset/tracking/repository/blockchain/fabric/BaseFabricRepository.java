package com.epam.asset.tracking.repository.blockchain.fabric;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.epam.asset.tracking.exception.BlockchainTransactionException;
import com.epam.asset.tracking.repository.BlockchainRepository;
import com.epam.asset.tracking.repository.blockchain.fabric.internal.TestConfig;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.SDKUtils;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseFabricRepository<T, ID> implements BlockchainRepository<T, ID> {

  private static final TestConfig testConfig = TestConfig.getConfig();
  private static final Logger log = LoggerFactory.getLogger(BaseFabricRepository.class);

  private HFClient client;
  private Channel channel;

  private @Autowired FabricInitializerHelper initializerHelper;

  public boolean delete(T entity) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  protected boolean modifyBlockchain(ProposalRequestArgs proposalRequestArgs) {
    init();
    Collection<ProposalResponse> successful = new LinkedList<>();
    Collection<ProposalResponse> failed = new LinkedList<>();
    TransactionProposalRequest transactionProposalRequest = new TransactionProposalRequestBuilder(client)
        .args(proposalRequestArgs.getArgs())
        .proposalWaitTime(testConfig.getProposalWaitTime())
        .buildTransactionProposalRequest();
    log.info("sending transactionProposal to all peers with arguments: addEvent()");
    Collection<ProposalResponse> transactionPropResp;
    try {
      transactionPropResp = channel.sendTransactionProposal(transactionProposalRequest, channel.getPeers());
    } catch (ProposalException e) {
      throw new BlockchainTransactionException("Error sending transaction proposal to ledger", e);
    } catch (InvalidArgumentException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
    for (ProposalResponse response : transactionPropResp) {
      if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
        log.info("Successful transaction proposal response Txid: {} from peer {}", response.getTransactionID(), response.getPeer().getName());
        successful.add(response);
      } else failed.add(response);
    }

    Collection<Set<ProposalResponse>> proposalConsistencySets;
    try {
      proposalConsistencySets = SDKUtils.getProposalConsistencySets(transactionPropResp);
    } catch (InvalidArgumentException e) {
      throw new IllegalArgumentException(e);
    }
    if (proposalConsistencySets.size() != 1) {
      log.error("Expected only one set of consistent proposal responses but got {}", proposalConsistencySets.size());
    }
    log.info("Received {} transaction proposal responses. Successful+verified: {}. Failed: {}", transactionPropResp.size(), successful.size(), failed.size());
    if (failed.size() > 0) {
      ProposalResponse firstTransactionProposalResponse = failed.iterator().next();
      log.error("Not enough endorsers for create(): {} endorser error: {}. Was verified: {}", failed.size(), firstTransactionProposalResponse.getMessage(), firstTransactionProposalResponse.isVerified());
    }
    log.info("Successfully received transaction proposal responses.");
    ProposalResponse resp = transactionPropResp.iterator().next();
    try {
      log.info("RESPONSE ACTION RESPONSE STATUS: " + resp.getChaincodeActionResponseStatus());
    } catch (InvalidArgumentException e) {
      // Looking at the source code, this exception will never e thrown... but who knows?
      log.error("!!!???", e);
      throw new IllegalArgumentException(e.getMessage());
    }
    log.info("Sending chaincode transaction(addEvent) to orderer.");

    CompletableFuture<TransactionEvent> future = channel.sendTransaction(successful);
    TransactionEvent txEvent;
    try {
      txEvent = future.get(testConfig.getTransactionWaitTime(), TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new BlockchainTransactionException("Interrupted transaction", e);
    } catch (ExecutionException e) {
      throw new BlockchainTransactionException("Transaction throw an exception but complete", e);
    } catch (TimeoutException e) {
      throw new BlockchainTransactionException("Wait too much to finish transaction", e);
    }
    log.warn("Chaincode transaction(addEvent) completed with transaction ID: {}", txEvent.getTransactionID());

    boolean result = successful.size() == channel.getPeers().size();
    channel.shutdown(true);
    return result;
  }

  protected Optional<String> queryBlockchain(ProposalRequestArgs proposalRequestArgs) {
    init();
    String payload = null;
    QueryByChaincodeRequest queryByChaincodeRequest = new TransactionProposalRequestBuilder(client)
        .args(proposalRequestArgs.getArgs())
        .buildQueryByChaincodeRequest();
    Collection<ProposalResponse> queryProposals;
    try {
      queryProposals = channel.queryByChaincode(queryByChaincodeRequest, channel.getPeers());
    } catch (InvalidArgumentException e) {
      throw new IllegalArgumentException(e.getMessage());
    } catch (ProposalException e) {
      throw new BlockchainTransactionException("Unable to create query proposal", e);
    }
    for (ProposalResponse proposalResponse : queryProposals) {
      if (!proposalResponse.isVerified() || proposalResponse.getStatus() != ProposalResponse.Status.SUCCESS) {
        log.warn("Failed query proposal from peer {} status: {}. Messages: {}. Was verified: {}", proposalResponse.getPeer().getName(), proposalResponse.getStatus(), proposalResponse.getMessage(), proposalResponse.isVerified());
      } else {
        payload = proposalResponse.getProposalResponse().getResponse().getPayload().toStringUtf8();
        log.info("Query payload from peer {} returned: {}", proposalResponse.getPeer().getName(), payload);
        if (payload != null && payload.trim().isEmpty()) payload = null;
      }
    }

    channel.shutdown(true);
    return Optional.ofNullable(payload);
  }

  private void init() {
    initializerHelper.init();
    client = initializerHelper.getClient();
    channel = initializerHelper.getChannel();
  }
}
