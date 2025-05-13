package cl.throttr;

import cl.throttr.enums.TTLType;
import cl.throttr.responses.GetResponse;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GetResponseTest {

    @Test
    void shouldCompareContentCorrectly() {
        byte[] value1 = new byte[]{0x45, 0x48, 0x4C, 0x4F}; // EHLO
        byte[] value2 = new byte[]{0x45, 0x48, 0x4C, 0x4F}; // EHLO (otra instancia)
        byte[] different = new byte[]{0x48, 0x4F, 0x4C, 0x41}; // HOLA

        GetResponse r1 = new GetResponse(true, TTLType.SECONDS, 30, value1);
        GetResponse r2 = new GetResponse(true, TTLType.SECONDS, 30, value2);
        GetResponse r3 = new GetResponse(true, TTLType.SECONDS, 30, different);

        assertEquals(r1, r2);
        assertNotEquals(r1, r3);
    }

    @Test
    void shouldGenerateConsistentHashCode() {
        byte[] value = "EHLO".getBytes();
        GetResponse r1 = new GetResponse(true, TTLType.SECONDS, 30, value);
        GetResponse r2 = new GetResponse(true, TTLType.SECONDS, 30, "EHLO".getBytes());

        assertEquals(r1.hashCode(), r2.hashCode());

        Set<GetResponse> set = new HashSet<>();
        set.add(r1);
        assertTrue(set.contains(r2));
    }

    @Test
    void shouldPrintContentInToString() {
        byte[] value = "EHLO".getBytes();
        GetResponse response = new GetResponse(true, TTLType.SECONDS, 30, value);

        String printed = response.toString();
        assertTrue(printed.contains("success=true"));
        assertTrue(printed.contains("ttlType=SECONDS"));
        assertTrue(printed.contains("ttl=30"));
        assertTrue(printed.contains("value=[69, 72, 76, 79]")); // EHLO en decimal
    }
}