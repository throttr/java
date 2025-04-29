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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.awaitility.Awaitility.await;

import java.time.Duration;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ServiceTest
 */
class ServiceTest {

    private Service service;

    @BeforeEach
    void setUp() throws Exception {
        service = new Service("127.0.0.1", 9000, 2);
        service.connect();
    }

    @AfterEach
    void tearDown() {
        service.close();
    }

    @Test
    void shouldInsertAndQuerySuccessfully() throws Exception {
        String consumerId = "user:123";
        String resourceId = "/api/test";

        CompletableFuture<Object> insertFuture = service.send(new InsertRequest(
                5, 0, TTLType.SECONDS, 5, consumerId, resourceId
        ));
        FullResponse insert = (FullResponse) insertFuture.get();

        assertTrue(insert.allowed());

        CompletableFuture<Object> queryFuture = service.send(new QueryRequest(
                consumerId, resourceId
        ));
        FullResponse query = (FullResponse) queryFuture.get();

        assertTrue(query.allowed());
        assertTrue(query.quotaRemaining() >= 0);
        assertTrue(query.ttlRemaining() >= 0);
    }

    @Test
    void shouldConsumeQuotaViaInsertUsageAndDenyAfterExhausted() throws Exception {
        String consumerId = "user:consume-insert";
        String resourceId = "/api/consume-insert";

        service.send(new InsertRequest(
                2, 0, TTLType.SECONDS, 5, consumerId, resourceId
        )).get();

        FullResponse first = (FullResponse) service.send(new InsertRequest(
                0, 1, TTLType.SECONDS, 5, consumerId, resourceId
        )).get();
        assertTrue(first.allowed());

        FullResponse second = (FullResponse) service.send(new InsertRequest(
                0, 1, TTLType.SECONDS, 5, consumerId, resourceId
        )).get();
        assertTrue(second.allowed());

        FullResponse query = (FullResponse) service.send(new QueryRequest(
                consumerId, resourceId
        )).get();
        assertTrue(query.quotaRemaining() <= 0);
    }

    @Test
    void shouldConsumeQuotaViaUpdateDecreaseAndReachZero() throws Exception {
        String consumerId = "user:consume-update";
        String resourceId = "/api/consume-update";

        service.send(new InsertRequest(
                2, 0, TTLType.SECONDS, 5, consumerId, resourceId
        )).get();

        SimpleResponse firstUpdate = (SimpleResponse) service.send(new UpdateRequest(
                AttributeType.QUOTA, ChangeType.DECREASE, 1, consumerId, resourceId
        )).get();
        assertTrue(firstUpdate.success());

        SimpleResponse secondUpdate = (SimpleResponse) service.send(new UpdateRequest(
                AttributeType.QUOTA, ChangeType.DECREASE, 1, consumerId, resourceId
        )).get();
        assertTrue(secondUpdate.success());

        SimpleResponse thirdUpdate = (SimpleResponse) service.send(new UpdateRequest(
                AttributeType.QUOTA, ChangeType.DECREASE, 1, consumerId, resourceId
        )).get();
        assertTrue(thirdUpdate.success());

        FullResponse query = (FullResponse) service.send(new QueryRequest(
                consumerId, resourceId
        )).get();
        assertTrue(query.quotaRemaining() <= 0);
    }

    @Test
    void shouldPurgeAndFailToQueryAfterwards() throws Exception {
        String consumerId = "user:purge";
        String resourceId = "/api/purge";

        service.send(new InsertRequest(
                1, 0, TTLType.SECONDS, 5, consumerId, resourceId
        )).get();

        SimpleResponse purge = (SimpleResponse) service.send(new PurgeRequest(
                consumerId, resourceId
        )).get();
        assertTrue(purge.success());

        FullResponse query = (FullResponse) service.send(new QueryRequest(
                consumerId, resourceId
        )).get();
        assertFalse(query.allowed());
    }

    @Test
    void shouldResetQuotaAfterTTLExpiration() throws Exception {
        String consumerId = "user:ttl";
        String resourceId = "/api/ttl";

        service.send(new InsertRequest(
                1, 1, TTLType.SECONDS, 2, consumerId, resourceId
        )).get();

        FullResponse queryAfterInsert = (FullResponse) service.send(new QueryRequest(
                consumerId, resourceId
        )).get();
        assertTrue(queryAfterInsert.quotaRemaining() <= 0);

        await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(250))
                .until(() -> {
                    FullResponse response = (FullResponse) service.send(new QueryRequest(
                            consumerId, resourceId
                    )).get();
                    return !response.allowed();
                });
    }
}