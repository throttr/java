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

package cl.throttr.parsers;

import cl.throttr.ReadResult;
import cl.throttr.responses.StatResponse;
import io.netty.buffer.ByteBuf;

public class StatParser implements ResponseParser {
    @Override
    public ReadResult tryParse(ByteBuf buf) {
        int index = buf.readerIndex();
        byte status = buf.getByte(index);
        if (status == 0x00) {
            return new ReadResult(new StatResponse(false, 0, 0, 0, 0), 1);
        }

        int expected = 1 + 8 * 4; // status + 4 campos uint64
        if (buf.readableBytes() < expected) return null;

        byte[] data = new byte[expected];
        buf.getBytes(index, data);
        return new ReadResult(StatResponse.fromBytes(data), expected);
    }
}