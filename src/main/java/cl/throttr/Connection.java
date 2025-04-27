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
import java.net.UnknownHostException;
import java.util.Optional;

/**
 * Connection
 */
public class Connection implements AutoCloseable {
    /**
     * Socket
     */
    private final Socket socket;

    /**
     * Constructor
     *
     * @param host Remote address
     * @param port Port
     *
     * @throws IOException If an I/O error occurs when creating the socket
     * @throws UnknownHostException – If the IP address of the host could not be determined
     * @throws IllegalArgumentException If used port isn't between 0 and 65535
     */
    public Connection(String host, int port) throws IOException, UnknownHostException, IllegalArgumentException {
        this.socket = new Socket(host, port);
    }

    /**
     * Send
     *
     * @param request Request
     * @return Optional<Response>
     */
    public Optional<Response> send(Request request) {
        try {
            OutputStream out = socket.getOutputStream();

            out.write(request.toBytes());
            out.flush();

            InputStream in = socket.getInputStream();
            byte[] responseBytes = new byte[13];
            int read = in.read(responseBytes);

            if (read == 13) {
                return Optional.of(Response.fromBytes(responseBytes));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Close
     * @throws IOException If an I/O error occurs when closing this socket.
     */
    @Override
    public void close() throws IOException {
        socket.close();
    }
}