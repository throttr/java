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
}