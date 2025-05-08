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

import cl.throttr.enums.RequestType;
import cl.throttr.enums.ValueSize;
import cl.throttr.requests.*;
import cl.throttr.responses.FullResponse;
import cl.throttr.responses.SimpleResponse;
import cl.throttr.utils.Binary;
import cl.throttr.utils.BufferedMessageReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Connection
 */
public class Connection implements AutoCloseable {
    private final Socket socket;
    private final Queue<PendingRequest> queue = new LinkedList<>();
    private boolean busy = false;
    private final ValueSize size;
    private volatile boolean closed = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final OutputStream out;
    private final InputStream in;

    public Connection(String host, int port, ValueSize size) throws IOException {
        this.socket = new Socket(host, port);
        this.socket.setTcpNoDelay(true);
        this.socket.setSoTimeout(5000);
        this.out = socket.getOutputStream();
        this.in = socket.getInputStream();
        this.size = size;
    }

    public CompletableFuture<Object> send(Object request) {
        if (closed || socket.isClosed()) {
            CompletableFuture<Object> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IOException("Socket is already closed"));
            return failed;
        }

        byte[] buffer;
        boolean expectFullResponse;
        RequestType requestType;

        switch (request) {
            case InsertRequest insert -> {
                buffer = insert.toBytes(size);
                expectFullResponse = false;
                requestType = RequestType.INSERT;
            }
            case QueryRequest query -> {
                buffer = query.toBytes();
                expectFullResponse = true;
                requestType = RequestType.QUERY;
            }
            case UpdateRequest update -> {
                buffer = update.toBytes(size);
                expectFullResponse = false;
                requestType = RequestType.UPDATE;
            }
            case PurgeRequest purge -> {
                buffer = purge.toBytes();
                expectFullResponse = false;
                requestType = RequestType.PURGE;
            }
            default -> throw new IllegalArgumentException("Unsupported request type: " + request.getClass());
        }

        CompletableFuture<Object> future = new CompletableFuture<>();
        synchronized (queue) {
            queue.add(new PendingRequest(buffer, future, expectFullResponse, requestType));
            if (!busy) {
                busy = true;
                executor.submit(this::processQueue);
            }
        }
        return future;
    }

    private void processQueue() {
        while (true) {
            PendingRequest pending;

            synchronized (queue) {
                if (queue.isEmpty()) {
                    busy = false;
                    return;
                }
                pending = queue.poll();
            }

            try {
                System.out.println("SEND  → [" + pending.getRequestType() + "] " + Binary.toHex(pending.getBuffer()));

                out.write(pending.getBuffer());
                out.flush();

                Object response;

                byte[] head = new byte[0];
                while(head.length != 1) {
                    head = in.readNBytes(1);
                }

                if (pending.getRequestType() == RequestType.QUERY) {
                    if (head[0] == 0x01) {
                        int expected = size.getValue() * 2 + 1;

                        System.out.println("Expected: " + expected);

                        byte[] merged = new byte[expected];
                        int offset = 0;

                        while (offset < expected) {
                            byte[] chunk = in.readNBytes(expected - offset);
                            if (chunk.length == 0) {
                                continue; // sigue intentando hasta que lea algo útil
                            }
                            System.arraycopy(chunk, 0, merged, offset, chunk.length);
                            offset += chunk.length;
                        }

                        byte[] full = new byte[1 + expected];
                        full[0] = head[0];
                        System.arraycopy(merged, 0, full, 1, expected);

                        System.out.println("RCV QUERY 0x01  → [" + pending.getRequestType() + "] " + Binary.toHex(full));

                        response = FullResponse.fromBytes(full, size);
                    } else {
                        System.out.println("RCV QUERY 0x00  → [" + pending.getRequestType() + "] " + Binary.toHex(head));

                        response = new SimpleResponse(false);
                    }
                } else {
                    System.out.println("RCV OTHERS  → [" + pending.getRequestType() + "] " + Binary.toHex(head));
                    response = new SimpleResponse(head[0] == 0x01);
                }

                pending.getFuture().complete(response);
            } catch (IOException e) {
                System.out.println("ERROR  ← [" + e.getMessage() + "]");
                pending.getFuture().completeExceptionally(e);

                synchronized (queue) {
                    while (!queue.isEmpty()) {
                        PendingRequest next = queue.poll();
                        next.getFuture().completeExceptionally(new IOException("Connection aborted due to previous failure"));
                    }
                    busy = false;
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ignored) {}
        socket.close();
        closed = true;
    }
}
