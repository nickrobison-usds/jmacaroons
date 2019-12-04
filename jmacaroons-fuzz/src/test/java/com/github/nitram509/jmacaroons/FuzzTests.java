package com.github.nitram509.jmacaroons;

import com.github.nitram509.jmacaroons.util.Base64;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

@RunWith(JQF.class)
public class FuzzTests {

    @Fuzz
    public void base64RoundTrip (String data) {
        final char[] b64 = Base64.encodeUrlSafe(data.getBytes(StandardCharsets.UTF_8));
        final String rounded = new String(Base64.decode(b64), StandardCharsets.UTF_8);
        assertEquals("Round trip should be equal", data, rounded);
    }
}
