package com.github.nitram509.jmacaroons;

import com.namics.commons.random.RandomData;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import java.net.URI;

import static com.github.nitram509.jmacaroons.MacaroonsConstants.MACAROON_MAX_CAVEATS;

public class MacaroonGenerator extends Generator<MacaroonGenerator.MacaroonTester> {

    private static final MacaroonVersion.SerializationVersion[] versions = {MacaroonVersion.SerializationVersion.V1_BINARY, MacaroonVersion.SerializationVersion.V2_JSON};

    public MacaroonGenerator() {
        super(MacaroonTester.class);
    }

    @Override
    public MacaroonTester generate(SourceOfRandomness sourceOfRandomness, GenerationStatus generationStatus) {
        final URI location = RandomData.random(URI.class);
        final String rootKey = RandomData.randomString();
        final String identifier = RandomData.randomString();
        final Macaroon macaroon = generateRandomMacaroon(location, rootKey, identifier, sourceOfRandomness);
        final MacaroonVersion.SerializationVersion version = sourceOfRandomness.choose(versions);

        return new MacaroonTester(rootKey, macaroon, version);
    }

    public static Macaroon generateRandomMacaroon(URI location, String rootKey, String identifier, SourceOfRandomness sourceOfRandomness) {


        final Macaroon macaroon = MacaroonsBuilder.create(location.toString(), rootKey, identifier);

        final MacaroonsBuilder modifier = MacaroonsBuilder.modify(macaroon);

        final int caveats = sourceOfRandomness.nextInt(MACAROON_MAX_CAVEATS - 1);
        for (int i = 0; i < caveats; i = i + 3) {

            // Add a first party or third-party caveat?
            if (sourceOfRandomness.nextBoolean()) {
                modifier
                        .add_first_party_caveat(RandomData.randomString());
            } else {
                modifier
                        .add_third_party_caveat(RandomData.random(URI.class).toString(), RandomData.randomString(), RandomData.randomString());
            }
        }

        return modifier.getMacaroon();
    }

    public static class MacaroonTester {

        private final String rootKey;
        private final Macaroon macaroon;
        private final MacaroonVersion.SerializationVersion version;

        public MacaroonTester(String rootKey, Macaroon macaroon, MacaroonVersion.SerializationVersion version) {
            this.rootKey = rootKey;
            this.macaroon = macaroon;
            this.version = version;
        }

        public String getRootKey() {
            return rootKey;
        }

        public Macaroon getMacaroon() {
            return macaroon;
        }

        public MacaroonVersion.SerializationVersion getVersion() {
            return version;
        }
    }
}
