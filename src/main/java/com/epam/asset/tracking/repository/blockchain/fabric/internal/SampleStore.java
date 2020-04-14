package com.epam.asset.tracking.repository.blockchain.fabric.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.hyperledger.fabric.sdk.Enrollment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleStore {

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  private static final Logger log = LoggerFactory.getLogger(
      SampleStore.class);
  private final Map<String, SampleUser> members = new HashMap<>();
  private String file;

  public SampleStore(File file) {
    this.file = file.getAbsolutePath();
  }

  private static PrivateKey getPrivateKeyFromBytes(byte[] data) throws IOException {
    final Reader pemReader = new StringReader(new String(data));
    final PrivateKeyInfo pemPair;
    try (PEMParser pemParser = new PEMParser(pemReader)) {
      pemPair = (PrivateKeyInfo) pemParser.readObject();
    }
    return new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getPrivateKey(pemPair);
  }

  /**
   * Get the value associated with name.
   *
   * @return value associated with the name
   */
  public String getValue(String name) {
    Properties properties = loadProperties();
    return properties.getProperty(name);
  }

  private Properties loadProperties() {
    Properties properties = new Properties();
    try (InputStream input = new FileInputStream(file)) {
      properties.load(input);
    } catch (FileNotFoundException e) {
      log.warn("Could not find the file \"{}\"", file);
    } catch (IOException e) {
      log.warn("Could not load keyvalue store from file \"{}\"", file, e);
    }
    return properties;
  }

  /**
   * Set the value associated with name.
   *
   * @param name The name of the parameter
   * @param value Value for the parameter
   */
  public void setValue(String name, String value) {
    Properties properties = loadProperties();
    try (OutputStream output = new FileOutputStream(file)) {
      properties.setProperty(name, value);
      properties.store(output, "");
    } catch (IOException e) {
      log.warn("Could not save the keyvalue store", e);
    }
  }

  /**
   * Get the user with a given name
   *
   * @return user
   */
  public SampleUser getMember(String name, String org) {
    // Try to get the SampleUser state from the cache
    SampleUser sampleUser = members.get(SampleUser.toKeyValStoreName(name, org));
    if (null != sampleUser) return sampleUser;

    // Create the SampleUser and try to restore it's state from the key value store (if found).
    sampleUser = new SampleUser(name, org, this);
    return sampleUser;
  }

  /**
   * Get the user with a given name
   *
   * @return user
   */
  public SampleUser getMember(String name, String org, String mspId, File privateKeyFile, File certificateFile) throws IOException {
    // Try to get the SampleUser state from the cache
    SampleUser sampleUser = members.get(SampleUser.toKeyValStoreName(name, org));
    if (null != sampleUser) return sampleUser;

    // Create the SampleUser and try to restore it's state from the key value store (if found).
    sampleUser = new SampleUser(name, org, this);
    sampleUser.setMspId(mspId);
    String certificate = new String(IOUtils.toByteArray(new FileInputStream(certificateFile)), StandardCharsets.UTF_8);
    PrivateKey privateKey = getPrivateKeyFromBytes(IOUtils.toByteArray(new FileInputStream(privateKeyFile)));
    sampleUser.setEnrollment(new SampleStore.SampleStoreEnrollement(privateKey, certificate));
    sampleUser.saveState();
    return sampleUser;
  }

private static final class SampleStoreEnrollement implements Enrollment, Serializable {
    private static final long serialVersionUID = -2784835212445309006L;
    private final PrivateKey privateKey;
    private final String certificate;

    SampleStoreEnrollement(PrivateKey privateKey, String certificate) {
      this.certificate = certificate;
      this.privateKey = privateKey;
    }

    @Override
    public PrivateKey getKey() {
      return privateKey;
    }

    @Override
    public String getCert() {
      return certificate;
    }
  }
}
