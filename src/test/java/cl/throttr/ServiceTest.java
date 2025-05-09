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
import cl.throttr.utils.Testing;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServiceTest {

    private Service service;

    @BeforeAll
    void setUp() throws Exception {
        ValueSize size = Testing.getValueSizeFromEnv();
        service = new Service("127.0.0.1", 9000, size, 1);
        service.connect();
    }

    @AfterAll
    void shutdown() throws IOException {
        service.close();
    }

    @Test
    void shouldBeProtocolCompliant() throws Exception {
        String key = UUID.randomUUID().toString();

        // INSERT with quota=7 and ttl=60
        SimpleResponse insert = (SimpleResponse) service.send(new InsertRequest(7, TTLType.SECONDS, 60, key));
        assertTrue(insert.success());

        // QUERY and validate
        FullResponse q1 = (FullResponse) service.send(new QueryRequest(key));
        assertTrue(q1.success());
        assertEquals(7, q1.quota());
        assertEquals(TTLType.SECONDS, q1.ttlType());
        assertTrue(q1.ttl() > 0 && q1.ttl() < 60);

        // UPDATE: DECREASE quota by 7
        SimpleResponse dec1 = (SimpleResponse) service.send(new UpdateRequest(AttributeType.QUOTA, ChangeType.DECREASE, 7, key));
        assertTrue(dec1.success());

        // UPDATE: DECREASE quota again -> should fail
        SimpleResponse dec2 = (SimpleResponse) service.send(new UpdateRequest(AttributeType.QUOTA, ChangeType.DECREASE, 7, key));
        assertFalse(dec2.success());

        // QUERY -> quota should be 0
        FullResponse q2 = (FullResponse) service.send(new QueryRequest(key));
        assertTrue(q2.success());
        assertEquals(0, q2.quota());

        // UPDATE: PATCH quota to 10
        SimpleResponse patchQuota = (SimpleResponse) service.send(new UpdateRequest(AttributeType.QUOTA, ChangeType.PATCH, 10, key));
        assertTrue(patchQuota.success());

        // QUERY -> quota should be 10
        FullResponse q3 = (FullResponse) service.send(new QueryRequest(key));
        assertEquals(10, q3.quota());

        // UPDATE: INCREASE quota by 20 -> should be 30
        SimpleResponse incQuota = (SimpleResponse) service.send(new UpdateRequest(AttributeType.QUOTA, ChangeType.INCREASE, 20, key));
        assertTrue(incQuota.success());

        // QUERY -> quota should be 30
        FullResponse q4 = (FullResponse) service.send(new QueryRequest(key));
        assertEquals(30, q4.quota());

        // UPDATE: INCREASE TTL by 60 -> ttl > 60 and < 120
        SimpleResponse incTtl = (SimpleResponse) service.send(new UpdateRequest(AttributeType.TTL, ChangeType.INCREASE, 60, key));
        assertTrue(incTtl.success());

        FullResponse q5 = (FullResponse) service.send(new QueryRequest(key));
        assertTrue(q5.ttl() > 60 && q5.ttl() < 120);

        // UPDATE: DECREASE TTL by 60 -> ttl < 60
        SimpleResponse decTtl = (SimpleResponse) service.send(new UpdateRequest(AttributeType.TTL, ChangeType.DECREASE, 60, key));
        assertTrue(decTtl.success());

        FullResponse q6 = (FullResponse) service.send(new QueryRequest(key));
        assertTrue(q6.ttl() > 0 && q6.ttl() < 60);

        // UPDATE: PATCH TTL to 90 -> ttl ~90
        SimpleResponse patchTtl = (SimpleResponse) service.send(new UpdateRequest(AttributeType.TTL, ChangeType.PATCH, 90, key));
        assertTrue(patchTtl.success());

        FullResponse q7 = (FullResponse) service.send(new QueryRequest(key));
        assertTrue(q7.ttl() > 60 && q7.ttl() <= 90);

        // PURGE
        SimpleResponse purge = (SimpleResponse) service.send(new PurgeRequest(key));
        assertTrue(purge.success());

        // RE-PURGE -> should fail
        SimpleResponse repurge = (SimpleResponse) service.send(new PurgeRequest(key));
        assertFalse(repurge.success());

        // QUERY -> should fail
        Awaitility.await().atMost(Duration.ofSeconds(2)).until(() -> !((SimpleResponse) service.send(new QueryRequest(key))).success());
    }
}
