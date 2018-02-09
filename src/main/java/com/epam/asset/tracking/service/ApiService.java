package com.epam.asset.tracking.service;

import com.epam.asset.tracking.domain.Asset;

public interface ApiService {
	
	String getHello();
	
	String getBalance(String holderName);
	
	void moveBalance(String fromName, String toName, String amount);

	void blockWalk();

	Asset getAssetById(String id);

}
