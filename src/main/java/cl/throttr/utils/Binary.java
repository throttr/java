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

package cl.throttr.utils;

import cl.throttr.enums.ValueSize;

import java.nio.ByteBuffer;

/**
 * Binary helpers
 */
public final class Binary {
    /**
     * Constructor
     */
    private Binary() {}

    /**
     * Put
     *
     * @param buffer
     * @param value
     * @param size
     */
    public static void put(ByteBuffer buffer, long value, ValueSize size) {
        switch (size) {
            case UINT8 -> buffer.put((byte) value);
            case UINT16 -> buffer.putShort((short) value);
            case UINT32 -> buffer.putInt((int) value);
            case UINT64 -> buffer.putLong(value);
        }
    }

    public static long read(ByteBuffer buffer, ValueSize size) {
        return switch (size) {
            case UINT8 -> buffer.get() & 0xFF;
            case UINT16 -> buffer.getShort() & 0xFFFF;
            case UINT32 -> buffer.getInt() & 0xFFFFFFFFL;
            case UINT64 -> buffer.getLong();
        };
    }
}