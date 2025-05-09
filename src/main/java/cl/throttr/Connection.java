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

import java.io.DataInputStream;
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

import static cl.throttr.utils.Binary.toHex;

/**
 * Connection
 */
public class Connection implements AutoCloseable {
    private final Socket socket;
    private final ValueSize size;
    private final OutputStream out;
    private final DataInputStream in;

    public Connection(String host, int port, ValueSize size) throws IOException {
        this.socket = new Socket(host, port);
        this.socket.setTcpNoDelay(true);
        this.socket.setSoTimeout(30000);
        this.out = socket.getOutputStream();
        this.in = new DataInputStream(socket.getInputStream());
        this.size = size;
    }

    public Object send(Object request) throws IOException {
        if (socket.isClosed()) {
            throw new IOException("Socket is already closed");
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

        System.out.println("SEND  → " + toHex(buffer));

        out.write(buffer);
        out.flush();


        byte head = in.readByte();

        if (expectFullResponse && head == 0x01) {
            int expected = size.getValue() * 2 + 1;
            byte[] merged = new byte[expected];
            in.readFully(merged);
            byte[] full = new byte[1 + expected];
            full[0] = head;
            System.arraycopy(merged, 0, full, 1, expected);
            System.out.println("RECV  ← 2 " + toHex(full));
            return FullResponse.fromBytes(full, size);
        } else {
            System.out.println("RECV  ← 1 " + toHex(new byte[]{head}));
            return new SimpleResponse(head == 0x01);
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
