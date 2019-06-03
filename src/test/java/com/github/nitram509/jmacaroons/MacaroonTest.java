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


import org.testng.annotations.Test;

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

}