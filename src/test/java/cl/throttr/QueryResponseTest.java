package cl.throttr;

import cl.throttr.enums.TTLType;
import cl.throttr.enums.ValueSize;
import cl.throttr.responses.QueryResponse;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

class QueryResponseTest {

    @Test
    void shouldParseValidQueryResponseWithSuccessTrue() {
        ByteBuffer buffer = ByteBuffer.allocate(1 + 2 + 1 + 2).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) 1);                // success
        buffer.putShort((short) 1234);       // quota
        buffer.put((byte) 4);                // TTLType.SECONDS
        buffer.putShort((short) 5678);       // ttl

        QueryResponse response = QueryResponse.fromBytes(buffer.array(), ValueSize.UINT16);

        assertTrue(response.success());
        assertEquals(1234, response.quota());
        assertEquals(TTLType.SECONDS, response.ttlType());
        assertEquals(5678, response.ttl());
    }

    @Test
    void shouldParseValidQueryResponseWithSuccessFalse() {
        ByteBuffer buffer = ByteBuffer.allocate(1 + 2 + 1 + 2).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) 0);                // success
        buffer.putShort((short) 4321);       // quota
        buffer.put((byte) 3);                // TTLType.MILLISECONDS
        buffer.putShort((short) 8765);       // ttl

        QueryResponse response = QueryResponse.fromBytes(buffer.array(), ValueSize.UINT16);

        assertFalse(response.success());
        assertEquals(4321, response.quota());
        assertEquals(TTLType.MILLISECONDS, response.ttlType());
        assertEquals(8765, response.ttl());
    }

    @Test
    void shouldThrowIfLengthIsInvalid() {
        byte[] data = new byte[5]; // intentionally incorrect size

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> QueryResponse.fromBytes(data, ValueSize.UINT16)
        );

        assertEquals("Invalid QueryResponse length: 5", ex.getMessage());
    }
}