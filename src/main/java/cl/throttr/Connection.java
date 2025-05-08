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
    private volatile boolean shouldStop = false;
    private volatile boolean closed = false;
    private final Object lock = new Object();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public Connection(String host, int port, ValueSize size) throws IOException {
        this.socket = new Socket(host, port);
        this.socket.setTcpNoDelay(true);
        this.socket.setSoTimeout(5000);
        this.size = size;

        try {
            Thread.sleep(1000); // para CI
        } catch (InterruptedException ignored) {}
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
            if (!busy && !executor.isShutdown()) {
                busy = true;
                executor.submit(this::processQueue);
            }
        }

        return future;
    }

    private void processQueue() {
        synchronized (lock) {
            while (!shouldStop) {
                PendingRequest pending;

                synchronized (queue) {
                    if (queue.isEmpty()) {
                        busy = false;
                        return;
                    }
                    pending = queue.poll();
                }

                try {
                    OutputStream out = socket.getOutputStream();
                    InputStream in = socket.getInputStream();
                    BufferedMessageReader reader = new BufferedMessageReader(in);

                    System.out.println("SEND  → [" + pending.getRequestType() + "] " + Binary.toHex(pending.getBuffer()));

                    out.write(pending.getBuffer());
                    out.flush();

                    byte[] fullBytes;
                    Object response;

                    if (pending.getRequestType() == RequestType.QUERY) {
                        byte[] head = reader.readFully(1);
                        if (head[0] == 0x01) {
                            int tailSize = size.getValue() * 2 + 1;
                            byte[] tail = reader.readFully(tailSize);
                            fullBytes = new byte[1 + tail.length];
                            fullBytes[0] = head[0];
                            System.arraycopy(tail, 0, fullBytes, 1, tail.length);
                            response = FullResponse.fromBytes(fullBytes, size);
                        } else {
                            fullBytes = head;
                            response = new SimpleResponse(false);
                        }
                    } else {
                        fullBytes = reader.readFully(1);
                        response = new SimpleResponse(fullBytes[0] == 1);
                    }

                    System.out.println("RECV  ← [" + pending.getRequestType() + "] " + Binary.toHex(fullBytes));
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

                    break; // rompe el loop si falla
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        shouldStop = true;
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
