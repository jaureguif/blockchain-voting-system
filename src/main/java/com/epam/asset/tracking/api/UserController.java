package com.epam.asset.tracking.api;

import com.epam.asset.tracking.dto.BusinessProviderDTO;
import com.epam.asset.tracking.dto.UserDTO;
import com.epam.asset.tracking.exception.InvalidUserException;
import com.epam.asset.tracking.service.UserService;
import io.swagger.annotations.*;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import com.epam.asset.tracking.domain.BusinessProvider;
import com.epam.asset.tracking.domain.User.Role;
import com.epam.asset.tracking.exception.DuplicateKeyExceptionWrapper;
import com.epam.asset.tracking.service.ApiService;
import com.epam.asset.tracking.service.BusinessProviderService;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/asset/tracking/users")
public class UserController {
	Logger logger = LoggerFactory.getLogger(UserController.class);

  @Autowired
  ApiService api;

  @Autowired
  private MapperFacade mapper;

  @Autowired
  BusinessProviderService businessProviderService;


    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Posting a new, unique, business provider into DB",
      notes = "Current validations are:" + "<br>"
          + "Not allowed numbers or symbols (space is not allowed): <b>username</b>" + "<br>"
          + "Not allowed numbers or symbols (space is allowed): <b>name</b>, <b>lastname</b>, <b>city</b> & <b>state</b>"
          + "<br>" + "Not allowed symbols (space, comma and periods are allowed): <b>address</b>"
          + "<br>"
          + "<b>businessType</b>, one of(case insensitive): Computer Seller, Computer Repair, Car Seller, Car Mechanic, House Seller, House Broker"
          + "<br>" + "<b>rfc</b> rules: Alphanumeric, not special characters allowed" + "<br>"
          + "<b>zipcode</b> rules: Numeric only, lenght exactly 5 digits" + "<br>"
          + "<b>password</b> rules: Text, numbers & symbols allowed, lenght must be between 8 and 10 characters")
  @ResponseStatus(HttpStatus.CREATED)
  @ApiResponses({
      @ApiResponse(code = 201, message = "Pojo was successfuly posted and insterted in DB."),
      @ApiResponse(code = 400,
          message = "Bad, request. Probably a validation error in the payload."),
      @ApiResponse(code = 409, message = "Username already taken. Please choose another.")})
  public void postUser(@ApiParam(
      value = "Posting a new Bussiness Provider. <br><br>"
          + "Username must be unique, otherwize application will return a 409 status code. <br><br>"
          + "Business Type is validated against allowed values (listed in implementation notes)",
      required = true) @RequestBody @Valid UserDTO user, HttpServletRequest req) {

    logger.debug("Call to POST/entity Request " + req);
    logger.debug("Call to POST/entity");
    logger.info("payload in body request" + user.toString());

    user.setRole(Role.BUSINESS_PROVIDER.name());
    BusinessProvider bp = mapper.map(user, BusinessProvider.class);

    try {
      businessProviderService.save(bp);
    } catch (DuplicateKeyException dke) {
      logger.info("Duplicated username {}, {}", bp.getUsername(), bp);
      throw new DuplicateKeyExceptionWrapper("Duplicated username: " + bp.getUsername(), dke);
    }
  }

	@RolesAllowed("ROLE_BUSINESS_PROVIDER")
	@GetMapping(value = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Getting a user data by username", authorizations = {@Authorization(value = "basicAuth")})
	@ApiResponses({ @ApiResponse(code = 200, message = "Returns the user data by username", response = BusinessProvider.class),
			@ApiResponse(code = 405, message = "Username provided doesn't match the currently logged username") })
	@ResponseStatus(HttpStatus.OK)
	public BusinessProviderDTO getUserData (
			@ApiParam(value = "The username of the asset that we want to retrieve", required = true) @PathVariable @Valid String username,
			HttpServletRequest req)  throws InvalidUserException {
		logger.debug("Call to GET:/asset/tracking/user/{username}");
        BusinessProviderDTO userData = null;

		if(req.getUserPrincipal().getName().equals(username)){
		    userData = mapper.map(businessProviderService.findUserbyUsername(req.getUserPrincipal().getName()).orElseThrow(() -> new UsernameNotFoundException("Invalid username or password.")), BusinessProviderDTO.class);
		} else {
			throw new InvalidUserException("Username provided doesn't match the one who is currently logged in");
		}



		return userData;
	}

    @GetMapping(value = "/password/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Sending a new random password to the user")
    @ApiResponses({ @ApiResponse(code = 200, message = "Send an email to the user with the new password", response = BusinessProvider.class),
            @ApiResponse(code = 405, message = "Invalid Username provided") })
    @ResponseStatus(HttpStatus.OK)
    public void sendUserPassword (
            @ApiParam(value = "Sends a random password to the user email, if the email is valid", required = true) @PathVariable String username,
            HttpServletRequest req)  throws InvalidUserException {
            logger.debug("Call to GET:/asset/tracking/users/password/{username}");

            BusinessProvider userData = businessProviderService.generateNewPassword(username);

            businessProviderService.sendEmail(userData);


    }



}
