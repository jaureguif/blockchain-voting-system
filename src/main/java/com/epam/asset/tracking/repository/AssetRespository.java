package com.epam.asset.tracking.repository;

import java.util.Optional;
import java.util.UUID;

import com.epam.asset.tracking.domain.Asset;
import com.epam.asset.tracking.domain.Event;

public interface AssetRespository extends BlockchainRepository<Asset, UUID> {
  Optional<Asset> addEvent(UUID assetId, Event event);
}
