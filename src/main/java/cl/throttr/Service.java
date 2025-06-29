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

import cl.throttr.enums.ValueSize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
     * Value size
     */
    private final ValueSize size;

    /**
     * Maximum connections
     */
    private final int maxConnections;

    /**
     * Constructor
     *
     * @param host The Throttr remote address
     * @param port The Throttr remote port
     * @param maxConnections Number of pooled connections
     */
    public Service(String host, int port, ValueSize size, int maxConnections) {
        if (maxConnections <= 0) {
            throw new IllegalArgumentException("maxConnections must be greater than 0.");
        }
        this.host = host;
        this.port = port;
        this.size = size;
        this.maxConnections = maxConnections;
    }

    /**
     * Connect
     *
     * @throws IOException Sockets can fail
     */
    public void connect() throws IOException {
        for (int i = 0; i < maxConnections; i++) {
            Connection conn = new Connection(host, port, size);
            connections.add(conn);
        }
    }

    /**
     * Send
     *
     * @param request Requests
     * @return Object
     */
    public Object send(Object request) throws IOException {
        if (connections.isEmpty()) {
            throw new IllegalStateException("There are no available connections.");
        }
        int index = roundRobinIndex.getAndUpdate(i -> (i + 1) % connections.size());
        Connection conn = connections.get(index);
        synchronized (conn) {
            return conn.send(request);
        }
    }

    /**
     * Get a direct connection (for subscription or fixed binding)
     *
     * @return Connection
     */
    public Connection getConnection() {
        if (connections.isEmpty()) {
            throw new IllegalStateException("There are no available connections.");
        }
        int index = roundRobinIndex.getAndUpdate(i -> (i + 1) % connections.size());
        return connections.get(index);
    }

    /**
     * Close
     */
    @Override
    public void close() throws IOException {
        for (Connection conn : connections) {
            conn.close();
        }
        connections.clear();
    }
}