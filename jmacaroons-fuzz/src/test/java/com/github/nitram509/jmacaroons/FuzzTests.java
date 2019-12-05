package com.github.nitram509.jmacaroons;

import com.github.nitram509.jmacaroons.util.Base64;
import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(JQF.class)
public class FuzzTests {

    @Fuzz
    public void base64RoundTrip(String data) {
        final char[] b64 = Base64.encodeUrlSafe(data.getBytes(StandardCharsets.UTF_8));
        final String rounded = new String(Base64.decode(b64), StandardCharsets.UTF_8);
        assertEquals("Round trip should be equal", data, rounded);
    }

    @Fuzz
    public void macaroonSerialization(@From(MacaroonGenerator.class) MacaroonGenerator.MacaroonTester tester) {
        final Macaroon macaroon = tester.getMacaroon();
        final String serialized = MacaroonsSerializer.serialize(macaroon, tester.getVersion());
        final List<Macaroon> m2 = MacaroonsDeSerializer.deserialize(serialized);
        assertEquals("Round trip should be equal", macaroon, m2.get(0));
    }
}
