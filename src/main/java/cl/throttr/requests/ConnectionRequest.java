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

/**
 * Stat request
 */
public record ConnectionRequest(
        String id
) {
    /**
     * To bytes
     *
     * @return byte[]
     */
    public byte[] toBytes() {
        byte[] idBytes = hexStringToByteArray(id);

        var buffer = ByteBuffer.allocate(1 + 16);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.put((byte) RequestType.CONNECTION.getValue());
        buffer.put(idBytes);

        return buffer.array();
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int hi = Character.digit(s.charAt(i), 16);
            int lo = Character.digit(s.charAt(i + 1), 16);
            data[i / 2] = (byte) ((hi << 4) + lo);
        }
        return data;
    }
}
