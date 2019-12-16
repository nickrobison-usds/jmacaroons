package com.github.nitram509.jmacaroons.util;

public class UTF8 {

    private UTF8() {
        // Not used
    }

    /**
     * Determines whether or not the given byte array contains only valid UTF-8 characters.
     * Used to determine if an input needs to be base64 encoded, or not.
     * Snagged from this StackOverflow answer: https://stackoverflow.com/questions/887148/how-to-determine-if-a-string-contains-invalid-encoded-characters
     *
     * @param input - {@link byte[]} input string to check
     * @return - {@code true} String is only UTF-8 characters. {@code false} String contains non-UTF8 characters
     */
    public static boolean validUTF8(byte[] input) {
      int i = 0;
      // Check for BOM
      if (input.length >= 3)
        if ((input[0] & 0xFF) == 0xEF)
          if ((input[1] & 0xFF) == 0xBB)
            if ((input[2] & 0xFF) == 0xBF) {
              i = 3;
            }

      int end;
      for (int j = input.length; i < j; ++i) {
        int octet = input[i];
        if ((octet & 0x80) == 0) {
          continue; // ASCII
        }

        // Check for UTF-8 leading byte
        if ((octet & 0xE0) == 0xC0) {
          end = i + 1;
        } else if ((octet & 0xF0) == 0xE0) {
          end = i + 2;
        } else if ((octet & 0xF8) == 0xF0) {
          end = i + 3;
        } else {
          // Java only supports BMP so 3 is max
          return false;
        }

        // Ensure we have enough bytes left for decoding
        if (j <= end) {
            return false;
        }

        while (i < end) {
          i++;
          octet = input[i];
          if ((octet & 0xC0) != 0x80) {
            // Not a valid trailing byte
            return false;
          }
        }
      }
      return true;
    }
}
