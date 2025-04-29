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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RequestTypesTest
 */
class RequestTypesTest {

    @Test
    void testInsertRequestToBytes() {
        InsertRequest request = new InsertRequest(
                5L, 0L, TTLType.SECONDS, 10000L, "user:123", "/api/test"
        );
        byte[] bytes = request.toBytes();
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        assertEquals((byte) RequestType.INSERT.getValue(), bytes[0]);
    }

    @Test
    void testQueryRequestToBytes() {
        QueryRequest request = new QueryRequest(
                "user:123", "/api/test"
        );
        byte[] bytes = request.toBytes();
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        assertEquals((byte) RequestType.QUERY.getValue(), bytes[0]);
    }

    @Test
    void testUpdateRequestToBytes() {
        UpdateRequest request = new UpdateRequest(
                AttributeType.QUOTA, ChangeType.DECREASE, 5L, "user:123", "/api/test"
        );
        byte[] bytes = request.toBytes();
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        assertEquals((byte) RequestType.UPDATE.getValue(), bytes[0]);
    }

    @Test
    void testPurgeRequestToBytes() {
        PurgeRequest request = new PurgeRequest(
                "user:123", "/api/test"
        );
        byte[] bytes = request.toBytes();
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        assertEquals((byte) RequestType.PURGE.getValue(), bytes[0]);
    }
}