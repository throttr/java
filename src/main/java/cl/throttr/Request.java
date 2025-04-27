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
 * Request
 *
 * @param ip
 * @param port
 * @param url
 * @param max_requests
 * @param ttl
 */
public record Request(InetAddress ip, int port, String url, int max_requests, long ttl) {

    /**
     * To bytes
     *
     * @return byte[]
     */
    public byte[] toBytes() {
        var urlBytes = url.getBytes(StandardCharsets.UTF_8);
        var buffer = ByteBuffer.allocate(28 + urlBytes.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        byte[] ipAddress = ip.getAddress();
        buffer.put((byte) ipAddress.length);

        byte[] ipPadded = new byte[16];
        System.arraycopy(ipAddress, 0, ipPadded, 0, ipAddress.length);
        buffer.put(ipPadded);

        buffer.putShort((short) port);
        buffer.putInt(max_requests);
        buffer.putInt((int) ttl);
        buffer.put((byte) urlBytes.length);
        buffer.put(urlBytes);

        return buffer.array();
    }

    /**
     * From
     *
     * @param ipAddress IP address
     * @param port Port
     * @param url URL
     * @param maxRequests Maximum requests
     * @param ttl TTL
     * @return Request
     *
     * @throws UnknownHostException If no IP address for the host could be found
     */
    public static Request from(String ipAddress, int port, String url, int maxRequests, long ttl) throws UnknownHostException {
        var ip = InetAddress.getByName(ipAddress);
        return new Request(ip, port, url, maxRequests, ttl);
    }
}