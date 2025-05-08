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

import cl.throttr.enums.RequestType;
import cl.throttr.requests.PendingRequest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PendingRequestTest
 */
class PendingRequestTest {

    @Test
    void testAccessors() {
        byte[] buffer = {0x01, 0x02, 0x03};
        CompletableFuture<Object> future = new CompletableFuture<>();
        boolean expectFullResponse = true;

        PendingRequest pendingRequest = new PendingRequest(buffer, future, expectFullResponse, RequestType.QUERY);

        assertArrayEquals(buffer, pendingRequest.getBuffer());
        assertEquals(future, pendingRequest.getFuture());
        assertTrue(pendingRequest.isExpectFullResponse());
    }
}