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

import java.time.Duration;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ServiceTest
 */
@Execution(ExecutionMode.SAME_THREAD)
class ServiceTest {

    private static Service service;

    @BeforeAll
    static void setUp() throws Exception {
        ValueSize size = Testing.getValueSizeFromEnv();
        service = new Service("127.0.0.1", 9000, size,1);
        service.connect();
        Thread.sleep(1000);
    }

    @AfterAll
    static void shutdown() {
    }

    @Test
    void shouldInsertAndQuerySuccessfully() throws Exception {
        String key = "user:1234";

        CompletableFuture<Object> insertFuture = service.send(new InsertRequest(
                5, TTLType.SECONDS, 5, key
        ));
        SimpleResponse insert = (SimpleResponse) insertFuture.get();

        assertTrue(insert.success());

        CompletableFuture<Object> queryFuture = service.send(new QueryRequest(
                key
        ));
        FullResponse query = (FullResponse) queryFuture.get();

        assertTrue(query.success());
        assertTrue(query.quota() >= 0);
        assertTrue(query.ttl() >= 0);
    }

    @Test
    void shouldConsumeQuotaViaInsertUsageAndDenyAfterExhausted() throws Exception {
        String key = "user:consume-insert";

        service.send(new InsertRequest(
                2, TTLType.SECONDS, 5, key
        )).get();

        SimpleResponse first = (SimpleResponse) service.send(new InsertRequest(
                0, TTLType.SECONDS, 5, key
        )).get();
        assertFalse(first.success());

        FullResponse query = (FullResponse) service.send(new QueryRequest(
                key
        )).get();
        assertTrue(query.quota() <= 2);
    }

    @Test
    void shouldConsumeQuotaViaUpdateDecreaseAndReachZero() throws Exception {
        String key = "user:consume-update";

        SimpleResponse insert = (SimpleResponse) service.send(new InsertRequest(
                2, TTLType.SECONDS, 5, key
        )).get();

        SimpleResponse firstUpdate = (SimpleResponse) service.send(new UpdateRequest(
                AttributeType.QUOTA, ChangeType.DECREASE, 1, key
        )).get();
        assertTrue(firstUpdate.success());

        SimpleResponse secondUpdate = (SimpleResponse) service.send(new UpdateRequest(
                AttributeType.QUOTA, ChangeType.DECREASE, 1, key
        )).get();
        assertTrue(secondUpdate.success());

        SimpleResponse thirdUpdate = (SimpleResponse) service.send(new UpdateRequest(
                AttributeType.QUOTA, ChangeType.DECREASE, 1, key
        )).get();
        assertFalse(thirdUpdate.success());

        FullResponse query = (FullResponse) service.send(new QueryRequest(
                key
        )).get();
        assertTrue(query.quota() <= 0);
    }

    @Test
    void shouldPurgeAndFailToQueryAfterwards() throws Exception {
        String key = "user:purge-3";

        SimpleResponse insert = (SimpleResponse) service.send(new InsertRequest(
                1, TTLType.SECONDS, 5, key
        )).get();

        SimpleResponse purge = (SimpleResponse) service.send(new PurgeRequest(
                key
        )).get();
        assertTrue(purge.success());

        FullResponse query = (FullResponse) service.send(new QueryRequest(
                key
        )).get();
        assertFalse(query.success());
    }

    @Test
    void shouldResetQuotaAfterTTLExpiration() throws Exception {
        String key = "user:ttl";

        service.send(new InsertRequest(
                2, TTLType.SECONDS, 2, key
        )).get();

        FullResponse queryAfterInsert = (FullResponse) service.send(new QueryRequest(
                key
        )).get();
        assertTrue(queryAfterInsert.quota() <= 2);

        await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(250))
                .until(() -> {
                    FullResponse response = (FullResponse) service.send(new QueryRequest(
                            key
                    )).get();
                    return !response.success();
                });
    }

    @Test
    void shouldAllTheFlowWorksAsExpected() throws Exception {
        String key = "user:purge-2";

        SimpleResponse insertResponse = (SimpleResponse) service.send(new InsertRequest(
                10, TTLType.SECONDS, 30, key
        )).get();
        assertTrue(insertResponse.success());

        FullResponse queryResponse1 = (FullResponse) service.send(new QueryRequest(
                key
        )).get();
        assertTrue(queryResponse1.success());
        assertEquals(10, queryResponse1.quota());

        SimpleResponse updateResponse = (SimpleResponse) service.send(new UpdateRequest(
                AttributeType.QUOTA, ChangeType.DECREASE, 5, key
        )).get();
        assertTrue(updateResponse.success());

        FullResponse queryResponse2 = (FullResponse) service.send(new QueryRequest(
                key
        )).get();
        assertTrue(queryResponse2.success());
        assertEquals(5L, queryResponse2.quota());

        SimpleResponse purgeResponse = (SimpleResponse) service.send(new PurgeRequest(
                key
        )).get();
        assertTrue(purgeResponse.success());

        FullResponse queryResponse3 = (FullResponse) service.send(new QueryRequest(
                key
        )).get();
        assertFalse(queryResponse3.success());
    }

    @Test
    void shouldThrowExceptionWhenMaxConnectionsIsZero() {
        ValueSize size = Testing.getValueSizeFromEnv();
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Service("127.0.0.1", 9000, size, 0)
        );

        assertEquals("maxConnections must be greater than 0.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenMaxConnectionsIsNegative() {
        ValueSize size = Testing.getValueSizeFromEnv();
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Service("127.0.0.1", 9000, size, -5)
        );

        assertEquals("maxConnections must be greater than 0.", exception.getMessage());
    }

    @Test
    void shouldFailWhenNoConnectionsAvailable() {
        ValueSize size = Testing.getValueSizeFromEnv();
        Service local = new Service("127.0.0.1", 9000, size, 1);

        CompletableFuture<Object> future = local.send(new InsertRequest(
                5, TTLType.SECONDS, 5, "user:no-connection"
        ));

        ExecutionException exception = assertThrows(
                ExecutionException.class,
                future::get
        );

        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertEquals("No available connections.", exception.getCause().getMessage());
    }
}