package com.epam.asset.tracking.service;

import java.util.Optional;
import java.util.UUID;

import com.epam.asset.tracking.domain.Asset;
import com.epam.asset.tracking.domain.Event;

public interface ApiService {

  Asset getAssetById(UUID id);

  Asset addEventToAsset(UUID assetId, Event event);

  Asset saveAsset(Asset asset);
}
