package com.epam.asset.tracking.service;

import static java.lang.String.format;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.epam.asset.tracking.domain.Asset;
import com.epam.asset.tracking.domain.Event;
import com.epam.asset.tracking.exception.AssetNotFoundException;
import com.epam.asset.tracking.repository.AssetRepository;

@Service
public class ApiServiceImpl implements ApiService {

  private Logger log = LoggerFactory.getLogger(ApiServiceImpl.class);

  private @Autowired AssetRepository assetRepository;

  @Override
  public Asset saveAsset(Asset asset) {
    return assetRepository.save(asset);
  }

  @Override
  @Cacheable(value="assets", key="#id")
  public Asset getAssetById(UUID id) {
    return assetRepository
        .findOne(id)
        .orElseThrow(() -> new AssetNotFoundException(format("Asset not found with the id %s", id)));
  }

  @Override
  @CacheEvict(value="assets", key="#assetId")
  public Asset addEventToAsset(UUID assetId, Event event) {
    return assetRepository
        .addEvent(assetId, event)
        .orElseThrow(() -> new AssetNotFoundException(format("Asset not found with the id %s", assetId)));
  }

  public void setAssetRepository(AssetRepository assetRepository) {
    this.assetRepository = assetRepository;
  }
}
