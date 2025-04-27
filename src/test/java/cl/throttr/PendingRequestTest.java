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

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ServiceTest
 */
class PendingRequestTest {

    @Test
    void testBufferAndFutureAccessors() {
        byte[] buffer = {0x01, 0x02, 0x03};
        CompletableFuture<Response> future = new CompletableFuture<>();

        var pendingRequest = new PendingRequest(buffer, future);

        assertArrayEquals(buffer, pendingRequest.buffer());
        assertEquals(future, pendingRequest.future());
    }


    @Test
    void testEqualsAndHashCode() {
        byte[] buffer1 = {0x01, 0x02, 0x03};
        byte[] buffer2 = {0x01, 0x02, 0x03};
        CompletableFuture<Response> future = new CompletableFuture<>();

        PendingRequest pending1 = new PendingRequest(buffer1, future);
        PendingRequest pending2 = new PendingRequest(buffer2, future);

        assertEquals(pending1, pending2);
        assertEquals(pending1.hashCode(), pending2.hashCode());
    }

    @Test
    void testNotEqualsDifferentBuffer() {
        byte[] buffer1 = {0x01, 0x02, 0x03};
        byte[] buffer2 = {0x04, 0x05, 0x06};
        CompletableFuture<Response> future = new CompletableFuture<>();

        PendingRequest pending1 = new PendingRequest(buffer1, future);
        PendingRequest pending2 = new PendingRequest(buffer2, future);

        assertNotEquals(pending1, pending2);
    }

    @Test
    void testToStringIsNotNull() {
        byte[] buffer = {0x01, 0x02, 0x03};
        CompletableFuture<Response> future = new CompletableFuture<>();

        PendingRequest pending = new PendingRequest(buffer, future);

        assertNotNull(pending.toString());
        assertTrue(pending.toString().contains("buffer"));
    }
}