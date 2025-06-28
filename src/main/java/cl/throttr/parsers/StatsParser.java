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
import cl.throttr.responses.StatsItem;
import cl.throttr.responses.StatsResponse;
import cl.throttr.utils.Binary;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class StatsParser implements ResponseParser {
    @Override
    public ReadResult tryParse(ByteBuf buf) {
        int index = buf.readerIndex();
        if (buf.readableBytes() < 1 + 8) return null;

        byte status = buf.getByte(index);
        if (status == 0x00) {
            return new ReadResult(new StatsResponse(false, new ArrayList<>()), 1);
        }

        int i = index + 1;
        long fragments = Binary.read(buf, i, ValueSize.UINT64);
        i += 8;

        List<StatsItem> items = new ArrayList<>();

        for (long f = 0; f < fragments; f++) {
            if (buf.readableBytes() < i - index + 8 + 8) return null;

            i += 8; // skip timestamp
            long keysInFragment = Binary.read(buf, i, ValueSize.UINT64);
            i += 8;

            int perKeyHeader = 33;
            if (keysInFragment > (Integer.MAX_VALUE / perKeyHeader)) {
                throw new ArithmeticException("Too many keys in fragment: " + keysInFragment);
            }

            int keyHeadersSize = Math.toIntExact(keysInFragment) * perKeyHeader;
            if (buf.readableBytes() < i - index + keyHeadersSize) return null;

            List<StatsItem> scopedItems = new ArrayList<>();
            List<Integer> keyLengths = new ArrayList<>();

            for (int j = 0; j < keysInFragment; j++) {
                int keyLength = buf.getUnsignedByte(i++);
                long readsPerMin  = Binary.read(buf, i, ValueSize.UINT64); i += 8;
                long writesPerMin = Binary.read(buf, i, ValueSize.UINT64); i += 8;
                long totalReads   = Binary.read(buf, i, ValueSize.UINT64); i += 8;
                long totalWrites  = Binary.read(buf, i, ValueSize.UINT64); i += 8;

                scopedItems.add(new StatsItem("", keyLength, readsPerMin, writesPerMin, totalReads, totalWrites));
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
        return new ReadResult(new StatsResponse(true, items), totalConsumed);
    }
}
