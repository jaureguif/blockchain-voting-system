package com.epam.asset.tracking.api;

import com.epam.asset.tracking.domain.Asset;
import com.epam.asset.tracking.service.ApiService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/asset/tracking/asset")
public class AssetController {

    Logger logger = LoggerFactory.getLogger(AssetController.class);

    @Autowired
    ApiService api;

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Getting a unique Asset by its Id")
    @ApiResponses({ @ApiResponse(code = 200, message = "Returns an Asset by its Id", response = Asset.class) })
    @ResponseStatus(HttpStatus.OK)
    public Asset getAssetById(
            @ApiParam(value = "The id of the asset that we want to retrieve", required = true) @PathVariable String id) {
        logger.debug("Call to GET/{id}");

        return api.getAssetById(id);
    }

}
