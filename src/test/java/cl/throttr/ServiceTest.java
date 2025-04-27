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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
    void testSendSingleRequest() throws Exception {
        Request request = new Request(
                InetAddress.getByName("127.0.0.1"),
                8080,
                "/test",
                5,
                10000
        );

        CompletableFuture<Response> future = service.send(request);
        Response response = future.get(); // Espera respuesta

        assertNotNull(response);
        assertTrue(response.can());
        assertTrue(response.available_requests() >= 0);
        assertTrue(response.ttl() >= 0);
    }

    @Test
    void testRoundRobinBehavior() throws Exception {
        Request request1 = new Request(
                InetAddress.getByName("127.0.0.1"),
                8080,
                "/one",
                5,
                10000
        );

        Request request2 = new Request(
                InetAddress.getByName("127.0.0.1"),
                8080,
                "/two",
                5,
                10000
        );

        CompletableFuture<Response> future1 = service.send(request1);
        CompletableFuture<Response> future2 = service.send(request2);

        Response response1 = future1.get();
        Response response2 = future2.get();

        assertNotNull(response1);
        assertNotNull(response2);

        assertTrue(response1.can());
        assertTrue(response2.can());
    }

    @Test
    void testSendWithoutConnections() throws UnknownHostException {
        Service emptyService = new Service("127.0.0.1", 9000, 1);
        Request request = new Request(
                InetAddress.getByName("127.0.0.1"),
                8080,
                "/fail",
                5,
                10000
        );

        CompletableFuture<Response> future = emptyService.send(request);

        assertThrows(ExecutionException.class, future::get);
    }


    @Test
    void testServiceConstructorThrowsExceptionOnInvalidMaxConnections() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Service("127.0.0.1", 9000, 0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Service("127.0.0.1", 9000, -5);
        });
    }
}