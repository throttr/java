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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Request class representing the structure of the request to be sent to the Throttr server.
 * It holds information about the consumer, resource, maximum requests, and TTL (Time-to-Live).
 */
public record Request(InetAddress ip, int port, String url, int max_requests, long ttl) {

    /**
     * Converts the Request to a byte array to be sent over the network.
     * The format includes:
     *  - Consumer IP address (padded to 16 bytes)
     *  - Port number
     *  - Maximum requests
     *  - TTL
     *  - URL (UTF-8 encoded)
     *
     * @return byte[] The byte array representing the request
     */
    public byte[] toBytes() {
        var urlBytes = url.getBytes(StandardCharsets.UTF_8);
        var buffer = ByteBuffer.allocate(28 + urlBytes.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        byte[] ipAddress = ip.getAddress();
        buffer.put((byte) ipAddress.length); // Length of the IP address

        byte[] ipPadded = new byte[16]; // Padding the IP to 16 bytes
        System.arraycopy(ipAddress, 0, ipPadded, 0, ipAddress.length);
        buffer.put(ipPadded); // IP Address

        buffer.putShort((short) port); // Port
        buffer.putInt(max_requests); // Max requests
        buffer.putInt((int) ttl); // TTL (Time-to-live)
        buffer.put((byte) urlBytes.length); // Length of the URL
        buffer.put(urlBytes); // URL

        return buffer.array();
    }

    /**
     * Static factory method to create a Request from given parameters.
     * This method is used to convert string representations of the IP address into InetAddress objects.
     *
     * @param ipAddress IP address as a string
     * @param port Port number
     * @param url URL as a string
     * @param maxRequests Maximum number of requests
     * @param ttl Time-to-live
     * @return Request The constructed Request object
     * @throws UnknownHostException If the IP address cannot be resolved
     */
    public static Request from(String ipAddress, int port, String url, int maxRequests, long ttl) throws UnknownHostException {
        var ip = InetAddress.getByName(ipAddress);
        return new Request(ip, port, url, maxRequests, ttl);
    }
}