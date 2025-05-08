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
    private final Object lock = new Object();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

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
        synchronized (lock) {
            while (true) {
                PendingRequest pending;

                synchronized (queue) {
                    if (queue.isEmpty()) {
                        busy = false;
                        return;
                    }
                    pending = queue.poll();
                    busy = true;
                }

                try {
                    OutputStream out = socket.getOutputStream();
                    InputStream in = socket.getInputStream();

                    System.out.println("SEND  → [" + pending.getRequestType() + "] " + Binary.toHex(pending.getBuffer()));

                    out.write(pending.getBuffer());
                    out.flush();

                    ByteArrayOutputStream fullBuffer = new ByteArrayOutputStream();

                    byte[] head = new byte[1];
                    int read = in.read(head);
                    System.out.println("It reads: " + read);
                    if (read != 1) throw new IOException("Expected 1 byte response");

                    fullBuffer.write(head);

                    byte[] finalBytes;
                    Object response;

                    if (pending.getRequestType() == RequestType.QUERY && head[0] == 0x01) {
                        int expected = size.getValue() * 2 + 1;
                        byte[] rest = new byte[expected];
                        int offset = 0;

                        while (offset < expected) {
                            int r = in.read(rest, offset, expected - offset);
                            System.out.println("Leyó " + r + " bytes desde el socket");
                            if (r == -1) throw new IOException("Connection closed while reading response");
                            offset += r;
                        }

                        fullBuffer.write(rest);
                        finalBytes = fullBuffer.toByteArray();
                        response = FullResponse.fromBytes(finalBytes, size);
                    } else {
                        finalBytes = fullBuffer.toByteArray();
                        response = new SimpleResponse(finalBytes[0] == 1);
                    }

                    System.out.println("RECV  ← [" + pending.getRequestType() + "] " + Binary.toHex(finalBytes));
                    pending.getFuture().complete(response);
                } catch (IOException e) {
                    pending.getFuture().completeExceptionally(e);
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        shutdownRequested = true;
        executor.shutdownNow();
        synchronized (queue) {
            if (!busy && queue.isEmpty()) {
                socket.close();
                closed = true;
            }
        }
    }
}