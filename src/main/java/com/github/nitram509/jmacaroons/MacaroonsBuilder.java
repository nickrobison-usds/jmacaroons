/*
 * Copyright 2014 Martin W. Kirst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nitram509.jmacaroons;

import com.github.nitram509.jmacaroons.util.ArrayTools;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static com.github.nitram509.jmacaroons.CryptoTools.*;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.*;

/**
 * Used to build and modify Macaroons, example:
 * <pre>{@code
 * String location = "http://www.example.org";
 * String secretKey = "this is our super secret key; only we should know it";
 * String identifier = "we used our secret key";
 * Macaroon macaroon = MacaroonsBuilder.create(location, secretKey, identifier);
 * }</pre>
 */
public class MacaroonsBuilder {

  private Macaroon macaroon = null;

  /**
   * @param location   location
   * @param secretKey  secretKey this secret will be enhanced, in case it's shorter than {@link com.github.nitram509.jmacaroons.MacaroonsConstants#MACAROON_SUGGESTED_SECRET_LENGTH}
   * @param identifier identifier
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException
   */
  public MacaroonsBuilder(String location, String secretKey, String identifier) throws GeneralSecurityRuntimeException {
    this.macaroon = computeMacaroon(location, secretKey, identifier, MacaroonVersion.VERSION_1);
  }
  /**
   * @param location   location
   * @param secretKey  secretKey this secret will be enhanced, in case it's shorter than {@link com.github.nitram509.jmacaroons.MacaroonsConstants#MACAROON_SUGGESTED_SECRET_LENGTH}
   * @param identifier identifier
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException
   */
  public MacaroonsBuilder(String location, String secretKey, String identifier, MacaroonVersion version) throws GeneralSecurityRuntimeException {
    this.macaroon = computeMacaroon(location, secretKey, identifier, version);
  }

  /**
   * @param location   location
   * @param secretKey  secretKey this secret will be used as it is (be sure that has suggested length {@link com.github.nitram509.jmacaroons.MacaroonsConstants#MACAROON_SUGGESTED_SECRET_LENGTH})
   * @param identifier identifier
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException
   */
  public MacaroonsBuilder(String location, byte[] secretKey, String identifier) throws GeneralSecurityRuntimeException {
    this.macaroon = computeMacaroon(location, secretKey, identifier, MacaroonVersion.VERSION_1);
  }

  /**
   * @param location   location
   * @param secretKey  secretKey this secret will be used as it is (be sure that has suggested length {@link com.github.nitram509.jmacaroons.MacaroonsConstants#MACAROON_SUGGESTED_SECRET_LENGTH})
   * @param identifier identifier
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException
   */
  public MacaroonsBuilder(String location, byte[] secretKey, String identifier, MacaroonVersion version) throws GeneralSecurityRuntimeException {
    this.macaroon = computeMacaroon(location, secretKey, identifier, version);
  }

  /**
   * @param macaroon macaroon to modify
   */
  public MacaroonsBuilder(Macaroon macaroon) {
    assert macaroon != null;
    this.macaroon = macaroon;
  }

  /**
   * @param location   location
   * @param secretKey  secretKey
   * @param identifier identifier
   * @return {@link com.github.nitram509.jmacaroons.Macaroon}
   */
  public static Macaroon create(String location, String secretKey, String identifier) {
    return computeMacaroon(location, secretKey, identifier, MacaroonVersion.VERSION_1);
  }

  /**
   * @param location   location
   * @param secretKey  secretKey
   * @param identifier identifier
   * @return {@link com.github.nitram509.jmacaroons.Macaroon}
   */
  public static Macaroon create(String location, byte[] secretKey, String identifier) {
    return computeMacaroon(location, secretKey, identifier, MacaroonVersion.VERSION_1);
  }

  /**
   * @param location   location
   * @param secretKey  secretKey
   * @param identifier identifier
   * @return {@link com.github.nitram509.jmacaroons.Macaroon}
   */
  public static Macaroon create(String location, byte[] secretKey, String identifier, MacaroonVersion version) {
    return computeMacaroon(location, secretKey, identifier, version);
  }

  /**
   * @param macaroon macaroon
   * @return {@link com.github.nitram509.jmacaroons.MacaroonsBuilder}
   */
  public static MacaroonsBuilder modify(Macaroon macaroon) {
    return new MacaroonsBuilder(macaroon);
  }

  /**
   * @param serializedMacaroon serializedMacaroon
   * @return {@link com.github.nitram509.jmacaroons.Macaroon}
   * @throws com.github.nitram509.jmacaroons.NotDeSerializableException when serialized macaroon is not valid base64, length is to short or contains invalid packet data
   */
  public static Macaroon deserialize(String serializedMacaroon) throws IllegalArgumentException {
    return MacaroonsDeSerializer.deserialize(serializedMacaroon);
  }

  /**
   * @return a {@link com.github.nitram509.jmacaroons.Macaroon}
   */
  public Macaroon getMacaroon() {
    return macaroon;
  }

