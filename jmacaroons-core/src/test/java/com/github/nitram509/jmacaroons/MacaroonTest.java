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


import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

import static org.fest.assertions.Assertions.assertThat;

public class MacaroonTest {

  @Test
  public void equals_and_hashcode_are_overwritten() {
    Macaroon m1 = new Macaroon("location", "identifier", new byte[]{1, 2, 3, 5, 6, 7, 8}, MacaroonVersion.VERSION_1);
    Macaroon m2 = new Macaroon("location", "identifier", new byte[]{1, 2, 3, 5, 6, 7, 8}, MacaroonVersion.VERSION_1);

    assertThat(m1.equals(m2)).isTrue();
    assertThat(m2.equals(m1)).isTrue();
    assertThat(m1 == m2).isFalse();

    assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
  }

  @Test
  public void inspect_null_safe() {
    Macaroon macaroon = new Macaroon(null, null, null, null, MacaroonVersion.VERSION_1);

    macaroon.inspect();

    // no exception
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void serialize_throws_NPE() {
    Macaroon macaroon = new Macaroon(null, null, null, null, MacaroonVersion.VERSION_1);

    macaroon.serialize();

    // NullPointerException
  }

  @DataProvider(name = "potentially_equal_macaroons")
  public static Object[][] testMacaroonEquality() {

    final Macaroon noCaveats = MacaroonsBuilder.create("http://equality.test", "secret, secret".getBytes(StandardCharsets.US_ASCII), "correct id", MacaroonVersion.VERSION_2);

    final Macaroon singleMacaroon = new MacaroonsBuilder("http://equality.test", "secret, secret".getBytes(StandardCharsets.US_ASCII), "correct id", MacaroonVersion.VERSION_2)
            .add_first_party_caveat("caveat = false")
            .getMacaroon();

    return new Object[][]{
            {noCaveats, true},
            {"Not a macaroon", false},
            {singleMacaroon, false},
            {MacaroonsBuilder.create("http://equality.wrong", "secret, secret".getBytes(StandardCharsets.US_ASCII), "correct id", MacaroonVersion.VERSION_2), false},
            {MacaroonsBuilder.create("http://equality.test", "secret".getBytes(StandardCharsets.US_ASCII), "correct id", MacaroonVersion.VERSION_2), false},
            {MacaroonsBuilder.create("http://equality.test", "secret, secret".getBytes(StandardCharsets.US_ASCII), "wrong id", MacaroonVersion.VERSION_1), false},
            {new Macaroon("http://equality.test", "correct id", "not a sig".getBytes(StandardCharsets.UTF_8), MacaroonVersion.VERSION_2), false},
            {new Macaroon(null, "correct id", "not a sig".getBytes(StandardCharsets.UTF_8), MacaroonVersion.VERSION_2), false},
            {new Macaroon("http://equality.test", null, "not a sig".getBytes(StandardCharsets.UTF_8), MacaroonVersion.VERSION_2), false},
            {new Macaroon("http://equality.test", "correct id",null, MacaroonVersion.VERSION_2), false},
    };
  }

  @Test(dataProvider = "potentially_equal_macaroons")
  public void test_macaroon_equality(Object potentiallyEqual, boolean isEqual) {
    final Macaroon m = new MacaroonsBuilder("http://equality.test", "secret, secret".getBytes(StandardCharsets.US_ASCII), "correct id", MacaroonVersion.VERSION_2).getMacaroon();

    assertThat(m.equals(potentiallyEqual)).isEqualTo(isEqual);
    assertThat(potentiallyEqual.equals(m)).isEqualTo(isEqual);
  }

  @DataProvider(name = "caveat_provider")
  public static Object[][] caveat_provider() {

    return new Object[][]{
            {"Not a caveat", false},
            {new CaveatPacket(CaveatPacket.Type.cid, "caveat value".getBytes(StandardCharsets.UTF_8)), true},
            {new CaveatPacket(CaveatPacket.Type.location, "caveat value".getBytes(StandardCharsets.UTF_8)), false},
    };
  }

  @Test(dataProvider = "caveat_provider")
  public void test_caveat_equality(Object potentiallyEqual, boolean isEqual) {
    final CaveatPacket caveatPacket = new CaveatPacket(CaveatPacket.Type.cid, "caveat value".getBytes(StandardCharsets.UTF_8));

    assertThat(caveatPacket.equals(potentiallyEqual)).isEqualTo(isEqual);
    assertThat(potentiallyEqual.equals(caveatPacket)).isEqualTo(isEqual);
  }

}