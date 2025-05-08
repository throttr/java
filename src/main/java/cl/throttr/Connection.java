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
import cl.throttr.requests.*;
import cl.throttr.responses.FullResponse;
import cl.throttr.responses.SimpleResponse;

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
    private final ValueSize size;

    public Connection(String host, int port, ValueSize size) throws IOException {
        this.socket = new Socket(host, port);
        this.socket.setTcpNoDelay(true);
        this.socket.setSoTimeout(5000);
        this.size = size;
    }

    public CompletableFuture<Object> send(Object request) {
        byte[] buffer;
        boolean expectFullResponse;

        switch (request) {
            case InsertRequest insert -> {
                buffer = insert.toBytes(size);
                expectFullResponse = false;
            }
            case QueryRequest query -> {
                buffer = query.toBytes();
                expectFullResponse = true;
            }
            case UpdateRequest update -> {
                buffer = update.toBytes(size);
                expectFullResponse = false;
            }
            case PurgeRequest purge -> {
                buffer = purge.toBytes();
                expectFullResponse = false;
            }
            default -> throw new IllegalArgumentException("Unsupported request type: " + request.getClass());
        }

        CompletableFuture<Object> future = new CompletableFuture<>();
        synchronized (queue) {
            queue.add(new PendingRequest(buffer, future, expectFullResponse));
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

                int first = in.read();
                if (first == -1) throw new IOException("No response received");

                Object response;

                if (!pending.isExpectFullResponse()) {
                    response = new SimpleResponse(first == 1);
                } else if (first == 0) {
                    response = new FullResponse(false, 0, null, 0);
                } else {
                    int payloadSize = size.getValue() * 2 + 1;
                    byte[] rest = new byte[payloadSize];
                    int totalRead = 0;
                    while (totalRead < payloadSize) {
                        int read = in.read(rest, totalRead, payloadSize - totalRead);
                        if (read == -1) throw new IOException("Connection closed during full response");
                        totalRead += read;
                    }

                    byte[] full = new byte[1 + payloadSize];
                    full[0] = (byte) first;
                    System.arraycopy(rest, 0, full, 1, payloadSize);

                    response = FullResponse.fromBytes(full, size);
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