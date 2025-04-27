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

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestTest {

    @Test
    void toBytesMatchesExpectedFormat() throws Exception {
        var ip = InetAddress.getByName("127.0.0.1");
        var request = new Request(ip, 9000, "/test", 5, 10000);

        byte[] actual = request.toBytes();

        byte[] expected = new byte[]{
                0x04,
                0x7f, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x28, 0x23,
                0x05, 0x00, 0x00, 0x00,
                0x10, 0x27, 0x00, 0x00,
                0x05,
                0x2f, 0x74, 0x65, 0x73, 0x74
        };

        assertArrayEquals(expected, actual);
    }

    @Test
    void testFromMethodCreatesCorrectRequest() throws UnknownHostException {
        String ipAddress = "127.0.0.1";
        int port = 8080;
        String url = "/test";
        int maxRequests = 5;
        long ttl = 10000;

        Request request = Request.from(ipAddress, port, url, maxRequests, ttl);

        assertNotNull(request);
        assertEquals(InetAddress.getByName(ipAddress), request.ip());
        assertEquals(port, request.port());
        assertEquals(url, request.url());
        assertEquals(maxRequests, request.max_requests());
        assertEquals(ttl, request.ttl());
    }
}