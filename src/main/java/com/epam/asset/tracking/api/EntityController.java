package com.epam.asset.tracking.api;

import org.springframework.web.bind.annotation.RestController;

import com.epam.asset.tracking.domain.BusinessProvider;
import com.epam.asset.tracking.domain.User.Role;
import com.epam.asset.tracking.dto.EntityDTO;
import com.epam.asset.tracking.exception.DuplicateKeyExceptionWrapper;
import com.epam.asset.tracking.service.ApiService;
import com.epam.asset.tracking.service.BusinessProviderService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.validation.Valid;

import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/asset/tracking/entity")
public class EntityController {
	Logger logger = LoggerFactory.getLogger(EntityController.class);

	@Autowired
	ApiService api;

	@Autowired
	private MapperFacade mapper;

	@Autowired
	BusinessProviderService businessProviderService;

	@GetMapping(value = "/hello", produces = MediaType.TEXT_PLAIN_VALUE)
	@ApiOperation("Hello world")
	@ResponseStatus(HttpStatus.OK)
	public String hello() {
		return "Hello world";
	}

	@GetMapping(value = "/moveAtoB", produces = MediaType.TEXT_PLAIN_VALUE)
	@ApiOperation("Move 10 units from a to b")
	@ResponseStatus(HttpStatus.OK)
	public String move() {
		api.moveBalance("a", "b", "5");
		return "moved 10 units from a to b!";
	}

	@GetMapping(value = "/moveBtoA", produces = MediaType.TEXT_PLAIN_VALUE)
	@ApiOperation("Move 10 units from b to a")
	@ResponseStatus(HttpStatus.OK)
	public String move2() {
		api.moveBalance("b", "a", "5");
		return "moved 10 units from b to a!";
	}

	@GetMapping(value = "/{name}/balance", produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation("Getting the balance from the ledger, validated in all peers (configurable)")
	@ApiResponses({ @ApiResponse(code = 200, message = "The world State of Pojo", response = Pojo.class) })
	@ResponseStatus(HttpStatus.OK)
	public Pojo getBalance(
			@ApiParam(value = "The name of the holder of the account to retrieve the associated balance", required = true) @PathVariable String name) {
		logger.debug("Call to GET/{name}/balance");

		return new Pojo(name, api.getBalance(name));
	}

	@PostMapping(value = "/pojo", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation("Posting a Pojo as Json")
	@ResponseStatus(HttpStatus.OK)
	@ApiResponses({ @ApiResponse(code = 200, message = "Pojo was successfuly posted", response = Pojo.class) })
	public void postPojo(@ApiParam(value = "Posting a pojo.", required = true) @RequestBody Pojo pojo) {
		logger.debug("Call to POST/pojo");
		logger.debug("pojo in body request" + pojo.toString());

	}

	@PostMapping(value = "/blockWalker", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation("Walk through the blockchain")
	@ResponseStatus(HttpStatus.OK)
	@ApiResponses({ @ApiResponse(code = 200, message = "Pojo was successfuly posted", response = Pojo.class) })
	public void walker() {
		api.blockWalk();

	}

	@PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Posting a new, unique, business provider into DB", notes = "Current validations are:"
			+ "<br>"+ "Not allowed numbers or symbols (space is not allowed): <b>username</b>"
			+ "<br>"+ "Not allowed numbers or symbols (space is allowed): <b>name</b>, <b>lastname</b>, <b>city</b> & <b>state</b>"
			+ "<br>"+ "Not allowed symbols (space, comma and periods are allowed): <b>address</b>"
			+ "<br>"+ "<b>businessType</b>, one of(case insensitive): Computer Seller, Computer Repair, Car Seller, Car Mechanic, House Seller, House Broker"
			+ "<br>"+ "<b>rfc</b> rules: Alphanumeric, not special characters allowed"
			+ "<br>"+ "<b>zipcode</b> rules: Numeric only, lenght exactly 5 digits"
			+ "<br>"+ "<b>password</b> rules: Text, numbers & symbols allowed, lenght must be between 8 and 10 characters")
	@ResponseStatus(HttpStatus.CREATED)
	@ApiResponses({ @ApiResponse(code = 201, message = "Pojo was successfuly posted and insterted in DB."),
		@ApiResponse(code = 400, message = "Bad, request. Probably a validation error in the payload."),
		@ApiResponse(code = 409, message = "Username already taken. Please choose another.")})
	public void postEntity(
			@ApiParam(value = "Posting a new Bussiness Provider. <br><br>"
					+ "Username nust be unique, otherwize application will return a 409 status code. <br><br>"
					+ "Business Type is validated against allowed values (listed in implementation notes)"
					, required = true) @RequestBody @Valid EntityDTO entity) {
		logger.debug("Call to POST/entity");
		logger.info("payload in body request" + entity.toString());

		entity.setRole(Role.BUSINESS_PROVIDER.name());
		BusinessProvider bp = mapper.map(entity, BusinessProvider.class);
		
		try {
		businessProviderService.save(bp);
		}
		catch(DuplicateKeyException dke) {
			logger.info("Duplicated username {}, {}", bp.getUsername(), bp);
			throw new DuplicateKeyExceptionWrapper("Duplicated username: " + bp.getUsername(), dke);
			
		}

	}

}

class Pojo {

	private String balance;
	private String name;

	Pojo() {
	}

	Pojo(String name, String balance) {
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