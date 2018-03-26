package com.epam.asset.tracking.repository.blockchain.fabric;

import java.util.Optional;
import java.util.UUID;

import com.epam.asset.tracking.domain.Asset;
import com.epam.asset.tracking.domain.Event;
import com.epam.asset.tracking.exception.BlockchainTransactionException;
import com.epam.asset.tracking.repository.AssetRepository;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AssetFabricRepository extends BaseFabricRepository<Asset, UUID> implements AssetRepository {

  private static final String NO_BINARY_DATA = "no-data";

  private static final class AssetChaincodeMethods {
    private static final String CREATE = "create";
    private static final String CREATE_EVENT = "addEvent";
    private static final String READ = "query";
    private static final String DELETE = "delete";
  }

  private @Autowired MapperFacade mapper;

  @Override
  public Optional<Asset> findOne(UUID assetId) {
    ProposalRequestArgs args = new ProposalRequestArgs.Builder()
        .chaincodeMethod(AssetChaincodeMethods.READ)
        .args(assetId.toString())
        .build();
    return queryBlockchain(args)
        .map(json -> mapper.map(json, Asset.class));
  }

  @Override
  public Asset save(Asset asset) {
    Event event = asset.getEvents().iterator().next();
    event.setEncodedImage(Optional.ofNullable(event.getEncodedImage()).orElse(NO_BINARY_DATA));
    event.setAttachment(Optional.ofNullable(event.getAttachment()).orElse(NO_BINARY_DATA));
    ProposalRequestArgs args = new ProposalRequestArgs.Builder()
        .chaincodeMethod(AssetChaincodeMethods.CREATE)
        .args(
            asset.getUuid().toString(),
            asset.getSerialNumber(),
            asset.getAssetType(),
            asset.getOwnerName(),
            asset.getDescription(),
            event.getBusinessProviderId(),
            event.getEncodedImage(),
            event.getAttachment(),
            event.getSummary(),
            event.getDescription()
        )
        .build();
    if (modifyBlockchain(args)) return asset;
    throw new BlockchainTransactionException("Unable to persist asset");
  }

  @Override
  public Optional<Asset> addEvent(UUID assetId, Event event) {
    return findOne(assetId)
      .filter(asset -> persistEvent(asset, event))
      .map(asset -> { asset.getEvents().add(event); return asset; });
  }

  @Override
  public boolean delete(Asset entity) {
    ProposalRequestArgs args = new ProposalRequestArgs.Builder()
        .chaincodeMethod(AssetChaincodeMethods.DELETE)
        .args(entity.getUuid().toString())
        .build();
    return modifyBlockchain(args);
  }

  private boolean persistEvent(Asset asset, Event event) {
    event.setEncodedImage(Optional.ofNullable(event.getEncodedImage()).orElse(NO_BINARY_DATA));
    event.setAttachment(Optional.ofNullable(event.getAttachment()).orElse(NO_BINARY_DATA));
    ProposalRequestArgs proposalRequestArgs = new ProposalRequestArgs.Builder()
        .chaincodeMethod(AssetChaincodeMethods.CREATE_EVENT)
        .args(
            asset.getUuid().toString(),
            event.getSummary(),
            event.getDescription(),
            event.getDate().toString(),
            event.getBusinessProviderId(),
            event.getEncodedImage(),
            event.getAttachment()
        )
        .build();
    return modifyBlockchain(proposalRequestArgs);
  }
}
