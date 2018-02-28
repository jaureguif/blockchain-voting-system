package com.epam.asset.tracking.api;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.epam.asset.tracking.domain.Asset;
import com.epam.asset.tracking.domain.Event;
import com.epam.asset.tracking.dto.AssetDTO;
import com.epam.asset.tracking.exception.AssetNotFoundException;
import com.epam.asset.tracking.repository.BusinessProviderRepository;
import com.epam.asset.tracking.service.ApiService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import ma.glasnost.orika.MapperFacade;

@RestController
@RequestMapping("/asset/tracking/asset")
public class AssetController {

  private static final Logger logger = LoggerFactory.getLogger(AssetController.class);

  @Autowired
  ApiService api;

  @Autowired
  private MapperFacade mapper;

  @Autowired
  private BusinessProviderRepository businesProviderRepository;

  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Getting an unique Asset by its Id")
  @ApiResponses({
      @ApiResponse(code = 200, message = "Returns an Asset by its Id", response = Asset.class),
      @ApiResponse(code = 400,
          message = "Bad, request. Most-likely the id is not a valid UUID string"),
      @ApiResponse(code = 404, message = "Asset with given UUID was not found")})
  @ResponseStatus(HttpStatus.OK)
  public Asset getAssetById(@ApiParam(value = "The id of the asset that we want to retrieve",
      required = true) @PathVariable @Valid UUID id) throws AssetNotFoundException {
    logger.debug("Call to GET:/asset/tracking/asset/{id}");

    return api.getAssetById(id);
  }

  @Autowired
  private Validator validator;

  @ApiOperation(value = "Post an Asset", authorizations = {@Authorization(value = "basicAuth")})
  @ApiResponses({@ApiResponse(code = 201, message = "Returns saved Asset", response = Asset.class),
      @ApiResponse(code = 400, message = "Bad, request.")})

  @PostMapping(value = "/", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Secured(value = {"ROLE_BUSINESS_PROVIDER"})
  @RolesAllowed("ROLE_BUSINESS_PROVIDER")
  public Asset postAsset(@RequestPart("file") MultipartFile file, AssetDTO dto,
      HttpServletRequest request) throws AssetNotFoundException, NoSuchMethodException,
      SecurityException, MethodArgumentNotValidException {
    logger.debug("Call to POST:/asset/tracking/asset");

    String businessProviderName = businesProviderRepository
        .findByUsername(request.getUserPrincipal().getName()).get().getName();


    this.validate(dto);

    Asset asset = new Asset();
    asset.setUuid(UUID.randomUUID());
    asset.setAssetType(dto.getAssetType());
    asset.setDescription(dto.getDescription());
    asset.setOwnerName(dto.getOwnerName());
    asset.setSerialNumber(dto.getSerialNumber());

    Event registerEvent = new Event();
    registerEvent.setSummary("Asset Registration");
    registerEvent.setBusinessProviderId(request.getUserPrincipal().getName());
    registerEvent.setDate(ZonedDateTime.now());
    registerEvent.setDescription(String.format(
        "Registration of an asset of type %s     " + "On date: %s     " + "Serial number: %s     "
            + "Owner name: %s     " + "Business provider id: %s     "
            + "Business provider name: %s     " + "Attached image name: %s",
        asset.getAssetType(), registerEvent.getDate().format(DateTimeFormatter.RFC_1123_DATE_TIME),
        asset.getSerialNumber(), asset.getOwnerName(), registerEvent.getBusinessProviderId(),
        businessProviderName, file.getOriginalFilename()));
    registerEvent.setEncodedImage(mapper.map(file, String.class));
    asset.getEvents().add(registerEvent);

    // Save asset to blockchain
    api.saveAsset(asset);

    return api.getAssetById(asset.getUuid());
  }

  private void validate(AssetDTO asset)
      throws NoSuchMethodException, SecurityException, MethodArgumentNotValidException {

    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(asset, "asset");
    validator.validate(asset, errors);
    if (errors.hasErrors()) {
      throw new MethodArgumentNotValidException(
          new MethodParameter(this.getClass().getDeclaredMethod("postAsset", MultipartFile.class,
              AssetDTO.class, HttpServletRequest.class), 0),
          errors);
    }
  }

}
