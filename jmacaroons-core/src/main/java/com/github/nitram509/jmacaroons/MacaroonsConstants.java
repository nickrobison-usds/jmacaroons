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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class MacaroonsConstants {

  /* public constants ... copied from libmacaroons */

  /**
   * All byte strings must be less than this length.
   * Enforced via "assert" internally.
   */
  public static final int MACAROON_MAX_STRLEN = 32768;
  /**
   * Place a sane limit on the number of caveats
   */
  public static final int MACAROON_MAX_CAVEATS = 65536;
  /**
   * Recommended secret length
   */
  public static final int MACAROON_SUGGESTED_SECRET_LENGTH = 32;
  public static final int MACAROON_HASH_BYTES = 32;

  /* ********************************* */
  /* more internal use ... */
  /* ********************************* */

  protected static final int PACKET_PREFIX_LENGTH = 4;
  protected static final int PACKET_MAX_SIZE = 65535;

  protected static final int MACAROON_SECRET_KEY_BYTES = 32;
  protected static final int MACAROON_SECRET_NONCE_BYTES = 24;

  /**
   * The number of zero bytes required by crypto_secretbox
   * before the plaintext.
   */
  protected static final int MACAROON_SECRET_TEXT_ZERO_BYTES = 32;
  /**
   * The number of zero bytes placed by crypto_secretbox
   * before the ciphertext
   */
  protected static final int MACAROON_SECRET_BOX_ZERO_BYTES = 16;

  protected static final int SECRET_BOX_OVERHEAD = MACAROON_SECRET_TEXT_ZERO_BYTES - MACAROON_SECRET_BOX_ZERO_BYTES;
  protected static final int VID_NONCE_KEY_SZ = MACAROON_SECRET_NONCE_BYTES + MACAROON_HASH_BYTES + SECRET_BOX_OVERHEAD;

  protected static final String LOCATION = "location";
  protected static final byte[] LOCATION_BYTES = LOCATION.getBytes(StandardCharsets.US_ASCII);

  protected static final String IDENTIFIER = "identifier";
  protected static final byte[] IDENTIFIER_BYTES = IDENTIFIER.getBytes(StandardCharsets.US_ASCII);

  protected static final String SIGNATURE = "signature";
  protected static final byte[] SIGNATURE_BYTES = SIGNATURE.getBytes(StandardCharsets.US_ASCII);

  protected static final String CID = "cid";
  protected static final byte[] CID_BYTES = CID.getBytes(StandardCharsets.US_ASCII);

  protected static final String VID = "vid";
  protected static final byte[] VID_BYTES = VID.getBytes(StandardCharsets.US_ASCII);

  protected static final String CL = "cl";
  protected static final byte[] CL_BYTES = CL.getBytes(StandardCharsets.US_ASCII);

  protected static final char LINE_SEPARATOR = '\n';
  protected static final int LINE_SEPARATOR_LEN = 1;

  protected static final char KEY_VALUE_SEPARATOR = ' ';
  protected static final int KEY_VALUE_SEPARATOR_LEN = 1;

  protected static final Charset IDENTIFIER_CHARSET = StandardCharsets.UTF_8;

  protected static final Charset RAW_BYTE_CHARSET = StandardCharsets.ISO_8859_1;

  private MacaroonsConstants() {
    // Not used
  }
}
