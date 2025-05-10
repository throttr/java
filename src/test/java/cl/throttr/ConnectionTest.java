package cl.throttr;

import cl.throttr.enums.ValueSize;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionTest {

    @Test
    void shouldThrowIfUnsupportedRequestTypeGiven() {
        Object invalidRequest = new Object(); // cualquier clase que no sea Insert/Query/etc.

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Connection.getRequestBuffer(invalidRequest, ValueSize.UINT16)
        );

        assertEquals("Unsupported request type", ex.getMessage());
    }

    @Test
    void shouldThrowIfNullRequestGiven() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Connection.getRequestBuffer(null, ValueSize.UINT16)
        );

        assertEquals("Unsupported request type", ex.getMessage());
    }
}