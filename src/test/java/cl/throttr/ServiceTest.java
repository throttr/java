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

import cl.throttr.enums.AttributeType;
import cl.throttr.enums.ChangeType;
import cl.throttr.enums.TTLType;
import cl.throttr.enums.ValueSize;
import cl.throttr.requests.InsertRequest;
import cl.throttr.requests.PurgeRequest;
import cl.throttr.requests.QueryRequest;
import cl.throttr.requests.UpdateRequest;
import cl.throttr.responses.FullResponse;
import cl.throttr.responses.SimpleResponse;
import cl.throttr.utils.Testing;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.time.Duration;


import static org.junit.jupiter.api.Assertions.*;

/**
 * ServiceTest
 */
class ServiceTest {

    static private Service service;

    @BeforeAll
    static void setUp() throws Exception {
        ValueSize size = Testing.getValueSizeFromEnv();
        service = new Service("127.0.0.1", 9000, size,1);
        service.connect();
    }

    @AfterAll
    static void shutdown() throws IOException {
        service.close();
    }

    @Test
    void shouldInsertAndQuerySuccessfully() throws Exception {
        String key = "user:1234";

        SimpleResponse insert = (SimpleResponse) service.send(new InsertRequest(
                5, TTLType.SECONDS, 5, key
        ));

        assertTrue(insert.success());

        FullResponse query = (FullResponse) service.send(new QueryRequest(
                key
        ));

        assertTrue(query.success());
        assertTrue(query.quota() >= 0);
        assertTrue(query.ttl() >= 0);

        SimpleResponse purge = (SimpleResponse) service.send(new PurgeRequest(
                key
        ));
        assertTrue(purge.success());
    }

    @Test
    void shouldConsumeQuotaViaInsertUsageAndDenyAfterExhausted() throws Exception {
        String key = "user:consume-insert";

        service.send(new InsertRequest(
                2, TTLType.SECONDS, 5, key
        ));

        SimpleResponse first = (SimpleResponse) service.send(new InsertRequest(
                0, TTLType.SECONDS, 5, key
        ));
        assertFalse(first.success());

        FullResponse query = (FullResponse) service.send(new QueryRequest(
                key
        ));
        assertTrue(query.quota() <= 2);

        SimpleResponse purge = (SimpleResponse) service.send(new PurgeRequest(
                key
        ));
        assertTrue(purge.success());
    }

    @Test
    void shouldConsumeQuotaViaUpdateDecreaseAndReachZero() throws Exception {
        String key = "user:consume-update";

        service.send(new InsertRequest(
                2, TTLType.SECONDS, 5, key
        ));

        SimpleResponse firstUpdate = (SimpleResponse) service.send(new UpdateRequest(
                AttributeType.QUOTA, ChangeType.DECREASE, 1, key
        ));
        assertTrue(firstUpdate.success());

        SimpleResponse secondUpdate = (SimpleResponse) service.send(new UpdateRequest(
                AttributeType.QUOTA, ChangeType.DECREASE, 1, key
        ));
        assertTrue(secondUpdate.success());

        SimpleResponse thirdUpdate = (SimpleResponse) service.send(new UpdateRequest(
                AttributeType.QUOTA, ChangeType.DECREASE, 1, key
        ));
        assertFalse(thirdUpdate.success());

        FullResponse query = (FullResponse) service.send(new QueryRequest(
                key
        ));
        assertTrue(query.quota() <= 0);

        SimpleResponse purge = (SimpleResponse) service.send(new PurgeRequest(
                key
        ));
        assertTrue(purge.success());
    }

    @Test
    void shouldPurgeAndFailToQueryAfterwards() throws Exception {
        String key = "user:purge-3";

        service.send(new InsertRequest(
                1, TTLType.SECONDS, 5, key
        ));

        SimpleResponse purge = (SimpleResponse) service.send(new PurgeRequest(
                key
        ));
        assertTrue(purge.success());

        SimpleResponse query = (SimpleResponse) service.send(new QueryRequest(
                key
        ));
        assertFalse(query.success());
    }

    @Test
    void shouldAllTheFlowWorksAsExpected() throws Exception {
        String key = "user:purge-2";

        SimpleResponse insertResponse = (SimpleResponse) service.send(new InsertRequest(
                10, TTLType.SECONDS, 30, key
        ));
        assertTrue(insertResponse.success());

        FullResponse queryResponse1 = (FullResponse) service.send(new QueryRequest(
                key
        ));
        assertTrue(queryResponse1.success());
        assertEquals(10, queryResponse1.quota());

        SimpleResponse updateResponse = (SimpleResponse) service.send(new UpdateRequest(
                AttributeType.QUOTA, ChangeType.DECREASE, 5, key
        ));
        assertTrue(updateResponse.success());

        FullResponse queryResponse2 = (FullResponse) service.send(new QueryRequest(
                key
        ));
        assertTrue(queryResponse2.success());
        assertEquals(5, queryResponse2.quota());

        SimpleResponse purgeResponse = (SimpleResponse) service.send(new PurgeRequest(
                key
        ));
        assertTrue(purgeResponse.success());

        SimpleResponse queryResponse3 = (SimpleResponse) service.send(new QueryRequest(
                key
        ));
        assertFalse(queryResponse3.success());
    }
}