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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nitram509.jmacaroons.util.Base64;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.nitram509.jmacaroons.CaveatPacket.Type;
import static com.github.nitram509.jmacaroons.MacaroonsConstants.*;

class MacaroonsDeSerializer {

  private static final ObjectMapper mapper = new ObjectMapper();

  private static final byte[] HEX_ALPHABET = new byte[]{
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 0, 0, 0, 0, 0,
      0, 10, 11, 12, 13, 14, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 10, 11, 12, 13, 14, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  private static final String MACAROON_IS_NULL = "Macaroon cannot be null";

  public static List<Macaroon> deserialize(String serializedMacaroon) throws NotDeSerializableException {
        assert serializedMacaroon != null;

        // Determine which format to use
        // Check to see if we're a single JSON Macaroon, or an array of JSON macaroons
        if (serializedMacaroon.charAt(0) == '{' || serializedMacaroon.charAt(0) == '[') {
            return deserializeMaybeJSONArray(serializedMacaroon);
        }
        return Collections.singletonList(deserializeBinaryFormat(Base64.decode(serializedMacaroon)));
    }

  private static Macaroon deserializeBinaryFormat(byte[] bytes) {
    return deserializeV1BinaryFormat(bytes);
  }

  private static Macaroon deserializeV1BinaryFormat(byte[] bytes) {
    int minLength = MACAROON_HASH_BYTES + KEY_VALUE_SEPARATOR_LEN + SIGNATURE.length();
    if (bytes.length < minLength) {
      throw new NotDeSerializableException("Couldn't deserialize macaroon. Not enough bytes for signature found. There have to be at least " + minLength + " bytes");
    }
    return deserializeStream(new StatefulPacketReader(bytes));
  }

  private static List<Macaroon> deserializeMaybeJSONArray(String macaroonBytes) {
    final JsonNode jsonValue;
    try {
      jsonValue = mapper.readTree(macaroonBytes);
    } catch (IOException e) {
      throw new NotDeSerializableException(e.getCause());
    }

    if (jsonValue == null) {
      throw new NotDeSerializableException(MACAROON_IS_NULL);
    }

    final List<Macaroon> macaroons = new ArrayList<>();

    // If it's an array, iterate over all the children and convert them
    if (jsonValue.isArray()) {
      for (final JsonNode jsonNode : jsonValue) {
        macaroons.add(deserializeJSONFormat(jsonNode));
      }
    } else {
      // If it's not an array, deserialize the Macaroon directly.
      macaroons.add(deserializeJSONFormat(jsonValue));
    }

    return macaroons;
  }

  private static Macaroon deserializeJSONFormat(JsonNode jsonValue) {
    // Check for version, if it doesn't have one, assume V2 encoding
    int version = 2;
    if (jsonValue.has("v")) {
      version = jsonValue.get("v").asInt(0);
    }
    switch (version) {
      case 2: return deserializeV2JSON(jsonValue);
      case 1: throw new IllegalArgumentException("Don't support V1 json, yet");
      default: throw new IllegalArgumentException(String.format("Cannot deserialize version %d", version));
    }
  }

  private static Macaroon deserializeV2JSON(JsonNode json) {
    final MacaroonJSONV2 jsonMacaroon;
    try {
       jsonMacaroon = mapper.treeToValue(json, MacaroonJSONV2.class);
    } catch (JsonProcessingException e) {
      throw new NotDeSerializableException(e.getCause());
    }


    if (jsonMacaroon == null) {
      throw new NotDeSerializableException(MACAROON_IS_NULL);
    }

    // Extract the caveats
    return new Macaroon(jsonMacaroon.getLocation(),
            jsonMacaroon.parseIdentifier(),
            jsonMacaroon.parseSignature(),
            jsonMacaroon.getCaveatPackets(), MacaroonVersion.VERSION_1);
  }

  private static Macaroon deserializeStream(StatefulPacketReader packetReader) {
    String location = null;
    String identifier = null;
    List<CaveatPacket> caveats = new ArrayList<>( 3 );
    byte[] signature = null;

    for (Packet packet; (packet = readPacket(packetReader)) != null; ) {
      if (bytesStartWith(packet.data, LOCATION_BYTES)) {
        location = parsePacket(packet, LOCATION_BYTES);
      } else if (bytesStartWith(packet.data, IDENTIFIER_BYTES)) {
        identifier = parsePacket(packet, IDENTIFIER_BYTES);
      } else if (bytesStartWith(packet.data, CID_BYTES)) {
        String s = parsePacket(packet, CID_BYTES);
        caveats.add(new CaveatPacket(Type.cid, s));
      } else if (bytesStartWith(packet.data, CL_BYTES)) {
        String s = parsePacket(packet, CL_BYTES);
        caveats.add(new CaveatPacket(Type.cl, s));
      } else if (bytesStartWith(packet.data, VID_BYTES)) {
        byte[] raw = parseRawPacket(packet, VID_BYTES);
        caveats.add(new CaveatPacket(Type.vid, raw));
      } else if (bytesStartWith(packet.data, SIGNATURE_BYTES)) {
        signature = parseSignature(packet, SIGNATURE_BYTES);
      }
    }
    return new Macaroon(location, identifier, signature, caveats.toArray(new CaveatPacket[0]), MacaroonVersion.VERSION_1);
  }

  private static byte[] parseSignature(Packet packet, byte[] signaturePacketData) {
    int headerLen = signaturePacketData.length + KEY_VALUE_SEPARATOR_LEN;
    int len = Math.min(packet.data.length - headerLen, MacaroonsConstants.MACAROON_HASH_BYTES);
    byte[] signature = new byte[len];
    System.arraycopy(packet.data, headerLen, signature, 0, len);
    return signature;
  }

  private static String parsePacket(Packet packet, byte[] header) {
    int headerLen = header.length + KEY_VALUE_SEPARATOR_LEN;
    int len = packet.data.length - headerLen;
    if (packet.data[headerLen + len - 1] == LINE_SEPARATOR) len--;
    return new String(packet.data, headerLen, len, IDENTIFIER_CHARSET);
  }

  private static byte[] parseRawPacket(Packet packet, byte[] header) {
    int headerLen = header.length + KEY_VALUE_SEPARATOR_LEN;
    int len = packet.data.length - headerLen - LINE_SEPARATOR_LEN;
    byte[] raw = new byte[len];
    System.arraycopy(packet.data, headerLen, raw, 0, len);
    return raw;
  }

  private static boolean bytesStartWith(byte[] bytes, byte[] startBytes) {
    if (bytes.length < startBytes.length) return false;
    for (int i = 0, len = startBytes.length; i < len; i++) {
      if (bytes[i] != startBytes[i]) return false;
    }
    return true;
  }

  private static Packet readPacket(StatefulPacketReader stream) {
    if (stream.isEOF()) return null;
    if (!stream.isPacketHeaderAvailable()) {
      throw new NotDeSerializableException("Not enough header bytes available. Needed " + PACKET_PREFIX_LENGTH + " bytes.");
    }
    int size = stream.readPacketHeader();
    assert size <= PACKET_MAX_SIZE;

    byte[] data = new byte[size - PACKET_PREFIX_LENGTH];
    int read = stream.read(data);
    if (read < 0) return null;
    if (read != data.length) {
      throw new NotDeSerializableException("Not enough data bytes available. Needed " + data.length + " bytes, but was only " + read);
    }

    return new Packet(size, data);
  }

  private static class Packet {
    final int size;
    final byte[] data;

    private Packet(int size, byte[] data) {
      this.size = size;
      this.data = data;
    }
  }

  static class StatefulPacketReader {

    private final byte[] buffer;
    private int seekIndex = 0;

    public StatefulPacketReader(byte[] buffer) {
      this.buffer = buffer;
    }

    public int read(byte[] data) {
      int len = Math.min(data.length, buffer.length - seekIndex);
      if (len > 0) {
        System.arraycopy(buffer, seekIndex, data, 0, len);
        seekIndex += len;
        return len;
      }
      return -1;
    }

    public int readPacketHeader() {
      return (HEX_ALPHABET[buffer[seekIndex++]] << 12)
          | (HEX_ALPHABET[buffer[seekIndex++]] << 8)
          | (HEX_ALPHABET[buffer[seekIndex++]] << 4)
          | HEX_ALPHABET[buffer[seekIndex++]];
    }

    public boolean isPacketHeaderAvailable() {
      return seekIndex <= (buffer.length - PACKET_PREFIX_LENGTH);
    }

    public boolean isEOF() {
      return !(seekIndex < buffer.length);
    }
  }

}
