// Copyright (C) 2025 Ian Torres
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program. If not, see <https://www.gnu.org/licenses/>.


package cl.throttr;

import cl.throttr.enums.*;
import cl.throttr.requests.*;
import cl.throttr.responses.*;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ServiceTest {

    @Test
    void shouldBeProtocolCompliant() throws Exception {
        ValueSize sized = ValueSize.UINT8;
        String size = System.getenv().getOrDefault("THROTTR_SIZE", "uint16");
        if ("uint16".equals(size)) sized = ValueSize.UINT16;
        if ("uint32".equals(size)) sized = ValueSize.UINT32;
        if ("uint64".equals(size)) sized = ValueSize.UINT64;

        Service service = new Service("127.0.0.1", 9000, sized, 1);
        service.connect();

        String key = UUID.randomUUID().toString();

        // INSERT with quota=7 and ttl=60
        StatusResponse insert = (StatusResponse) service.send(new InsertRequest(7, TTLType.SECONDS, 60, key));
        assertTrue(insert.success());

        // QUERY and validate
        QueryResponse q1 = (QueryResponse) service.send(new QueryRequest(key));
        assertTrue(q1.success());
        assertEquals(7, q1.quota());
        assertEquals(TTLType.SECONDS, q1.ttlType());
        assertTrue(q1.ttl() > 0 && q1.ttl() < 60);

        // UPDATE: DECREASE quota by 7
        StatusResponse dec1 = (StatusResponse) service.send(new UpdateRequest(AttributeType.QUOTA, ChangeType.DECREASE, 7, key));
        assertTrue(dec1.success());

        // UPDATE: DECREASE quota again -> should fail
        StatusResponse dec2 = (StatusResponse) service.send(new UpdateRequest(AttributeType.QUOTA, ChangeType.DECREASE, 7, key));
        assertFalse(dec2.success());

        // QUERY -> quota should be 0
        QueryResponse q2 = (QueryResponse) service.send(new QueryRequest(key));
        assertTrue(q2.success());
        assertEquals(0, q2.quota());

        // UPDATE: PATCH quota to 10
        StatusResponse patchQuota = (StatusResponse) service.send(new UpdateRequest(AttributeType.QUOTA, ChangeType.PATCH, 10, key));
        assertTrue(patchQuota.success());

        // QUERY -> quota should be 10
        QueryResponse q3 = (QueryResponse) service.send(new QueryRequest(key));
        assertEquals(10, q3.quota());

        // UPDATE: INCREASE quota by 20 -> should be 30
        StatusResponse incQuota = (StatusResponse) service.send(new UpdateRequest(AttributeType.QUOTA, ChangeType.INCREASE, 20, key));
        assertTrue(incQuota.success());

        // QUERY -> quota should be 30
        QueryResponse q4 = (QueryResponse) service.send(new QueryRequest(key));
        assertEquals(30, q4.quota());

        // UPDATE: INCREASE TTL by 60 -> ttl > 60 and < 120
        StatusResponse incTtl = (StatusResponse) service.send(new UpdateRequest(AttributeType.TTL, ChangeType.INCREASE, 60, key));
        assertTrue(incTtl.success());

        QueryResponse q5 = (QueryResponse) service.send(new QueryRequest(key));
        assertTrue(q5.ttl() > 60 && q5.ttl() < 120);

        // UPDATE: DECREASE TTL by 60 -> ttl < 60
        StatusResponse decTtl = (StatusResponse) service.send(new UpdateRequest(AttributeType.TTL, ChangeType.DECREASE, 60, key));
        assertTrue(decTtl.success());

        QueryResponse q6 = (QueryResponse) service.send(new QueryRequest(key));
        assertTrue(q6.ttl() > 0 && q6.ttl() < 60);

        // UPDATE: PATCH TTL to 90 -> ttl ~90
        StatusResponse patchTtl = (StatusResponse) service.send(new UpdateRequest(AttributeType.TTL, ChangeType.PATCH, 90, key));
        assertTrue(patchTtl.success());

        QueryResponse q7 = (QueryResponse) service.send(new QueryRequest(key));
        assertTrue(q7.ttl() > 60 && q7.ttl() <= 90);

        // PURGE
        StatusResponse purge = (StatusResponse) service.send(new PurgeRequest(key));
        assertTrue(purge.success());

        // RE-PURGE -> should fail
        Awaitility.await().atMost(Duration.ofSeconds(2)).until(() -> !((StatusResponse) service.send(new PurgeRequest(key))).success());

        // QUERY -> should fail
        Awaitility.await().atMost(Duration.ofSeconds(2)).until(() -> !((QueryResponse) service.send(new QueryRequest(key))).success());

        service.close();
    }

    @Test
    void shouldThrowIfMaxConnectionsIsZero() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Service("127.0.0.1", 9000, ValueSize.UINT16, 0)
        );
        assertEquals("maxConnections must be greater than 0.", ex.getMessage());
    }

    @Test
    void shouldThrowIfSendCalledWithoutConnect() {
        var service = new Service("127.0.0.1", 9000, ValueSize.UINT16, 1);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.send(new Object())); // NOSONAR

        assertEquals("There are no available connections.", ex.getMessage());
    }
}
