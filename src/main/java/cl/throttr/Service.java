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
 * Service
 */
public class Service implements AutoCloseable {
    /**
     * Connections
     */
    private final List<Connection> connections = new ArrayList<>();

    /**
     * Round-robin index
     */
    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);

    /**
     * Host
     */
    private final String host;

    /**
     * Port
     */
    private final int port;

    /**
     * Max connections
     */
    private final int maxConnections;

    /**
     * Constructor
     *
     * @param host Host
     * @param port Port
     * @param maxConnections Max Connections
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
     * Connect
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
     * Send
     *
     * @param request Request
     * @return CompletableFuture<Response>
     */
    public CompletableFuture<Response> send(Request request) {
        if (connections.isEmpty()) {
            CompletableFuture<Response> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("No available connections."));
            return future;
        }

        int index = roundRobinIndex.getAndUpdate(i -> (i + 1) % connections.size());
        Connection conn = connections.get(index);
        return conn.send(request);
    }

    /**
     * Disconnect
     */
    @Override
    public void close() {
        for (Connection conn : connections) {
            try {
                conn.close();
            } catch (IOException e) {
                // Silent close
            }
        }
        connections.clear();
    }
}