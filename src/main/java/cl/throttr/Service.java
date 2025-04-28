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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service class that manages multiple connections and handles sending requests to the Throttr server.
 * It uses round-robin load balancing across multiple connections.
 */
public class Service implements AutoCloseable {
    /**
     * List of connections to the Throttr server
     */
    private final List<Connection> connections = new ArrayList<>();

    /**
     * Atomic index for round-robin load balancing
     */
    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);

    /**
     * Host of the Throttr server
     */
    private final String host;

    /**
     * Port of the Throttr server
     */
    private final int port;

    /**
     * Maximum number of connections allowed to the Throttr server
     */
    private final int maxConnections;

    /**
     * Constructor for initializing the service with server host, port, and maximum connections.
     *
     * @param host Host address of the server
     * @param port Port number of the server
     * @param maxConnections Maximum number of concurrent connections to the server
     */
    public Service(String host, int port, int maxConnections) {
        if (maxConnections <= 0) {
            throw new IllegalArgumentException("maxConnections must be greater than 0.");
        }
        this.host = host;
        this.port = port;
        this.maxConnections = maxConnections;
    }

    /**
     * Establishes multiple connections to the Throttr server.
     *
     * @throws IOException If any connection fails
     */
    public void connect() throws IOException {
        for (int i = 0; i < maxConnections; i++) {
            Connection conn = new Connection(host, port);
            connections.add(conn);
        }
    }

    /**
     * Sends a request to the server. The request is distributed across the available connections using round-robin.
     *
     * @param request The request to send
     * @return CompletableFuture<Response> The future containing the response from the server
     */
    public CompletableFuture<Response> send(Request request) {
        if (connections.isEmpty()) {
            CompletableFuture<Response> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("No available connections."));
            return future;
        }

        // Round-robin logic to distribute requests across connections
        int index = roundRobinIndex.getAndUpdate(i -> (i + 1) % connections.size());
        Connection conn = connections.get(index);
        return conn.send(request);
    }

    /**
     * Closes all the connections and releases resources.
     */
    @Override
    public void close() {
        for (Connection conn : connections) {
            try {
                conn.close();
            } catch (IOException e) {
                // Silent close, ignore the exception
            }
        }
        connections.clear();
    }
}