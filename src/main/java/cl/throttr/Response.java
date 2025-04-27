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
 * Response
 *
 * @param can
 * @param availableRequests
 * @param ttl
 */
public record Response(boolean can, int availableRequests, long ttl) {

    /**
     * From bytes
     *
     * @param data Bytes
     * @return Response
     */
    public static Response fromBytes(byte[] data) {
        var buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        boolean can = buffer.get() == 1;
        int availableRequests = buffer.getInt();
        long ttl = buffer.getLong();

        return new Response(can, availableRequests, ttl);
    }
}