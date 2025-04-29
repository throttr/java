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

/**
 * Update request
 */
public record UpdateRequest(
        AttributeType attribute,
        ChangeType change,
        long value,
        String consumerId,
        String resourceId
) {
    /**
     * To bytes
     *
     * @return byte[]
     */
    public byte[] toBytes() {
        byte[] consumerIdBytes = consumerId.getBytes(StandardCharsets.UTF_8);
        byte[] resourceIdBytes = resourceId.getBytes(StandardCharsets.UTF_8);

        var buffer = ByteBuffer.allocate(
                1 + 1 + 1 + 8 + 1 + 1 + consumerIdBytes.length + resourceIdBytes.length
        );
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.put((byte) RequestType.Update.getValue());
        buffer.put((byte) attribute.getValue());
        buffer.put((byte) change.getValue());
        buffer.putLong(value);
        buffer.put((byte) consumerIdBytes.length);
        buffer.put((byte) resourceIdBytes.length);
        buffer.put(consumerIdBytes);
        buffer.put(resourceIdBytes);

        return buffer.array();
    }
}