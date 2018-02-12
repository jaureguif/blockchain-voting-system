package com.epam.asset.tracking.service;

import java.util.UUID;

import com.epam.asset.tracking.domain.Asset;
import com.epam.asset.tracking.exception.AssetNotFoundException;

public interface ApiService {

	Asset getAssetById(UUID id) throws AssetNotFoundException;

}
