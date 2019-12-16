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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static org.fest.assertions.Assertions.assertThat;

public class MacaroonsBuilderTest {

  private String identifier;
  private String secret;
  private String location;
  private Macaroon m;

  @BeforeMethod
  public void setUp() {
    location = "http://mybank/";
    secret = "this is our super secret key; only we should know it";
    identifier = "we used our secret key";
  }

  @Test
  public void create_a_Macaroon_and_verify_signature_location_and_identfier() {
    m = new MacaroonsBuilder(location, secret, identifier).getMacaroon();

    assertThat(m.location).isEqualTo(location);
    assertThat(m.identifier).isEqualTo(identifier);
    assertThat(m.signature).isEqualTo("e3d9e02908526c4c0039ae15114115d97fdd68bf2ba379b342aaf0f617d0552f");
  }

  @Test
  public void create_a_Macaroon_with_static_helper_method() {
    m = MacaroonsBuilder.create("http://example.org/", "example secret key", "example entifier");

    assertThat(m.location).isEqualTo("http://example.org/");
    assertThat(m.identifier).isEqualTo("example entifier");
    assertThat(m.signature).isEqualTo("b642ce881b02b1e00b030e039374ece15ea852daa1a42a8dd4e7502977717f8b");
  }

  @Test
  public void create_a_Macaroon_and_inspect() {
    m = new MacaroonsBuilder(location, secret, identifier).getMacaroon();

    String inspect = m.inspect();

    assertThat(inspect).isEqualTo(
        "location http://mybank/\n" +
            "identifier we used our secret key\n" +
            "signature e3d9e02908526c4c0039ae15114115d97fdd68bf2ba379b342aaf0f617d0552f\n"
    );
  }

  @Test
  public void different_locations_doesnt_change_the_signatures() {
    Macaroon m1 = new MacaroonsBuilder("http://location_ONE", secret, identifier).getMacaroon();
    Macaroon m2 = new MacaroonsBuilder("http://location_TWO", secret, identifier).getMacaroon();

    assertThat(m1.signature).isEqualTo(m2.signature);
  }

  @Test
  public void Macaroon_can_be_serialized() {
    m = new MacaroonsBuilder(location, secret, identifier).getMacaroon();

    assertThat(m.serialize()).isEqualTo("MDAxY2xvY2F0aW9uIGh0dHA6Ly9teWJhbmsvCjAwMjZpZGVudGlmaWVyIHdlIHVzZWQgb3VyIHNlY3JldCBrZXkKMDAyZnNpZ25hdHVyZSDj2eApCFJsTAA5rhURQRXZf91ovyujebNCqvD2F9BVLwo");
  }

  @DataProvider(name = "computed_macaroon_provider")
  public Object[][] computed_macaroons() {
    identifier ="we used our secret key";
    byte[] secretBytes = secret.getBytes(StandardCharsets.US_ASCII);
    location ="http://www.example.org";

    return new Object[][]{
            {MacaroonsBuilder.create(location, secretBytes, identifier, MacaroonVersion.VERSION_1)},
            {MacaroonsBuilder.create(location, secret, identifier.getBytes(StandardCharsets.US_ASCII), MacaroonVersion.VERSION_1)},
            {MacaroonsBuilder.create(location, secretBytes, identifier.getBytes(StandardCharsets.US_ASCII), MacaroonVersion.VERSION_1)},
            {MacaroonsBuilder.create(location, secretBytes, identifier, MacaroonVersion.VERSION_2)},
            {MacaroonsBuilder.create(location, secret, identifier.getBytes(StandardCharsets.US_ASCII), MacaroonVersion.VERSION_2)},
            {MacaroonsBuilder.create(location, secretBytes, identifier.getBytes(StandardCharsets.US_ASCII), MacaroonVersion.VERSION_2)},
            {MacaroonsBuilder.create(location, secretBytes, identifier)},
            {MacaroonsBuilder.create(location, secret, identifier.getBytes(StandardCharsets.US_ASCII))},
            {MacaroonsBuilder.create(location, secretBytes, identifier.getBytes(StandardCharsets.US_ASCII))},
    };
  }

  @Test(dataProvider = "computed_macaroon_provider")
  public void ensure_static_methods_work_correctly(Macaroon m) {

    final String serialize = m.serialize();
    final Macaroon m2 = MacaroonsBuilder.deserialize(serialize).get(0);

    assertThat(m.equals(m2)).isTrue();
  }

}