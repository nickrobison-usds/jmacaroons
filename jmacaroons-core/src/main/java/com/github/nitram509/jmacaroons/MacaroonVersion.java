package com.github.nitram509.jmacaroons;

public enum MacaroonVersion {

    VERSION_1,
    VERSION_2;

    public enum SerializationVersion {
        V1_BINARY,
        V1_JSON,
        V2_BINARY,
        V2_JSON
    }
}
