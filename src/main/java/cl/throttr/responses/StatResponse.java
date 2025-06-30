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

import cl.throttr.enums.ValueSize;
import cl.throttr.utils.Binary;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

public record StatResponse(
        boolean success,
        long readsPerMinute,
        long writesPerMinute,
        long totalReads,
        long totalWrites
) {
    public static StatResponse fromBytes(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        boolean success = Binary.read(buffer, ValueSize.UINT8) == 0x01;
        long rpm = Binary.read(buffer, ValueSize.UINT64);
        long wpm = Binary.read(buffer, ValueSize.UINT64);
        long tr = Binary.read(buffer, ValueSize.UINT64);
        long tw = Binary.read(buffer, ValueSize.UINT64);

        return new StatResponse(success, rpm, wpm, tr, tw);
    }
}