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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/spanish-greetings")
public class HelloController {
	Logger logger = LoggerFactory.getLogger(HelloController.class);
	
	@Autowired
	ApiService api;
	
	static int id = 0;
	static String name = "A";

	@GetMapping(value ="/random", produces=MediaType.TEXT_PLAIN_VALUE)
	@ApiOperation("Greeting the Blockchain world")
	@ApiResponses({ @ApiResponse(code = 200, message = "ABC") })
	@ResponseStatus(HttpStatus.OK)
	public String index() {
		logger.error("Greeting at {}", java.time.Instant.now());
		return api.getHello();
	}
	
	
	@GetMapping(value="/pojo", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation("Greeting the Blockchain world with a pojo")
	@ApiResponses({ @ApiResponse(code = 200, message = "The world State of Pojo", response = Pojo.class) })
	@ResponseStatus(HttpStatus.OK)
	public Pojo pojo() {
		logger.debug("Call to GET/pojo");
		logger.error("getting a pojo{}", java.time.Instant.now());
		logger.error("getting a pojo{}", java.time.Instant.now());
		logger.error("getting a pojo{}", java.time.Instant.now());
		logger.error("getting a pojo{}", java.time.Instant.now());
		char c = name.charAt(name.length()-1);
		logger.debug(String.valueOf(c));
		name = name.concat(String.valueOf(++c));
		logger.debug(String.valueOf(c));
		logger.debug(name);
		
		return new Pojo(++id, name);
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
		
		name = pojo.getName();
		
	}

}


class Pojo{
	
	Pojo(){}
	
	Pojo(int id, String name){
		this.id = id;
		this.name = name;
	}
	
	int id;
	String name;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}