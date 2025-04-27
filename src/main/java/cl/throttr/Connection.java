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
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

/**
 * Connection
 */
public class Connection implements AutoCloseable {
    /**
     * Socket
     */
    private final Socket socket;

    /**
     * Queue of pending requests
     */
    private final Queue<PendingRequest> queue = new LinkedList<>();

    /**
     * Busy
     */
    private boolean busy = false;

    /**
     * Constructor
     *
     * @param host Remote address
     * @param port Port
     * @throws IOException              If an I/O error occurs when creating the socket
     * @throws IllegalArgumentException If used port isn't between 0 and 65535
     */
    public Connection(String host, int port) throws IOException, IllegalArgumentException {
        this.socket = new Socket(host, port);
    }

    /**
     * Send
     *
     * @param request Request
     * @return CompletableFuture<Response>
     */
    public CompletableFuture<Response> send(Request request) {
        byte[] buffer = request.toBytes();
        CompletableFuture<Response> future = new CompletableFuture<>();

        synchronized (queue) {
            queue.add(new PendingRequest(buffer, future));
            processQueue();
        }

        return future;
    }


    /**
     * Process queue
     */
    private void processQueue() {
        synchronized (queue) {
            if (busy || queue.isEmpty()) {
                return;
            }

            PendingRequest pending = queue.poll();

            busy = true;

            try {
                OutputStream out = socket.getOutputStream();
                out.write(pending.buffer());
                out.flush();

                InputStream in = socket.getInputStream();
                byte[] responseBytes = new byte[13];
                int totalRead = 0;

                while (totalRead < 13) {
                    int read = in.read(responseBytes, totalRead, 13 - totalRead);
                    if (read == -1) {
                        throw new IOException("Connection closed before full response received");
                    }
                    totalRead += read;
                }

                Response response = Response.fromBytes(responseBytes);
                pending.future().complete(response);
            } catch (IOException e) {
                pending.future().completeExceptionally(e);
            } finally {
                busy = false;
                processQueue();
            }
        }
    }

    /**
     * Close
     *
     * @throws IOException If an I/O error occurs when closing this socket.
     */
    @Override
    public void close() throws IOException {
        socket.close();
    }
}