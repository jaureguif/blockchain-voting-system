package com.epam.asset.tracking.service;

import static java.time.ZonedDateTime.now;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import com.epam.asset.tracking.domain.Asset;
import com.epam.asset.tracking.domain.Event;
import com.epam.asset.tracking.exception.AssetNotFoundException;
import com.epam.asset.tracking.repository.AssetRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApiServiceImplTest {

  private @Mock AssetRepository assetRepository;
  private @InjectMocks ApiServiceImpl service;

  @Test
  public void shouldSaveAssetReturnAssetWhenRepositoryReturnsAsset() {
    Asset expectedAsset = newTestAsset();
    when(assetRepository.save(expectedAsset)).thenReturn(expectedAsset);
    assertThat(service.saveAsset(expectedAsset)).isNotNull().isEqualTo(expectedAsset);
  }

  @Test
  public void shouldSaveAssetThrowExceptionWhenRepositoryThrowsException() {
    when(assetRepository.save(any(Asset.class))).thenThrow(new IllegalArgumentException());
    assertThatThrownBy(() -> service.saveAsset(null)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void shouldFindOneReturnAssetWhenRepositoryReturnsAsset() {
    Asset expectedAsset = newTestAsset();
    when(assetRepository.findOne(expectedAsset.getUuid())).thenReturn(Optional.of(expectedAsset));
    assertThat(service.getAssetById(expectedAsset.getUuid())).isNotNull().isEqualTo(expectedAsset);
  }

  @Test
  public void shouldFindOneThrowAssetNotFoundExceptionWhenRepositoryReturnsEmpty() {
    UUID uuid = UUID.randomUUID();
    when(assetRepository.findOne(uuid)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.getAssetById(uuid)).isInstanceOf(AssetNotFoundException.class).hasMessageStartingWith("Asset not found with the id");
  }

  @Test
  public void shouldAddEventToAssetReturnAssetWhenRepositoryReturnsAsset() {
    Asset expectedAsset = newTestAsset();
    when(assetRepository.addEvent(expectedAsset.getUuid(), expectedAsset.getEvents().iterator().next())).thenReturn(Optional.of(expectedAsset));
    assertThat(service.addEventToAsset(expectedAsset.getUuid(), expectedAsset.getEvents().iterator().next())).isNotNull().isEqualTo(expectedAsset);
  }

  @Test
  public void shouldAddEventToAssetThrowAssetNotFoundExceptionWhenRepositoryReturnsEmpty() {
    Asset asset = newTestAsset();
    when(assetRepository.addEvent(asset.getUuid(), asset.getEvents().iterator().next())).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.addEventToAsset(asset.getUuid(), asset.getEvents().iterator().next())).isInstanceOf(AssetNotFoundException.class).hasMessageStartingWith("Asset not found with the id");
  }

  private Asset newTestAsset() {
    Asset asset = new Asset();
    asset.setUuid(UUID.randomUUID());
    asset.setSerialNumber("PG67A/W");
    asset.setAssetType("SERUM");
    asset.setDescription("Progenitor virus based serum");
    asset.setOwnerName("Albert Wesker");
    asset.getEvents().add(newTestEvent());
    return asset;
  }

  private Event newTestEvent() {
    Event event = new Event();
    event.setSummary("Summary");
    event.setDescription("Description");
    event.setBusinessProviderId(UUID.randomUUID().toString());
    event.setDate(now());
    return event;
  }
}
