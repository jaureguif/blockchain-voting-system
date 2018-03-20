package com.epam.asset.tracking.repository.blockchain.fabric;

import java.util.Optional;
import java.util.UUID;

import com.epam.asset.tracking.domain.Asset;
import com.epam.asset.tracking.domain.Event;
import com.epam.asset.tracking.repository.AssetRespository;

public class AssetFabricRespository implements AssetRespository {

  @Override
  public Optional<Asset> addEvent(UUID assetId, Event event) {
    return Optional.empty();
  }

  @Override
  public Optional<Asset> findOne(UUID uuid) {
    return Optional.empty();
  }

  @Override
  public Asset save(Asset entity) {
    return null;
  }

  @Override
  public boolean delete(Asset entity) {
    return false;
  }
}
