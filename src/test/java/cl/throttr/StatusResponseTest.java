package cl.throttr;

import cl.throttr.responses.StatusResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusResponseTest {

    @Test
    void shouldParseTrueFromByteOne() {
        byte[] data = {1};
        StatusResponse response = StatusResponse.fromBytes(data);
        assertTrue(response.success());
    }

    @Test
    void shouldParseFalseFromByteZero() {
        byte[] data = {0};
        StatusResponse response = StatusResponse.fromBytes(data);
        assertFalse(response.success());
    }

    @Test
    void shouldThrowIfLengthIsZero() {
        byte[] data = {};
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> StatusResponse.fromBytes(data)
        );
        assertEquals("Invalid SimpleResponse length: 0", ex.getMessage());
    }

    @Test
    void shouldThrowIfLengthGreaterThanOne() {
        byte[] data = {1, 0};
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> StatusResponse.fromBytes(data)
        );
        assertEquals("Invalid SimpleResponse length: 2", ex.getMessage());
    }
}