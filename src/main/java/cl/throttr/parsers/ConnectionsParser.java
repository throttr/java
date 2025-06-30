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
import cl.throttr.responses.ConnectionsItem;
import cl.throttr.responses.ConnectionsResponse;
import cl.throttr.utils.Binary;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class ConnectionsParser implements ResponseParser {
    private static final int ENTRY_SIZE = 237;
    private static final int HEADER_SIZE = 1 + 8; // status + fragments

    @Override
    public ReadResult tryParse(ByteBuf buf) {
        int index = buf.readerIndex();

        if (buf.readableBytes() < HEADER_SIZE) return null;

        byte status = buf.getByte(index);
        if (status != 0x01) {
            return new ReadResult(new ConnectionsResponse(false, new ArrayList<>()), 1);
        }

        int i = index + 1;
        long fragments = Binary.read(buf, i, ValueSize.UINT64);
        i += 8;

        List<ConnectionsItem> items = new ArrayList<>();

        for (long f = 0; f < fragments; f++) {
            if (buf.readableBytes() < i - index + 8 + 8) return null;

            i += 8; // skip fragment ID
            long count = Binary.read(buf, i, ValueSize.UINT64);
            i += 8;

            int totalFragmentBytes = Math.toIntExact(count) * ENTRY_SIZE;
            if (buf.readableBytes() < i - index + totalFragmentBytes) return null;

            for (int c = 0; c < count; c++) {
                if (buf.readableBytes() < i - index + ENTRY_SIZE) return null;

                byte[] entryData = new byte[ENTRY_SIZE];
                buf.getBytes(i, entryData);
                i += ENTRY_SIZE;

                items.add(ConnectionsItem.fromBytes(entryData));
            }
        }

        int totalConsumed = i - index;
        return new ReadResult(new ConnectionsResponse(true, items), totalConsumed);
    }
}
