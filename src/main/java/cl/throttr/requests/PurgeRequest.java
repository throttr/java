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

package cl.throttr.requests;

import cl.throttr.enums.RequestType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Purge request
 */
public record PurgeRequest(
        String key
) {
    /**
     * To bytes
     *
     * @return byte[]
     */
    public byte[] toBytes() {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

        var buffer = ByteBuffer.allocate(
                2 + keyBytes.length
        );
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.put((byte) RequestType.PURGE.getValue());
        buffer.put((byte) keyBytes.length);
        buffer.put(keyBytes);

        return buffer.array();
    }
}