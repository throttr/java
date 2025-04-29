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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

/**
 * Connection
 */
public class Connection implements AutoCloseable {
    private final Socket socket;
    private final Queue<PendingRequest> queue = new LinkedList<>();
    private boolean busy = false;

    public Connection(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
    }

    public CompletableFuture<Object> send(Object request) {
        byte[] buffer;
        int expectedSize;
        boolean expectFullResponse;

        switch (request) {
            case InsertRequest insert -> {
                buffer = insert.toBytes();
                expectedSize = 18;
                expectFullResponse = true;
            }
            case QueryRequest query -> {
                buffer = query.toBytes();
                expectedSize = 18;
                expectFullResponse = true;
            }
            case UpdateRequest update -> {
                buffer = update.toBytes();
                expectedSize = 1;
                expectFullResponse = false;
            }
            case PurgeRequest purge -> {
                buffer = purge.toBytes();
                expectedSize = 1;
                expectFullResponse = false;
            }
            default -> throw new IllegalArgumentException("Unsupported request type: " + request.getClass());
        }

        CompletableFuture<Object> future = new CompletableFuture<>();
        synchronized (queue) {
            queue.add(new PendingRequest(buffer, future, expectedSize, expectFullResponse));
            processQueue();
        }
        return future;
    }

    private void processQueue() {
        synchronized (queue) {
            if (busy || queue.isEmpty()) {
                return;
            }

            PendingRequest pending = queue.poll();
            busy = true;

            try {
                OutputStream out = socket.getOutputStream();
                out.write(pending.getBuffer());
                out.flush();

                InputStream in = socket.getInputStream();
                byte[] responseBytes = new byte[pending.getExpectedSize()];
                int totalRead = 0;

                while (totalRead < pending.getExpectedSize()) {
                    int read = in.read(responseBytes, totalRead, pending.getExpectedSize() - totalRead);
                    if (read == -1) {
                        throw new IOException("Connection closed before full response received");
                    }
                    totalRead += read;
                }

                Object response;
                if (pending.isExpectFullResponse()) {
                    response = FullResponse.fromBytes(responseBytes);
                } else {
                    response = SimpleResponse.fromBytes(responseBytes);
                }

                pending.getFuture().complete(response);
            } catch (IOException e) {
                pending.getFuture().completeExceptionally(e);
            } finally {
                busy = false;
                processQueue();
            }
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}