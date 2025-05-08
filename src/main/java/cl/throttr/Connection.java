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

import java.io.ByteArrayOutputStream;
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
    private volatile boolean shutdownRequested = false;
    private volatile boolean closed = false;

    public Connection(String host, int port, ValueSize size) throws IOException {
        this.socket = new Socket(host, port);
        this.socket.setTcpNoDelay(true);
        this.socket.setSoTimeout(5000);
        this.size = size;

        try {
            Thread.sleep(1000); // espera a que el servidor contenedor quede listo (solo en CI se nota)
        } catch (InterruptedException ignored) {
        }
    }

    public CompletableFuture<Object> send(Object request) {
        if (closed || socket.isClosed()) {
            CompletableFuture<Object> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IOException("Socket is already closed"));
            return failed;
        }

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
                InputStream in = socket.getInputStream();

                out.write(pending.getBuffer());
                out.flush();

                ByteArrayOutputStream fullBuffer = new ByteArrayOutputStream();
                byte[] temp = new byte[64];
                int total = 0;

                long start = System.currentTimeMillis();
                while (true) {
                    if (System.currentTimeMillis() - start > 5000) {
                        throw new IOException("Timeout waiting for response");
                    }

                    if (in.available() == 0) {
                        Thread.sleep(1); // evita busy-wait real
                        continue;
                    }

                    int read = in.read(temp);
                    if (read == -1) {
                        throw new IOException("Connection closed unexpectedly");
                    }

                    fullBuffer.write(temp, 0, read);
                    total += read;

                    byte[] bytes = fullBuffer.toByteArray();

                    if (pending.isExpectFullResponse()) {
                        if (total >= 1 && bytes[0] == 0x00) break;
                        int expected = 1 + size.getValue() * 2 + 1;
                        if (total >= expected) break;
                    } else {
                        if (total >= 1) break;
                    }
                }
                Object response;

                byte[] finalBytes = fullBuffer.toByteArray();
                if (pending.isExpectFullResponse()) {
                    if (finalBytes.length == 1 && finalBytes[0] == 0x00) {
                        response = new FullResponse(false, 0, null, 0);
                    } else {
                        response = FullResponse.fromBytes(finalBytes, size);
                    }
                } else {
                    response = new SimpleResponse(finalBytes[0] == 1);
                }

                pending.getFuture().complete(response);
            } catch (IOException | InterruptedException e) {
                pending.getFuture().completeExceptionally(e);
            } finally {
                busy = false;
                processQueue();
            }
        }
    }

    @Override
    public void close() throws IOException {
        shutdownRequested = true;
        synchronized (queue) {
            if (!busy && queue.isEmpty()) {
                socket.close();
                closed = true;
            }
        }
    }
}