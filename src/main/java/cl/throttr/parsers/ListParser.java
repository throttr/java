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
import cl.throttr.responses.ListItem;
import cl.throttr.responses.ListResponse;
import cl.throttr.utils.Binary;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ListParser implements ResponseParser {
    private final ValueSize size;

    public ListParser(ValueSize size) {
        this.size = size;
    }

    @Override
    public ReadResult tryParse(ByteBuf buf) {
        int index = buf.readerIndex();
        if (buf.readableBytes() < 1 + 8) return null;

        int i = index + 1;
        long fragments = Binary.read(buf, i, ValueSize.UINT64);
        i += 8;

        List<ListItem> items = new ArrayList<>();

        for (long f = 0; f < fragments; f++) {
            if (buf.readableBytes() < i - index + 8 + 8) return null;

            i += 8; // skip fragment ID
            long keysInFragment = Binary.read(buf, i, ValueSize.UINT64);
            i += 8;

            int perKeyHeader = 3 + 8 + size.getValue();
            int keyHeadersSize = Math.toIntExact(keysInFragment) * perKeyHeader;

            if (buf.readableBytes() < i - index + keyHeadersSize) return null;

            List<Integer> keyLengths = new ArrayList<>();
            List<ListItem> scopedItems = new ArrayList<>();

            for (int j = 0; j < keysInFragment; j++) {
                int keyLength = buf.getUnsignedByte(i++);
                int keyType = buf.getUnsignedByte(i++);
                int ttlType = buf.getUnsignedByte(i++);
                long expiresAt = Binary.read(buf, i, ValueSize.UINT64); i += 8;
                long bytesUsed = Binary.read(buf, i, size); i += size.getValue();

                scopedItems.add(new ListItem("", keyLength, keyType, ttlType, expiresAt, bytesUsed));
                keyLengths.add(keyLength);
            }

            int totalKeyBytes = keyLengths.stream().mapToInt(Integer::intValue).sum();
            if (buf.readableBytes() < i - index + totalKeyBytes) return null;

            for (int j = 0; j < scopedItems.size(); j++) {
                int len = keyLengths.get(j);
                byte[] keyBytes = new byte[len];
                buf.getBytes(i, keyBytes);
                i += len;
                scopedItems.get(j).setKey(new String(keyBytes, StandardCharsets.UTF_8));
            }

            items.addAll(scopedItems);
        }

        int totalConsumed = i - index;
        return new ReadResult(new ListResponse(true, items), totalConsumed);
    }
}
