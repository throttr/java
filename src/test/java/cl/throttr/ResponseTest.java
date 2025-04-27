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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResponseTest {

    @Test
    void fromBytesParsesCorrectly() {
        byte[] responseBytes = new byte[]{
                0x01,
                0x04, 0x00, 0x00, 0x00,
                0x10, 0x27, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        };

        var response = Response.fromBytes(responseBytes);

        assertTrue(response.can());
        assertEquals(4, response.available_requests());
        assertEquals(10000, response.ttl());
    }
}