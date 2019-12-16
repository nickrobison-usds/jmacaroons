package com.github.nitram509.jmacaroons.util;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

import static org.fest.assertions.Assertions.assertThat;

public class UTF8Test {

    @DataProvider(name = "UTF8_test_strings")
    public static Object[][] UTF8_test_strings() {
        return new Object[][]{
                {new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, true},
                {new byte[]{(byte) 0xEF, (byte) 0xB, (byte) 0xBF}, false},
                {new byte[]{(byte) 0xE1, (byte) 0xB, (byte) 0xBF}, false},
                {new byte[]{(byte) 0xEF, (byte) 0xF, (byte) 0xBF}, false},
                {new byte[]{(byte) 0xEF, (byte) 0xB, (byte) 0xC1}, false},
                {new byte[]{(byte) 0xEF, (byte) 0xBF}, false},
                {new byte[]{(byte) 0xEF}, false},
                {new byte[]{(byte) 0xEF, (byte) 0xB}, false},
                {new byte[]{}, true},
                {"\\xf0\\x28\\x8c\\xbc🤓".getBytes(StandardCharsets.ISO_8859_1), true},
                {"OLsóS«\u0090\u0007Fµ«\u0003ý×hJ&\u0017,Ä\u0015\u0097#".getBytes(StandardCharsets.ISO_8859_1), false},
                {" ×".getBytes(StandardCharsets.ISO_8859_1), false},
                {"ä".getBytes(StandardCharsets.ISO_8859_1), false},
                {"".getBytes(StandardCharsets.ISO_8859_1), true},
                {"Mark".getBytes(StandardCharsets.ISO_8859_1), true},
                {"\u1234".getBytes(StandardCharsets.ISO_8859_1), true},
        };
    }

    @Test(dataProvider = "UTF8_test_strings")
    void test_utf8_detector(byte[] input, boolean validUTF8) {
        assertThat(UTF8.validUTF8(input)).isEqualTo(validUTF8);
    }

}
