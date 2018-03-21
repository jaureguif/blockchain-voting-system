package com.epam.asset.tracking.service;

import static java.lang.String.format;

import java.util.UUID;

import com.epam.asset.tracking.domain.Asset;
import com.epam.asset.tracking.domain.Event;
import com.epam.asset.tracking.exception.AssetNotFoundException;
import com.epam.asset.tracking.repository.AssetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApiServiceImpl implements ApiService {

  private Logger log = LoggerFactory.getLogger(ApiServiceImpl.class);

  private @Autowired AssetRepository assetRepository;

  @Override
  public Asset saveAsset(Asset asset) {
    return assetRepository.save(asset);
  }

  @Override
  public Asset getAssetById(UUID id) {
    return assetRepository
        .findOne(id)
        .orElseThrow(() -> new AssetNotFoundException(format("Asset not found with the id %s", id)));
  }

  @Override
  public Asset addEventToAsset(UUID assetId, Event event) {
    return assetRepository
        .addEvent(assetId, event)
        .orElseThrow(() -> new AssetNotFoundException(format("Asset not found with the id %s", assetId)));
  }
}
