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
import cl.throttr.responses.ConnectionResponse;
import cl.throttr.responses.ConnectionsItem;
import io.netty.buffer.ByteBuf;

public class ConnectionParser implements ResponseParser {
    private static final int ENTRY_SIZE = 237;

    @Override
    public ReadResult tryParse(ByteBuf buf) {
        int index = buf.readerIndex();

        if (buf.readableBytes() < 1) return null;

        byte status = buf.getByte(index);

        if (status == 0x00) {
            return new ReadResult(new ConnectionResponse(false, null), 1);
        }

        if (buf.readableBytes() < 1 + ENTRY_SIZE) return null;

        byte[] data = new byte[ENTRY_SIZE];
        buf.getBytes(index + 1, data, 0, ENTRY_SIZE);

        ConnectionsItem item = ConnectionsItem.fromBytes(data);
        return new ReadResult(new ConnectionResponse(true, item), 1 + ENTRY_SIZE);
    }
}
