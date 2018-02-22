package com.epam.asset.tracking.api;

import com.epam.asset.tracking.domain.Asset;
import com.epam.asset.tracking.dto.AssetDTO;
import com.epam.asset.tracking.exception.AssetNotFoundException;
import com.epam.asset.tracking.service.ApiService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.UUID;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/asset/tracking/asset")
public class AssetController {

	private static final Logger logger = LoggerFactory.getLogger(AssetController.class);

	@Autowired
	ApiService api;

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation("Getting an unique Asset by its Id")
	@ApiResponses({@ApiResponse(code = 200, message = "Returns an Asset by its Id", response = Asset.class),
		@ApiResponse(code = 400, message = "Bad, request. Most-likely the id is not a valid UUID string"),
		@ApiResponse(code = 404, message = "Asset with given UUID was not found")})
	@ResponseStatus(HttpStatus.OK)
	public Asset getAssetById(
			@ApiParam(value = "The id of the asset that we want to retrieve", required = true) @PathVariable @Valid UUID id)
			throws AssetNotFoundException {
		logger.debug("Call to GET:/asset/tracking/asset/{id}");

		return api.getAssetById(id);
	}
	
	@ApiOperation("Post an Asset")
	@ApiResponses({@ApiResponse(code = 201, message = "Returns saved Asset", response = Asset.class),
		@ApiResponse(code = 400, message = "Bad, request.")})

	@PostMapping(consumes= {"multipart/form-data"} )
	public AssetDTO postAsset(@RequestPart("file") MultipartFile file ) {
		logger.debug("Call to POST:/asset/tracking/asset");
		
		return new AssetDTO();
	}
		
	
	

}
