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

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConnectionTest
 */
class ConnectionTest {

    @Test
    void testProcessQueueWithEmptyQueue() throws Exception {
        Connection conn = new Connection("127.0.0.1", 9000);

        Method method = Connection.class.getDeclaredMethod("processQueue");
        method.setAccessible(true);

        assertDoesNotThrow(() -> {
            try {
                method.invoke(conn);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        conn.close();
    }
}