package com.epam.asset.tracking.repository.blockchain.fabric;

import java.util.Optional;
import java.util.UUID;

import com.epam.asset.tracking.domain.Asset;
import com.epam.asset.tracking.domain.Event;
import com.epam.asset.tracking.repository.AssetRepository;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AssetFabricRepository extends BaseFabricRepository<Asset, UUID> implements AssetRepository {

  private static final String NO_BINARY_DATA = "no-data";

  private static final class AssetChaincodeMethods {
    private static final String QUERY = "query";
    private static final String SAVE = "create";
    private static final String SAVE_EVENT = "addEvent";
  }

  private @Autowired MapperFacade mapper;

  @Override
  public Optional<Asset> findOne(UUID uuid) {
    ProposalRequestArgs args = new ProposalRequestArgs.Builder()
        .chaincodeMethod(AssetChaincodeMethods.QUERY)
        .args(uuid.toString())
        .build();
    return queryBlockchain(args)
        .map(json -> mapper.map(json, Asset.class));
  }

  @Override
  public Asset save(Asset asset) {
    Event event = asset.getEvents().iterator().next();
    ProposalRequestArgs args = new ProposalRequestArgs.Builder()
        .chaincodeMethod(AssetChaincodeMethods.SAVE)
        .args(
            asset.getUuid().toString(),
            asset.getSerialNumber(),
            asset.getAssetType(),
            asset.getOwnerName(),
            asset.getDescription(),
            event.getBusinessProviderId(),
            Optional.ofNullable(event.getEncodedImage()).orElse(NO_BINARY_DATA),
            Optional.ofNullable(event.getAttachment()).orElse(NO_BINARY_DATA),
            event.getSummary(),
            event.getDescription()
        )
        .build();
    modifyBlockchain(args);
    return asset;
  }

  @Override
  public Optional<Asset> addEvent(UUID assetId, Event event) {
    return findOne(assetId)
      .filter(asset -> persistEvent(asset, event))
      .map(asset -> { asset.getEvents().add(event); return asset; });
  }

  private boolean persistEvent(Asset asset, Event event) {
    ProposalRequestArgs proposalRequestArgs = new ProposalRequestArgs.Builder()
        .chaincodeMethod(AssetChaincodeMethods.SAVE_EVENT)
        .args(
            asset.getUuid().toString(),
            event.getSummary(),
            event.getDescription(),
            event.getDate().toString(),
            event.getBusinessProviderId(),
            Optional.ofNullable(event.getEncodedImage()).orElse(NO_BINARY_DATA),
            Optional.ofNullable(event.getAttachment()).orElse(NO_BINARY_DATA)
        )
        .build();
    return modifyBlockchain(proposalRequestArgs);
  }
}
