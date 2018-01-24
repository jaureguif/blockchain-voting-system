package com.epam.asset.tracking;

import org.springframework.web.bind.annotation.RestController;

import com.epam.asset.tracking.dto.EntityDTO;
import com.epam.asset.tracking.service.ApiService;
import com.epam.asset.tracking.service.EntityService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.validation.Valid;

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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/asset/tracking/entity")
public class EntityController {
	Logger logger = LoggerFactory.getLogger(EntityController.class);
	
	@Autowired
	ApiService api;
	
	@Autowired
	EntityService entityService;
	
	@GetMapping(value ="/hello", produces=MediaType.TEXT_PLAIN_VALUE)
	@ApiOperation("Hello world")
	@ResponseStatus(HttpStatus.OK)
	public String hello() {
		return "Hello world";
	}
	
	@GetMapping(value ="/moveAtoB", produces=MediaType.TEXT_PLAIN_VALUE)
	@ApiOperation("Move 10 units from a to b")
	@ResponseStatus(HttpStatus.OK)
	public String move() {
		api.moveBalance("a", "b", "5");
		return "moved 10 units from a to b!";
	}
	
	@GetMapping(value ="/moveBtoA", produces=MediaType.TEXT_PLAIN_VALUE)
	@ApiOperation("Move 10 units from b to a")
	@ResponseStatus(HttpStatus.OK)
	public String move2() {
		api.moveBalance("b", "a", "5");
		return "moved 10 units from b to a!";
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
	
	
	@PostMapping(value="/", produces=MediaType.APPLICATION_JSON_VALUE, consumes=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation("Posting a new entity into DB")
	@ResponseStatus(HttpStatus.CREATED)
	@ApiResponses({ @ApiResponse(code = 200, message = "Pojo was successfuly posted", response = Pojo.class) })
	public void postEntity( 
			@ApiParam(value = "Posting a new Entity.", required = true)
			@RequestBody @Valid EntityDTO entity) {
		logger.info("Call to POST/pojo");
		logger.info("pojo in body request" + entity.toString());
		
		entityService.newEntity(entity);
		
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