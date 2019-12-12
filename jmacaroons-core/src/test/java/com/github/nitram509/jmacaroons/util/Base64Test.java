package com.github.nitram509.jmacaroons.util;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.fest.assertions.Assertions.assertThat;

public class Base64Test {

  @DataProvider(name = "URL_safe_base64_strings_and_bytes")
  public static Object[][] URL_safe_base64_strings_and_bytes() {
    return new Object[][]{
        {"-A", (byte) 0xf8},
        {"-Q", (byte) 0xf9},
        {"-g", (byte) 0xfa},
        {"-w", (byte) 0xfb},
        {"_A", (byte) 0xfc},
        {"_Q", (byte) 0xfd},
        {"_g", (byte) 0xfe},
        {"_w", (byte) 0xff},
    };
  }

  @DataProvider(name = "regular_base64_strings_and_bytes")
  public static Object[][] regular_base64_strings_and_bytes() {
    return new Object[][]{
        {"+A", (byte) 0xf8},
        {"+Q", (byte) 0xf9},
        {"+g", (byte) 0xfa},
        {"+w", (byte) 0xfb},
        {"/A", (byte) 0xfc},
        {"/Q", (byte) 0xfd},
        {"/g", (byte) 0xfe},
        {"/w", (byte) 0xff},
    };
  }

  @DataProvider(name = "invalid_base64_strings")
  public static Object[][] invalid_base64_strings() {
    return new Object[][]{
            {""},
            {"ø"},
    };
  }

  @Test(dataProvider = "URL_safe_base64_strings_and_bytes")
  public void decoder_works_with_URL_safe_alphabet(String base64str, byte expected) {
    byte[] actual = Base64.decode(base64str);
    assertThat(actual).isEqualTo(new byte[]{expected});
    assertThat(Base64.decode(base64str.toCharArray())).isEqualTo(new byte[]{expected});
  }

  @Test
  public void encoder_works_with_empty_string() {
    final char[] actual = Base64.encodeUrlSafe(new byte[0]);
    assertThat(actual).isEmpty();
  }

  @Test(dataProvider = "regular_base64_strings_and_bytes")
  public void decoder_works_with_regular_alphabet(String base64str, byte expected) {
    byte[] actual = Base64.decode(base64str);
    assertThat(actual).isEqualTo(new byte[]{expected});
    assertThat(Base64.decode(base64str.toCharArray())).isEqualTo(new byte[]{expected});
  }

  @Test(dataProvider = "URL_safe_base64_strings_and_bytes")
  public void encoder_produces_URL_safe_bytes_without_padding(String expectedString, byte b) {
    String actual = Base64.encodeUrlSafeToString(new byte[]{b});
    assertThat(actual).isEqualTo(expectedString);
  }

  @Test(dataProvider = "URL_safe_base64_strings_and_bytes")
  public void encoder_produces_URL_safe_bytes_with_padding(String expectedString, byte b) {
    assertThat(Base64.encodeUrlSafe(new byte[]{b})).isEqualTo((expectedString + "==").toCharArray());
  }

  @Test(dataProvider = "invalid_base64_strings")
  public void decoder_works_with_invalid_input(String base64Str) {
    assertThat(Base64.decode(base64Str)).isEqualTo(new byte[]{});
  }
}