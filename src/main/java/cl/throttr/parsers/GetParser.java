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
import cl.throttr.enums.ValueSize;
import cl.throttr.responses.GetResponse;
import cl.throttr.utils.Binary;
import io.netty.buffer.ByteBuf;

public class GetParser implements ResponseParser {
    private final ValueSize size;

    public GetParser(ValueSize size) {
        this.size = size;
    }

    @Override
    public ReadResult tryParse(ByteBuf buf) {
        int index = buf.readerIndex();

        if (buf.readableBytes() < 1) return null;
        byte success = buf.getByte(index);

        if (success == 0) {
            if (buf.readableBytes() < 1) return null;
            byte[] data = new byte[1];
            buf.getBytes(index, data);
            return new ReadResult(GetResponse.fromBytes(data, size), 1);
        }

        int minHeader = 1 + 1 + size.getValue() + size.getValue(); // success + ttlType + ttl + valueSize
        if (buf.readableBytes() < minHeader) return null;

        int valueSizeOffset = index + 1 + 1 + size.getValue(); // skip success + ttlType + ttl

        if (buf.readableBytes() < (valueSizeOffset + size.getValue() - index)) return null;

        long valueSize = Binary.read(buf, valueSizeOffset, size);
        long total = minHeader + valueSize;

        if (buf.readableBytes() < total) return null;

        byte[] data = new byte[(int) total];
        buf.getBytes(index, data);
        return new ReadResult(GetResponse.fromBytes(data, size), (int) total);
    }
}