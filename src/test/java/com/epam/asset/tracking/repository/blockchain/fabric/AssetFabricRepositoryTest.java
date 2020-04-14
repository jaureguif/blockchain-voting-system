package com.epam.asset.tracking.repository.blockchain.fabric;

import static java.time.ZonedDateTime.now;
import static java.util.Optional.empty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.Optional;
import java.util.UUID;

import com.epam.asset.tracking.Application;
import com.epam.asset.tracking.domain.Asset;
import com.epam.asset.tracking.domain.Event;
import org.assertj.core.groups.Tuple;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class AssetFabricRepositoryTest {

  private @Autowired AssetFabricRepository repository;

  @Test
  @Ignore("Because it's not guaranteed to be run after org.hyperledger.fabric.sdk.endtoend.End2endTest")
  public void testCrud() {
    Asset asset = newTestAsset();
    shouldFindNothingWhenAssetIsNotPresent(asset.getUuid());
    shouldPersistAsset(asset);
    shouldFindAssetWhenIsPersisted(asset);

    Event destroyEvent = new Event();
    destroyEvent.setSummary("Asset destruction");
    destroyEvent.setDescription("Serum was destroyed by Chris and Sheva");
    destroyEvent.setBusinessProviderId("Umbrella Corporation");
    destroyEvent.setDate(now());

    shouldPersistNewEvents(asset, destroyEvent);
    shouldDeleteAsset(asset);
    shouldFindNothingWhenAssetIsNotPresent(asset.getUuid());
  }

  private void shouldFindNothingWhenAssetIsNotPresent(UUID uuid) {
    assertThat(repository.findOne(uuid)).isNotNull().isEqualTo(empty());
  }

  private void shouldFindAssetWhenIsPersisted(Asset expectedAsset) {
    Optional<Asset> optionalAsset = repository.findOne(expectedAsset.getUuid());
    assertThat(optionalAsset).isNotNull().isNotEqualTo(empty());
    assertAsset(optionalAsset.get(), expectedAsset);
  }

  private void shouldPersistAsset(Asset expectedAsset) {
    Asset actualAsset = repository.save(expectedAsset);
    assertThat(actualAsset).isNotNull();
    assertAsset(actualAsset, expectedAsset);
  }

  private void shouldPersistNewEvents(Asset expectedAsset, Event newEvent) {
    Optional<Asset> optionalAsset = repository.addEvent(expectedAsset.getUuid(), newEvent);
    assertThat(optionalAsset).isNotNull().isNotEqualTo(empty());
    expectedAsset.getEvents().add(newEvent);
    assertAsset(optionalAsset.get(), expectedAsset);
  }

  private void shouldDeleteAsset(Asset expectedAsset) {
    assertThat(repository.delete(expectedAsset)).isTrue();
  }

  private void assertAsset(Asset actualAsset, Asset expectedAsset) {
    assertThat(actualAsset)
        .extracting(Asset::getUuid, Asset::getSerialNumber, Asset::getAssetType, Asset::getDescription, Asset::getOwnerName)
        .containsOnly(expectedAsset.getUuid(), expectedAsset.getSerialNumber(), expectedAsset.getAssetType(), expectedAsset.getDescription(), expectedAsset.getOwnerName());

    Tuple[] eventTuples = expectedAsset.getEvents().stream().map(this::eventToTuple).toArray(Tuple[]::new);
    assertThat(actualAsset.getEvents())
        .hasSameSizeAs(expectedAsset.getEvents())
        .extracting(Event::getSummary, Event::getDescription, Event::getBusinessProviderId, Event::getEncodedImage, Event::getAttachment)
        .containsOnly(eventTuples);
  }

  private Tuple eventToTuple(Event event) {
    return tuple(
        event.getSummary(),
        event.getDescription(),
        event.getBusinessProviderId(),
        Optional.ofNullable(event.getEncodedImage()).orElse("no-data"),
        Optional.ofNullable(event.getAttachment()).orElse("no-data")
        );
  }

  private Asset newTestAsset() {
    Asset asset = new Asset();
    asset.setUuid(UUID.randomUUID());
    asset.setSerialNumber("PG67A/W");
    asset.setAssetType("SERUM");
    asset.setDescription("Progenitor virus based serum");
    asset.setOwnerName("Albert Wesker");

    Event registerEvent = new Event();
    registerEvent.setSummary("Asset Registration");
    registerEvent.setDescription("Registration of asset");
    registerEvent.setBusinessProviderId("Umbrella Corporation");
    registerEvent.setDate(now());
    registerEvent.setEncodedImage("/9j/4AAQSkZJRgABAQEASABIAAD//gATQ3JlYXRlZCB3aXRoIEdJTVD/2wBDACgcHiMeGSgjISMtKygwPGRBPDc3PHtYXUlkkYCZlo+AjIqgtObDoKrarYqMyP/L2u71////m8H////6/+b9//j/2wBDASstLTw1PHZBQXb4pYyl+Pj4+Pj4+Pj4+Pj4+Pj4+Pj4+Pj4+Pj4+Pj4+Pj4+Pj4+Pj4+Pj4+Pj4+Pj4+Pj4+Pj/wgARCAAiABQDAREAAhEBAxEB/8QAGAAAAwEBAAAAAAAAAAAAAAAAAAIDBAH/xAAVAQEBAAAAAAAAAAAAAAAAAAAAAf/aAAwDAQACEAMQAAABxHRhDRLCxhiRcgAwoH//xAAdEAACAgEFAAAAAAAAAAAAAAABAgAQERIhIjEy/9oACAEBAAEFAoBmHak4r3FGoucmeUo3/8QAFBEBAAAAAAAAAAAAAAAAAAAAMP/aAAgBAwEBPwF//8QAFBEBAAAAAAAAAAAAAAAAAAAAMP/aAAgBAgEBPwF//8QAHBAAAQMFAAAAAAAAAAAAAAAAEAABERIgITFh/9oACAEBAAY/AhAqELGh17v/xAAbEAACAwADAAAAAAAAAAAAAAABEQAQMSFBcf/aAAgBAQABPyGEJDYZLoqQ5achLMxbA04cChtaTXl//9oADAMBAAIAAwAAABASDAAAAAf/xAAXEQADAQAAAAAAAAAAAAAAAAABIDBR/9oACAEDAQE/EEGz/8QAFhEAAwAAAAAAAAAAAAAAAAAAASAw/9oACAECAQE/EEM//8QAHRABAAICAgMAAAAAAAAAAAAAAQAREGEhMVFx4f/aAAgBAQABPxCAztSkuBr3vAAe0GvsdWtW2KJ09vglTAGgw7dI0ZbkVoVef//Z");
    asset.getEvents().add(registerEvent);

    return asset;
  }
}
