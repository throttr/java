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

package cl.throttr.responses;

import cl.throttr.enums.TTLType;
import cl.throttr.enums.ValueSize;
import cl.throttr.utils.Binary;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Full response
 */
public record GetResponse(
        boolean success,
        TTLType ttlType,
        long ttl,
        byte[] value
) {
    /**
     * Parse from bytes
     *
     * @param data Byte array (must be 18 bytes)
     * @return QueryResponse
     */
    public static GetResponse fromBytes(byte[] data, ValueSize size) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        boolean success = buffer.get() == 1;
        if (!success) {
            return new GetResponse(false, null, 0, null);
        }

        TTLType ttlType = TTLType.fromByte(buffer.get());
        long ttl = Binary.read(buffer, size);
        long valueSize = Binary.read(buffer, size);

        byte[] value = new byte[(int) valueSize];
        buffer.get(value);

        return new GetResponse(true, ttlType, ttl, value);
    }
}