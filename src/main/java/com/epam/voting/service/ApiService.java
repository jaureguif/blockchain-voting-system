package com.epam.voting.service;

public interface ApiService {
	
	String getHello();
	
	String getBalance(String holderName);
	
	void moveBalance(String fromName, String toName, String amount);

	void blockWalk();
	
}
