package com.epam.asset.tracking.api;

import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.epam.asset.tracking.domain.Asset;
import com.epam.asset.tracking.exception.AssetNotFoundException;
import com.epam.asset.tracking.service.ApiService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/asset/tracking/asset")
public class AssetController {

	private static final Logger logger = LoggerFactory.getLogger(AssetController.class);

	@Autowired
	ApiService api;

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation("Getting a unique Asset by its Id")
	@ApiResponses({ @ApiResponse(code = 200, message = "Returns an Asset by its Id", response = Asset.class),
			@ApiResponse(code = 400, message = "Bad, request. Most-likely the id is not a valid UUID string"),
			@ApiResponse(code = 404, message = "Asset with given UUID was not found") })
	@ResponseStatus(HttpStatus.OK)
	public Asset getAssetById(
			@ApiParam(value = "The id of the asset that we want to retrieve", required = true) @PathVariable @Valid UUID id,
			HttpServletRequest req) throws AssetNotFoundException {
		logger.debug("Call to GET:/asset/tracking/asset/{id}");

		return api.getAssetById(id);
	}

	@RolesAllowed("ROLE_BUSINESS_PROVIDER")
	@PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
	public Asset postAsset() {
		throw new NotImplementedException();
	}

}
