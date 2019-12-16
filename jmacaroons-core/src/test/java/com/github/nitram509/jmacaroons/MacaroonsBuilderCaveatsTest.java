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

import java.util.Random;
import java.util.stream.Stream;

import static com.github.nitram509.jmacaroons.CaveatPacket.Type;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.MACAROON_MAX_CAVEATS;
import static org.fest.assertions.Assertions.assertThat;

public class MacaroonsBuilderCaveatsTest {

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
  public void add_first_party_caveat() {
    m = new MacaroonsBuilder(location, secret, identifier)
        .add_first_party_caveat("account = 3735928559")
        .getMacaroon();

    assertThat(m.identifier).isEqualTo(m.identifier);
    assertThat(m.location).isEqualTo(m.location);
    assertThat(m.caveatPackets[0].getType()).isEqualTo(Type.cid);
    assertThat(m.caveatPackets).isEqualTo(new CaveatPacket[]{new CaveatPacket(Type.cid, "account = 3735928559")});
    assertThat(m.signature).isEqualTo("1efe4763f290dbce0c1d08477367e11f4eee456a64933cf662d79772dbb82128");
  }

  @Test
  public void add_null_first_party_caveat() {
    m = new MacaroonsBuilder(location, secret, identifier)
            .add_first_party_caveat(null)
            .getMacaroon();

    assertThat(m.inspect()).isEqualTo("location http://mybank/\n" +
            "identifier we used our secret key\n" +
            "signature e3d9e02908526c4c0039ae15114115d97fdd68bf2ba379b342aaf0f617d0552f\n");
  }

  @Test(expectedExceptions = IllegalStateException.class)
  void add_too_many_first_party_caveats() {
    final MacaroonsBuilder builder = new MacaroonsBuilder(location, secret, identifier);
    caveatSupplier()
            .limit(MACAROON_MAX_CAVEATS)
            .forEach(builder::add_first_party_caveat);

    final Macaroon m = builder.getMacaroon();
    assertThat(m.caveatPackets.length).isEqualTo(MACAROON_MAX_CAVEATS);

    final MacaroonsBuilder modify = MacaroonsBuilder.modify(m);

    caveatSupplier()
            .limit(1)
            .forEach(modify::add_first_party_caveat);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  void add_too_many_third_party_caveats() {
    final MacaroonsBuilder builder = new MacaroonsBuilder(location, secret, identifier);
    caveatSupplier()
            .limit(MACAROON_MAX_CAVEATS)
            .forEach(cav -> builder.add_third_party_caveat("http://local.test", "secret", cav));

    final Macaroon m = builder.getMacaroon();
    assertThat(m.caveatPackets.length).isEqualTo(MACAROON_MAX_CAVEATS);

    final MacaroonsBuilder modify = MacaroonsBuilder.modify(m);

    caveatSupplier()
            .limit(1)
            .forEach(cav -> modify.add_third_party_caveat("http://local.test", "secret", cav));
  }

  @Test
  public void modify_also_copies_first_party_caveats() {
    // given
    m = new MacaroonsBuilder(location, secret, identifier)
        .add_first_party_caveat("account = 3735928559")
        .getMacaroon();

    // when
    m = MacaroonsBuilder.modify(m)
        .getMacaroon();

    assertThat(m.identifier).isEqualTo(m.identifier);
    assertThat(m.location).isEqualTo(m.location);
    assertThat(m.signature).isEqualTo("1efe4763f290dbce0c1d08477367e11f4eee456a64933cf662d79772dbb82128");

    final CaveatPacket expectedCaveat = new CaveatPacket(Type.cid, "account = 3735928559");
    assertThat(m.caveatPackets[0].equals(expectedCaveat)).isTrue();
    assertThat(m.caveatPackets[0].equals(new CaveatPacket(Type.cid, "account = 1"))).isFalse();
    assertThat(m.caveatPackets[0].getType()).isEqualTo(expectedCaveat.getType());
    assertThat(m.caveatPackets[0].toString()).isEqualTo("cid [97, 99, 99, 111, 117, 110, 116, 32, 61, 32, 51, 55, 51, 53, 57, 50, 56, 53, 53, 57]");
    assertThat(m.caveatPackets[0].getValueAsText()).isEqualTo(expectedCaveat.getValueAsText());
    assertThat(m.caveatPackets).isEqualTo(new CaveatPacket[]{expectedCaveat});
    assertThat(m.caveatPackets).isNotEqualTo(new CaveatPacket[]{});

  }

  @Test
  public void add_first_party_caveat_3_times() {
    m = new MacaroonsBuilder(location, secret, identifier)
        .add_first_party_caveat("account = 3735928559")
        .add_first_party_caveat("time < 2015-01-01T00:00")
        .add_first_party_caveat("email = alice@example.org")
        .getMacaroon();

    assertThat(m.identifier).isEqualTo(m.identifier);
    assertThat(m.location).isEqualTo(m.location);
    assertThat(m.caveatPackets).isEqualTo(new CaveatPacket[]{
        new CaveatPacket(Type.cid, "account = 3735928559"),
        new CaveatPacket(Type.cid, "time < 2015-01-01T00:00"),
        new CaveatPacket(Type.cid, "email = alice@example.org")
    });
    assertThat(m.signature).isEqualTo("882e6d59496ed5245edb7ab5b8839ecd63e5d504e54839804f164070d8eed952");
  }

  @Test
  public void add_first_party_caveat_German_umlauts_using_UTF8_encoding() {
    MacaroonsBuilder mb = new MacaroonsBuilder(location, secret, identifier);
    mb = mb.add_first_party_caveat("\u00E4");
    mb = mb.add_first_party_caveat("\u00FC");
    mb = mb.add_first_party_caveat("\u00F6");
    m = mb.getMacaroon();

    assertThat(m.identifier).isEqualTo(m.identifier);
    assertThat(m.location).isEqualTo(m.location);
    assertThat(m.caveatPackets).isEqualTo(new CaveatPacket[]{
        new CaveatPacket(Type.cid, "\u00E4"),
        new CaveatPacket(Type.cid, "\u00FC"),
        new CaveatPacket(Type.cid, "\u00F6")
    });
    assertThat(m.signature).isEqualTo("e38cce985a627fbfaea3490ca184fb8c59ec2bd14f0adc3b5035156e94daa111");
  }

  @Test
  public void add_first_party_caveat_null_save() {
    m = new MacaroonsBuilder(location, secret, identifier)
        .add_first_party_caveat(null)
        .getMacaroon();

    assertThat(m.identifier).isEqualTo(m.identifier);
    assertThat(m.location).isEqualTo(m.location);
    assertThat(m.signature).isEqualTo("e3d9e02908526c4c0039ae15114115d97fdd68bf2ba379b342aaf0f617d0552f");
  }

  @Test
  public void add_first_party_caveat_inspect() {
    m = new MacaroonsBuilder(location, secret, identifier)
        .add_first_party_caveat("account = 3735928559")
        .getMacaroon();

    String inspect = m.inspect();

    assertThat(inspect).isEqualTo(
        "location http://mybank/\n" +
            "identifier we used our secret key\n" +
            "cid account = 3735928559\n" +
            "signature 1efe4763f290dbce0c1d08477367e11f4eee456a64933cf662d79772dbb82128\n"
    );
  }

  private static Stream<String> caveatSupplier() {
    final Random randomGenerator = new Random();
    return Stream.generate(() -> String.format("random_value = %s", randomGenerator.nextInt()));
  }

}