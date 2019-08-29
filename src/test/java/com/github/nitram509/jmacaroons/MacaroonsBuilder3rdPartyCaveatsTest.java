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

import static com.github.nitram509.jmacaroons.CaveatPacket.Type;
import static org.fest.assertions.Assertions.assertThat;

public class MacaroonsBuilder3rdPartyCaveatsTest {

  @Test
  public void add_third_party_caveat() {

    String secret = "this is a different super-secret key; never use the same secret twice";
    String publicIdentifier = "we used our other secret key";
    String location = "http://mybank/";

    String caveat_key = "4; guaranteed random by a fair toss of the dice";
    String predicate = "user = Alice";
    String identifier = "this was how we remind auth of key/pred";
    Macaroon m = new MacaroonsBuilder(location, secret, publicIdentifier)
        .add_first_party_caveat("account = 3735928559")
        .add_third_party_caveat("http://auth.mybank/", caveat_key, identifier)
        .getMacaroon();

    assertThat(m.identifier).isEqualTo(publicIdentifier);
    assertThat(m.location).isEqualTo(location);
    assertThat(m.caveatPackets[0]).isEqualTo(new CaveatPacket(Type.cid, "account = 3735928559"));
    assertThat(m.caveatPackets[1]).isEqualTo(new CaveatPacket(Type.cid, identifier));
    // packet with type VID can't be asserted to be equal to a constant, because random nonce influences signature
    assertThat(m.caveatPackets[3]).isEqualTo(new CaveatPacket(Type.cl, "http://auth.mybank/"));
    assertThat(m.caveatPackets).hasSize(4);
    // signature can't be asserted to be equal to a constant, because random nonce influences signature
    assertThat(m.serialize()).startsWith("MDAxY2xvY2F0aW9uIGh0dHA6Ly9teWJhbmsvCjAwMmNpZGVudGlmaWVyIHdlIHVzZWQgb3VyIG90aGVyIHNlY3JldCBrZXkKMDAxZGNpZCBhY2NvdW50ID0gMzczNTkyODU1OQowMDMwY2lkIHRoaXMgd2FzIGhvdyB3ZSByZW1pbmQgYXV0aCBvZiBrZXkvcHJlZAowMDUxdmlkI");
  }

  @Test
  public void add_third_party_encoded() {
    String secret = "this is a different super-secret key; never use the same secret twice";
    String publicIdentifier = "we used our other secret key";
    String location = "http://mybank/";

    String caveat_key = "4; guaranteed random by a fair toss of the dice";
    String predicate = "user = Alice";
    // Test raw byte string
    String identifier = "³\u0016^Ü\u0091\u0007\u0007'Võ\u0016Ü\u009F\u0090tÄrrª\u0088í9@é? ºrd\u0018x÷";
    final String third_party_location = "http://auth.mybank/";
    Macaroon m = new MacaroonsBuilder(location, secret, publicIdentifier)
            .add_first_party_caveat(predicate)
            .add_third_party_caveat(third_party_location, caveat_key, identifier.getBytes(MacaroonsConstants.RAW_BYTE_CHARSET))
            .getMacaroon();

    assertThat(m.identifier).isEqualTo(publicIdentifier);
    assertThat(m.location).isEqualTo(location);
    assertThat(m.caveatPackets[0]).isEqualTo(new CaveatPacket(Type.cid, predicate));
    assertThat(m.caveatPackets[1]).isEqualTo(new CaveatPacket(Type.cid, identifier.getBytes(MacaroonsConstants.RAW_BYTE_CHARSET)));
    // packet with type VID can't be asserted to be equal to a constant, because random nonce influences signature
    assertThat(m.caveatPackets[3]).isEqualTo(new CaveatPacket(Type.cl, third_party_location));
    assertThat(m.caveatPackets).hasSize(4);

    final Macaroon D = new MacaroonsBuilder(third_party_location, caveat_key, identifier.getBytes(MacaroonsConstants.RAW_BYTE_CHARSET))
            .getMacaroon();

    final Macaroon DP = new MacaroonsBuilder(m)
            .prepare_for_request(D)
            .getMacaroon();

    new MacaroonsVerifier(m)
            .satisfyExact(predicate)
            .satisfy3rdParty(DP)
            .assertIsValid(secret);
  }

}