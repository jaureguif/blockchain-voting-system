package com.epam.voting;

import org.springframework.web.bind.annotation.RestController;

import com.epam.voting.service.ApiService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/blockchain/account")
public class HelloController {
	Logger logger = LoggerFactory.getLogger(HelloController.class);
	
	@Autowired
	ApiService api;
	
	@GetMapping(value ="/move", produces=MediaType.TEXT_PLAIN_VALUE)
	@ApiOperation("Move 1 from a to b")
	@ApiResponses({ @ApiResponse(code = 200, message = "ABC") })
	@ResponseStatus(HttpStatus.OK)
	public String move() {
		api.moveBalance("b", "a", "10");
		return "moved!";
	}
	
	@GetMapping(value="/{name}/balance", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation("Getting the balance from the ledger, validated in all peers (configurable)")
	@ApiResponses({ @ApiResponse(code = 200, message = "The world State of Pojo", response = Pojo.class) })
	@ResponseStatus(HttpStatus.OK)
	public Pojo getBalance(@ApiParam(value = "The name of the holder of the account to retrieve the associated balance", required = true) @PathVariable String name) {
		logger.debug("Call to GET/{name}/balance");
		
		return new Pojo(name, api.getBalance(name));
	}
	
	@PostMapping(value="/pojo", produces=MediaType.APPLICATION_JSON_VALUE, consumes=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation("Posting a Pojo as Json")
	@ResponseStatus(HttpStatus.OK)
	@ApiResponses({ @ApiResponse(code = 200, message = "Pojo was successfuly posted", response = Pojo.class) })
	public void postPojo( 
			@ApiParam(value = "Posting a pojo.", required = true)
			@RequestBody Pojo pojo) {
		logger.debug("Call to POST/pojo");
		logger.debug("pojo in body request" + pojo.toString());
		
		
	}
	
	@PostMapping(value="/blockWalker", produces=MediaType.APPLICATION_JSON_VALUE, consumes=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation("Walk through the blockchain")
	@ResponseStatus(HttpStatus.OK)
	@ApiResponses({ @ApiResponse(code = 200, message = "Pojo was successfuly posted", response = Pojo.class) })
	public void walker() {
		api.blockWalk();
		
	}

}


class Pojo{
	
	private String balance;
	private String name;
	
	Pojo(){}
	
	Pojo(String name, String balance){
		this.name = name;
		this.setBalance(balance);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getBalance() {
		return balance;
	}

	public void setBalance(String balance) {
		this.balance = balance;
	}
	
	
}