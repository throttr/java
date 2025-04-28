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

/**
 * Response class representing the server's response to a request.
 * It contains the information about whether the request can be processed,
 * the available number of requests, and the remaining TTL (Time-to-live).
 */
public record Response(boolean can, int availableRequests, long ttl) {

    /**
     * Converts a byte array (response from the server) into a Response object.
     * The expected response format is:
     *  - can (boolean) indicating if the request can be processed
     *  - availableRequests (integer) the number of remaining requests allowed
     *  - ttl (long) the remaining TTL
     *
     * @param data The byte array representing the response from the server
     * @return Response The parsed response object
     */
    public static Response fromBytes(byte[] data) {
        var buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        boolean can = buffer.get() == 1; // First byte indicates whether the request can be processed
        int availableRequests = buffer.getInt(); // Next 4 bytes represent available requests
        long ttl = buffer.getLong(); // Last 8 bytes represent the remaining TTL

        return new Response(can, availableRequests, ttl); // Return a new Response object
    }
}