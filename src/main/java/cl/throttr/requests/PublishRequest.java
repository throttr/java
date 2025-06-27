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
import cl.throttr.enums.ValueSize;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import static cl.throttr.utils.Binary.put;

/**
 * Publish request
 */
public record PublishRequest(
        String channel,
        String payload
) {
    /**
     * To bytes
     *
     * @return byte[]
     */
    public byte[] toBytes(ValueSize size) {
        byte[] channelBytes = channel.getBytes(StandardCharsets.UTF_8);
        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);

        var buffer = ByteBuffer.allocate(
                2 + channelBytes.length + size.getValue() + payloadBytes.length
        );
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.put((byte) RequestType.PUBLISH.getValue());
        buffer.put((byte) channelBytes.length);
        put(buffer, payloadBytes.length, size);
        buffer.put(channelBytes);
        buffer.put(payloadBytes);

        return buffer.array();
    }
}
