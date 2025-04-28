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
 * Full response
 *
 * @param allowed Whether the request was allowed
 * @param quotaRemaining Remaining quota
 * @param ttlRemaining Remaining TTL
 */
public record FullResponse(boolean allowed, long quotaRemaining, long ttlRemaining) {

    /**
     * From bytes
     *
     * @param data Buffer received
     * @return FullResponse
     */
    public static FullResponse fromBytes(byte[] data) {
        if (data.length != 18) {
            throw new IllegalArgumentException("Expected 18 bytes for full response");
        }

        var buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        boolean allowed = buffer.get() == 1;
        long quotaRemaining = buffer.getLong();
        long ttlRemaining = buffer.getLong();

        return new FullResponse(allowed, quotaRemaining, ttlRemaining);
    }
}