  /**
   * @param caveat caveat
   * @return this {@link com.github.nitram509.jmacaroons.MacaroonsBuilder}
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException
   * @throws IllegalStateException                                           if there are more than {@link com.github.nitram509.jmacaroons.MacaroonsConstants#MACAROON_MAX_CAVEATS} caveats.
   */
  public MacaroonsBuilder add_first_party_caveat(String caveat) throws IllegalStateException, GeneralSecurityRuntimeException {
    if (caveat != null) {
      byte[] caveatBytes = caveat.getBytes(MacaroonsConstants.IDENTIFIER_CHARSET);
      assert caveatBytes.length < MACAROON_MAX_STRLEN;
      if (this.macaroon.caveatPackets.length + 1 > MACAROON_MAX_CAVEATS) {
        throw new IllegalStateException("Too many caveats. There are max. " + MACAROON_MAX_CAVEATS + " caveats allowed.");
      }
      try {
        byte[] signature = macaroon_hmac(macaroon.signatureBytes, caveatBytes);
        CaveatPacket[] caveatsAppended = ArrayTools.appendToArray(macaroon.caveatPackets, new CaveatPacket(CaveatPacket.Type.cid, caveatBytes));
        this.macaroon = new Macaroon(macaroon.location, macaroon.identifier, signature, caveatsAppended, MacaroonVersion.VERSION_1);
      } catch (InvalidKeyException | NoSuchAlgorithmException e) {
        throw new GeneralSecurityRuntimeException(e);
      }
    }
    return this;
  }

  /**
   * Adds a third-party caveat to a macaroon.
   * The ID is converted to a byte array via the {@link MacaroonsConstants#IDENTIFIER_CHARSET} charset.
   *
   * @param location - {@link String} URL of third party
   * @param secret - {@link String} secret key used to encrypt the caveat
   * @param identifier - {@link String} Caveat ID in String format
   * @return this {@link com.github.nitram509.jmacaroons.MacaroonsBuilder}
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException
   * @throws IllegalStateException                                           if there are more than {@link com.github.nitram509.jmacaroons.MacaroonsConstants#MACAROON_MAX_CAVEATS} caveats.
   */
  public MacaroonsBuilder add_third_party_caveat(String location, String secret, String identifier) throws IllegalStateException, GeneralSecurityRuntimeException {
    return add_third_party_caveat(location, secret, identifier.getBytes(IDENTIFIER_CHARSET));
  }

  /**
   * Adds a third-party caveat to a macaroon.
   * The raw byte array is used directly, with no special encoding applied.
   *
   * @param location - {@link String} URL of third party
   * @param secret - {@link String} secret key used to encrypt the caveat
   * @param identifier - {@link byte[]} Caveat ID byte values
   * @return this {@link com.github.nitram509.jmacaroons.MacaroonsBuilder}
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException
   * @throws IllegalStateException                                           if there are more than {@link com.github.nitram509.jmacaroons.MacaroonsConstants#MACAROON_MAX_CAVEATS} caveats.
   */
  public MacaroonsBuilder add_third_party_caveat(String location, String secret, byte[] identifier) throws IllegalStateException, GeneralSecurityRuntimeException {
    assert location.length() < MACAROON_MAX_STRLEN;
    assert identifier.length < MACAROON_MAX_STRLEN;

    if (this.macaroon.caveatPackets.length + 1 > MACAROON_MAX_CAVEATS) {
      throw new IllegalStateException("Too many caveats. There are max. " + MACAROON_MAX_CAVEATS + " caveats allowed.");
    }
    try {
      final ThirdPartyPacket thirdPartyPacket = macaroon_add_third_party_caveat_raw(macaroon.signatureBytes, secret, identifier);
      byte[] hash = thirdPartyPacket.signature;
      CaveatPacket[] caveatsExtended = ArrayTools.appendToArray(macaroon.caveatPackets,
          new CaveatPacket(CaveatPacket.Type.cid, identifier),
          new CaveatPacket(CaveatPacket.Type.vid, thirdPartyPacket.vid_data),
          new CaveatPacket(CaveatPacket.Type.cl, location)
      );
      this.macaroon = new Macaroon(macaroon.location, macaroon.identifier, hash, caveatsExtended, MacaroonVersion.VERSION_1);
    } catch (InvalidKeyException | NoSuchAlgorithmException e) {
      throw new GeneralSecurityRuntimeException(e);
    }
    return this;
  }

  /**
   * @param macaroon macaroon used for preparing a request
   * @return this {@link com.github.nitram509.jmacaroons.MacaroonsBuilder}
   * @throws com.github.nitram509.jmacaroons.GeneralSecurityRuntimeException
   */
  public MacaroonsBuilder prepare_for_request(Macaroon macaroon) throws GeneralSecurityRuntimeException {
    assert macaroon.signatureBytes.length > 0;
    assert getMacaroon().signatureBytes.length > 0;
    try {
      byte[] hash = macaroon_bind(getMacaroon().signatureBytes, macaroon.signatureBytes);
      this.macaroon = new Macaroon(macaroon.location, macaroon.identifier, hash, macaroon.caveatPackets, MacaroonVersion.VERSION_1);
      return this;
    } catch (InvalidKeyException | NoSuchAlgorithmException e) {
      throw new GeneralSecurityRuntimeException(e);
    }
  }

  private static Macaroon computeMacaroon(String location, String secretKey, String identifier, MacaroonVersion version) throws GeneralSecurityRuntimeException {
    try {
      return computeMacaroon(location, generate_derived_key(secretKey), identifier, version);
    } catch (InvalidKeyException | NoSuchAlgorithmException e) {
      throw new GeneralSecurityRuntimeException(e);
    }
  }

  private static Macaroon computeMacaroon(String location, byte[] secretKey, String identifier, MacaroonVersion version) throws GeneralSecurityRuntimeException {
    assert location.length() < MACAROON_MAX_STRLEN;
    assert identifier.length() < MACAROON_MAX_STRLEN;
    try {
      byte[] hash = macaroon_hmac(secretKey, identifier);
      return new Macaroon(location, identifier, hash, version);
    } catch (InvalidKeyException | NoSuchAlgorithmException e) {
      throw new GeneralSecurityRuntimeException(e);
    }
  }

}
