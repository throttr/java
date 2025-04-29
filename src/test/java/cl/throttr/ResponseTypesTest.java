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
 * ResponseTypesTest
 */
class ResponseTypesTest {

    @Test
    void testFullResponseFromBytes() {
        byte[] fullResponseBytes = new byte[]{
                0x01,                              // allowed = true
                0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // quota_remaining = 4
                0x01,                              // ttl_type = 1 → Milliseconds
                0x10, 0x27, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00  // ttl_remaining = 10000
        };

        FullResponse response = FullResponse.fromBytes(fullResponseBytes);

        assertTrue(response.allowed());
        assertEquals(4L, response.quotaRemaining());
        assertEquals(TTLType.Milliseconds, response.ttlType());
        assertEquals(10000L, response.ttlRemaining());
    }

    @Test
    void testSimpleResponseFromBytes() {
        byte[] simpleResponseBytes = new byte[]{0x01}; // success = true

        SimpleResponse response = SimpleResponse.fromBytes(simpleResponseBytes);

        assertTrue(response.success());
    }
}