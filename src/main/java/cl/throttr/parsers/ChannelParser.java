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
import cl.throttr.responses.ChannelConnectionItem;
import cl.throttr.responses.ChannelResponse;
import cl.throttr.utils.Binary;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

public class ChannelParser implements ResponseParser {
    private static final int CONNECTION_ENTRY_SIZE = 40;

    @Override
    public ReadResult tryParse(ByteBuf buf) {
        int start = buf.readerIndex();

        if (buf.readableBytes() < 1) return null;

        byte status = buf.getByte(start);
        if (status != 0x01) {
            return new ReadResult(new ChannelResponse(false, List.of()), 1);
        }

        if (buf.readableBytes() < 1 + ValueSize.UINT64.getValue()) return null;
        long connectionCount = Binary.read(buf, start + 1, ValueSize.UINT64);

        int requiredBytes = 1 + ValueSize.UINT64.getValue() + Math.toIntExact(connectionCount) * CONNECTION_ENTRY_SIZE;
        if (buf.readableBytes() < requiredBytes) return null;

        int offset = start + 1 + ValueSize.UINT64.getValue();
        List<ChannelConnectionItem> connections = new ArrayList<>();

        for (int i = 0; i < connectionCount; i++) {
            byte[] uuidBytes = new byte[16];
            buf.getBytes(offset, uuidBytes); offset += 16;

            String id = HexFormat.of().formatHex(uuidBytes);

            long subscribedAt = Binary.read(buf, offset, ValueSize.UINT64); offset += 8;
            long readBytes = Binary.read(buf, offset, ValueSize.UINT64); offset += 8;
            long writeBytes = Binary.read(buf, offset, ValueSize.UINT64); offset += 8;

            connections.add(new ChannelConnectionItem(id, subscribedAt, readBytes, writeBytes));
        }

        int totalRead = offset - start;

        return new ReadResult(new ChannelResponse(true, connections), totalRead);
    }
}