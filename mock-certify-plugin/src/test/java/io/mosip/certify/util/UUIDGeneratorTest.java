package io.mosip.certify.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UUIDGeneratorTest {
    @Test
    void shouldReturnUUIDInRequiredFormatWhenGenerated() {
        String generatedUUID = new UUIDGenerator().generate();

        assertTrue(generatedUUID.matches("^urn:uuid:[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}$"));
    }
}
