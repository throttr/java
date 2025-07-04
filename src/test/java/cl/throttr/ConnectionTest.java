package cl.throttr;

import cl.throttr.enums.ValueSize;
import cl.throttr.requests.Serializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

class ConnectionTest {

    @Test
    void shouldThrowIfUnsupportedRequestTypeGiven() {
        Object invalidRequest = new Object(); // cualquier clase que no sea Insert/Query/etc.

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Serializer.invoke(invalidRequest, ValueSize.UINT16)
        );

        assertEquals("Unsupported request type", ex.getMessage());
    }

    @Test
    void shouldThrowIfNullRequestGiven() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Serializer.invoke(null, ValueSize.UINT16)
        );

        assertEquals("Unsupported request type", ex.getMessage());
    }

    @Test
    void shouldThrowIfConstructorFailsToConnect() {
        assertThrows(IOException.class, () -> {
            new Connection("localhost", 65535, ValueSize.UINT16);
        });
    }

    @Test
    void shouldThrowIfSocketIsClosedOnSend() throws Exception {
        Connection conn = new Connection("127.0.0.1", 9000, ValueSize.UINT16);
        conn.close();

        IOException ex = assertThrows(IOException.class, () -> conn.send(new Object()));
        assertEquals("Socket is already closed", ex.getMessage());
    }
}