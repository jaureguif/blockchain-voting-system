package com.epam.asset.tracking.repository.blockchain.fabric.internal;

import java.lang.reflect.Field;

import org.hyperledger.fabric.sdk.helper.Config;

public class TestConfigHelper {

  public static final String CONFIG_OVERRIDES = "FABRICSDKOVERRIDES";

  /**
   * clearConfig "resets" Config so that the Config testcases can run without interference from
   * other test suites. Depending on what order JUnit decides to run the tests, Config could have
   * been instantiated earlier and could contain values that make the tests here fail.
   *
   * @throws SecurityException
   * @throws NoSuchFieldException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   *
   */
  public void clearConfig()  {
    Config config = Config.getConfig();
    Field configInstance = null;
    try {
      configInstance = config.getClass().getDeclaredField("config");
      configInstance.setAccessible(true);
      configInstance.set(null, null);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException("Cannot find 'config' field in Config class", e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Cannot delete 'config' field in Config class", e);
    }
  }

  /**
   * customizeConfig() sets up the properties listed by env var CONFIG_OVERRIDES The value of the
   * env var is <i>property1=value1,property2=value2</i> and so on where each <i>property</i> is a
   * property from the SDK's config file.
   *
   * @throws SecurityException
   * @throws IllegalArgumentException
   */
  public void customizeConfig() throws SecurityException, IllegalArgumentException {
    String fabricSdkConfig = System.getenv(CONFIG_OVERRIDES);
    if (fabricSdkConfig != null && fabricSdkConfig.length() > 0) {
      String[] configs = fabricSdkConfig.split(",");
      String[] configKeyValue;
      for (String config : configs) {
        configKeyValue = config.split("=");
        if (configKeyValue != null && configKeyValue.length == 2) {
          System.setProperty(configKeyValue[0], configKeyValue[1]);
        }
      }
    }
  }

}
