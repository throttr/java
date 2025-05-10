package cl.throttr;

import cl.throttr.responses.SimpleResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleResponseTest {

    @Test
    void shouldParseTrueFromByteOne() {
        byte[] data = {1};
        SimpleResponse response = SimpleResponse.fromBytes(data);
        assertTrue(response.success());
    }

    @Test
    void shouldParseFalseFromByteZero() {
        byte[] data = {0};
        SimpleResponse response = SimpleResponse.fromBytes(data);
        assertFalse(response.success());
    }

    @Test
    void shouldThrowIfLengthIsZero() {
        byte[] data = {};
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> SimpleResponse.fromBytes(data)
        );
        assertEquals("Invalid SimpleResponse length: 0", ex.getMessage());
    }

    @Test
    void shouldThrowIfLengthGreaterThanOne() {
        byte[] data = {1, 0};
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> SimpleResponse.fromBytes(data)
        );
        assertEquals("Invalid SimpleResponse length: 2", ex.getMessage());
    }
}