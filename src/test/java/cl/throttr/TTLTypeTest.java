package cl.throttr;

import cl.throttr.enums.TTLType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TTLTypeTest {
    @Test
    void shouldThrowIfInvalidByteGiven() {
        byte invalid = (byte) 99;
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> TTLType.fromByte(invalid)
        );
        assertTrue(ex.getMessage().contains("Invalid TTLType value"));
    }
}