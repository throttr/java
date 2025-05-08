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

import cl.throttr.responses.SimpleResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ResponseTypesTest
 */
class ResponseTypesTest {
    @Test
    void testSimpleResponseFromBytes() {
        byte[] simpleResponseBytes = new byte[]{0x01}; // success = true

        SimpleResponse response = SimpleResponse.fromBytes(simpleResponseBytes);

        assertTrue(response.success());

        byte[] simpleResponseDeniedBytes = new byte[]{0x00}; // success = false

        SimpleResponse responseDenied = SimpleResponse.fromBytes(simpleResponseDeniedBytes);

        assertFalse(responseDenied.success());

        byte[] invalidBytes = new byte[]{0x01, 0x02}; // inválido: 2 bytes

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> SimpleResponse.fromBytes(invalidBytes)
        );

        assertTrue(exception.getMessage().contains("Invalid SimpleResponse length"));
    }
}