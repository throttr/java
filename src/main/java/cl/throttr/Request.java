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
        var url_bytes = url.getBytes(StandardCharsets.UTF_8);
        var buffer = ByteBuffer.allocate(28 + url_bytes.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        byte[] ip_bytes = ip.getAddress();
        buffer.put((byte) ip_bytes.length);

        byte[] ip_padded = new byte[16];
        System.arraycopy(ip_bytes, 0, ip_padded, 0, ip_bytes.length);
        buffer.put(ip_padded);

        buffer.putShort((short) port);
        buffer.putInt(max_requests);
        buffer.putInt((int) ttl);
        buffer.put((byte) url_bytes.length);
        buffer.put(url_bytes);

        return buffer.array();
    }

    /**
     * From
     *
     * @param ip_address IP address
     * @param port Port
     * @param url URL
     * @param max_requests Maximum requests
     * @param ttl TTL
     * @return Request
     *
     * @throws UnknownHostException If no IP address for the host could be found
     */
    public static Request from(String ip_address, int port, String url, int max_requests, long ttl) throws UnknownHostException {
        var ip = InetAddress.getByName(ip_address);
        return new Request(ip, port, url, max_requests, ttl);
    }
}