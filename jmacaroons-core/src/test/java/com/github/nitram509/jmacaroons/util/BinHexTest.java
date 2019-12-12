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

package com.github.nitram509.jmacaroons.util;

import org.testng.annotations.Test;

import static org.fest.assertions.Assertions.assertThat;

public class BinHexTest {

  @Test
  public void happy_path() {
    final byte[] testBytes = {(byte) 1, (byte) 128, (byte) 255};
    String hexstr = BinHex.bin2hex(testBytes);

    assertThat(hexstr).isEqualTo("0180ff");

    assertThat(BinHex.hex2bin(hexstr)).isEqualTo(testBytes);
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  public void null_safe() {
    assertThat(BinHex.bin2hex((byte[]) null)).isNull();

    assertThat(BinHex.hex2bin(null)).isNull();
  }

  @Test
  public void empty_string() {
    String hexstr = BinHex.bin2hex();

    assertThat(hexstr).isEqualTo("");
  }

}