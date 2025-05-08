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
import cl.throttr.requests.InsertRequest;
import cl.throttr.requests.PurgeRequest;
import cl.throttr.requests.QueryRequest;
import cl.throttr.requests.UpdateRequest;
import cl.throttr.utils.Testing;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RequestTypesTest
 */
class RequestTypesTest {

    @Test
    void testInsertRequestToBytes() {
        ValueSize size = Testing.getValueSizeFromEnv();
        InsertRequest request = new InsertRequest(
                5, TTLType.SECONDS, 30, "user:123"
        );
        byte[] bytes = request.toBytes(size);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        assertEquals((byte) RequestType.INSERT.getValue(), bytes[0]);
    }

    @Test
    void testQueryRequestToBytes() {
        QueryRequest request = new QueryRequest("user:123");
        byte[] bytes = request.toBytes();
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        assertEquals((byte) RequestType.QUERY.getValue(), bytes[0]);
    }

    @Test
    void testUpdateRequestToBytes() {
        ValueSize size = Testing.getValueSizeFromEnv();
        UpdateRequest request = new UpdateRequest(
                AttributeType.QUOTA, ChangeType.DECREASE, 5, "user:123"
        );
        byte[] bytes = request.toBytes(size);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        assertEquals((byte) RequestType.UPDATE.getValue(), bytes[0]);
    }

    @Test
    void testPurgeRequestToBytes() {
        PurgeRequest request = new PurgeRequest("user:123");
        byte[] bytes = request.toBytes();
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        assertEquals((byte) RequestType.PURGE.getValue(), bytes[0]);
    }
}