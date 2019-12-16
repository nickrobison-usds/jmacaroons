package com.github.nitram509.jmacaroons.util;

import com.github.nitram509.jmacaroons.GeneralCaveatVerifier;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.fest.assertions.Assertions.assertThat;

public class ArrayToolsTest {

    @Test
    public void test_array_append() {
        List<GeneralCaveatVerifier> verifiers = new ArrayList<>();
        verifiers.add(new TestVerifier("v1"));
        verifiers.add(new TestVerifier("v2"));
        final GeneralCaveatVerifier addedVerifier = new TestVerifier("v3");
        final GeneralCaveatVerifier[] appendedArray = ArrayTools.appendToArray(verifiers.toArray(new GeneralCaveatVerifier[0]), addedVerifier);
        verifiers.add(addedVerifier);
        assertThat(Arrays.asList(appendedArray)).isEqualTo(verifiers);
    }

    public static class TestVerifier implements GeneralCaveatVerifier {

        private final String name;

        TestVerifier(String name) {
            this.name = name;
        }

        @Override
        public boolean verifyCaveat(String caveat) {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestVerifier that = (TestVerifier) o;
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}
