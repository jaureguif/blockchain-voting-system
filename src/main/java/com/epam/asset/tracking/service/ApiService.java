package com.epam.asset.tracking.service;

import java.util.Optional;
import java.util.UUID;
import com.epam.asset.tracking.domain.Asset;
import com.epam.asset.tracking.domain.Event;
import com.epam.asset.tracking.exception.AssetNotFoundException;

public interface ApiService {

  Asset getAssetById(UUID id) throws AssetNotFoundException;

  Optional<Asset> addEventToAsset(UUID assetId, Event event);

  String saveAsset(Asset asset);

}
