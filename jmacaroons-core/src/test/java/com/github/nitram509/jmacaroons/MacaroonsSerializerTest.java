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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class MacaroonsSerializerTest {

  private String identifier;
  private String secret;
  private String location;

  @BeforeMethod
  public void setUp() {
    location = "http://mybank/";
    secret = "this is our super secret key; only we should know it";
    identifier = "we used our secret key";
  }

  @Test
  public void Macaroon_can_be_serialized() {
    Macaroon m = new MacaroonsBuilder(location, secret, identifier).getMacaroon();

    assertThat(MacaroonsSerializer.serialize(m)).isEqualTo("MDAxY2xvY2F0aW9uIGh0dHA6Ly9teWJhbmsvCjAwMjZpZGVudGlmaWVyIHdlIHVzZWQgb3VyIHNlY3JldCBrZXkKMDAyZnNpZ25hdHVyZSDj2eApCFJsTAA5rhURQRXZf91ovyujebNCqvD2F9BVLwo");
    assertThat(MacaroonsSerializer.serialize(m)).isEqualTo(m.serialize());
  }

  @Test
  public void Macaroon_with_caveat_can_be_serialized() {
    Macaroon m = new MacaroonsBuilder(location, secret, identifier)
        .add_first_party_caveat("account = 3735928559")
        .getMacaroon();

    assertThat(MacaroonsSerializer.serialize(m)).isEqualTo("MDAxY2xvY2F0aW9uIGh0dHA6Ly9teWJhbmsvCjAwMjZpZGVudGlmaWVyIHdlIHVzZWQgb3VyIHNlY3JldCBrZXkKMDAxZGNpZCBhY2NvdW50ID0gMzczNTkyODU1OQowMDJmc2lnbmF0dXJlIB7-R2PykNvODB0IR3Nn4R9O7kVqZJM89mLXl3LbuCEoCg");
    assertThat(MacaroonsSerializer.serialize(m)).isEqualTo(m.serialize());
  }

  @Test
  public void Macaroon_with_3rd_party_caveat_can_be_serialized() {
    Macaroon m = new MacaroonsBuilder(location, secret, identifier)
        .add_first_party_caveat("account = 3735928559")
        .add_third_party_caveat("http://auth.mybank/", "SECRET for 3rd party caveat", identifier)
        .getMacaroon();

    assertThat(MacaroonsSerializer.serialize(m)).startsWith("MDAxY2xvY2F0aW9uIGh0dHA6Ly9teWJhbmsvCjAwMjZpZGVudGlmaWVyIHdlIHVzZWQgb3VyIHNlY3JldCBrZXkKMDAxZGNpZCBhY2NvdW50ID0gMzczNTkyODU1OQowMDFmY2lkIHdlIHVzZWQgb3VyIHNlY3JldCBrZXkKMDA1MXZpZC");
    assertThat(MacaroonsSerializer.serialize(m)).isEqualTo(m.serialize());
  }

  @Test
  public void Macaroon_v2_json_can_be_serialized() {
      Macaroon m = new MacaroonsBuilder(location, secret, identifier, MacaroonVersion.VERSION_2).getMacaroon();

      assertThat(MacaroonsSerializer.serialize(m, MacaroonVersion.SerializationVersion.V2_JSON)).isEqualTo("{\"v\":2,\"l\":\"http://mybank/\",\"i\":\"we used our secret key\",\"s64\":\"49ngKQhSbEwAOa4VEUEV2X_daL8ro3mzQqrw9hfQVS8\"}");
      assertThat(m.serialize(MacaroonVersion.SerializationVersion.V2_JSON)).isEqualTo("{\"v\":2,\"l\":\"http://mybank/\",\"i\":\"we used our secret key\",\"s64\":\"49ngKQhSbEwAOa4VEUEV2X_daL8ro3mzQqrw9hfQVS8\"}");
  }

  @Test
  public void Macaroon_v2_json_array_can_be_serialized() {
    Macaroon m1 = new MacaroonsBuilder(location, secret, identifier, MacaroonVersion.VERSION_2).getMacaroon();
    Macaroon m2 = new MacaroonsBuilder(location, secret, identifier, MacaroonVersion.VERSION_2)
            .add_first_party_caveat("account = 3735928559")
            .getMacaroon();

    final List<Macaroon> mArray = new ArrayList<>();
    mArray.add(m1);
    mArray.add(m2);

    assertThat(MacaroonsSerializer.serialize(mArray, MacaroonVersion.SerializationVersion.V2_JSON)).isEqualTo("[{\"v\":2,\"l\":\"http://mybank/\",\"i\":\"we used our secret key\",\"s64\":\"49ngKQhSbEwAOa4VEUEV2X_daL8ro3mzQqrw9hfQVS8\"},{\"v\":2,\"l\":\"http://mybank/\",\"i\":\"we used our secret key\",\"c\":[{\"i64\":\"YWNjb3VudCA9IDM3MzU5Mjg1NTk\"}],\"s64\":\"Hv5HY_KQ284MHQhHc2fhH07uRWpkkzz2YteXctu4ISg\"}]");
  }

  @Test
  public void Macaroon_v2_json_with_caveat_can_be_serialized() {
      Macaroon m = new MacaroonsBuilder(location, secret, identifier, MacaroonVersion.VERSION_2)
              .add_first_party_caveat("account = 3735928559")
              .getMacaroon();

      final Macaroon m2 = MacaroonsDeSerializer.deserialize(m.serialize(MacaroonVersion.SerializationVersion.V2_JSON)).get(0);
      assertThat(m).isEqualTo(m2);
  }
}