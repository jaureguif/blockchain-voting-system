package com.epam.asset.tracking.repository;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.epam.asset.tracking.domain.Asset;
import com.epam.asset.tracking.domain.Event;

public interface AssetRepository extends BlockchainRepository<Asset, UUID> {
  @Nonnull Optional<Asset> addEvent(@Nonnull UUID assetId, @Nonnull Event event);
}
