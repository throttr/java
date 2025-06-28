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
import cl.throttr.responses.QueryResponse;
import io.netty.buffer.ByteBuf;

public class QueryParser implements ResponseParser {
    private final ValueSize size;

    public QueryParser(ValueSize size) {
        this.size = size;
    }

    @Override
    public ReadResult tryParse(ByteBuf buf) {
        int index = buf.readerIndex();
        if (buf.readableBytes() < 1) return null;

        byte success = buf.getByte(index);

        if (success == 0x00) {
            byte[] data = new byte[1];
            buf.getBytes(index, data);
            return new ReadResult(QueryResponse.fromBytes(data, size), 1);
        }

        int expected = 1 + size.getValue() + 1 + size.getValue();
        if (buf.readableBytes() < expected) return null;

        byte[] data = new byte[expected];
        buf.getBytes(index, data);
        QueryResponse response = QueryResponse.fromBytes(data, size);

        return new ReadResult(response, expected);
    }
}
