package io.mosip.certify.util;

import java.util.UUID;

public class UUIDGenerator {
    public String generate() {
        return "urn:uuid:" + UUID.randomUUID();
    }
}
