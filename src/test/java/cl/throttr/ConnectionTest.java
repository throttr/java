package cl.throttr;

import cl.throttr.enums.ValueSize;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static org.mockito.Mockito.*;

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

    @Test
    void shouldThrowIfConstructorFailsToConnect() {
        assertThrows(IOException.class, () -> {
            new Connection("localhost", 65535, ValueSize.UINT16);
        });
    }

    @Test
    void shouldThrowIfSocketIsClosedOnSend() throws Exception {
        // Mock socket cerrado
        Socket mockSocket = mock(Socket.class);
        when(mockSocket.isClosed()).thenReturn(true);
        when(mockSocket.getInputStream()).thenReturn(mock(InputStream.class));
        when(mockSocket.getOutputStream()).thenReturn(mock(OutputStream.class));

        Connection conn = new Connection("localhost", 1, ValueSize.UINT16) {
            @Override
            public Object send(Object request) throws IOException {
                if (mockSocket.isClosed()) {
                    throw new IOException("Socket is already closed");
                }
                return null;
            }
        };

        IOException ex = assertThrows(IOException.class, () -> conn.send(new Object()));
        assertEquals("Socket is already closed", ex.getMessage());
    }
}