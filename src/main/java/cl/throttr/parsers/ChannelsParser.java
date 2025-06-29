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
import cl.throttr.responses.ChannelsItem;
import cl.throttr.responses.ChannelsResponse;
import cl.throttr.utils.Binary;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ChannelsParser implements ResponseParser {
    private static final int HEADER_SIZE = 8; // FRAGMENTS (P)
    private static final int FRAGMENT_HEADER_SIZE = 16; // FRAGMENT ID + Q
    private static final int ENTRY_SIZE = 25; // QL (1 byte) + 3 x UINT64 (24 bytes)

    @Override
    public ReadResult tryParse(ByteBuf buf) {
        int start = buf.readerIndex();

        // Validar mínimo 1 byte para status
        if (buf.readableBytes() < 1) return null;
        byte status = buf.getByte(start);
        if (status != 0x01) {
            buf.readerIndex(start + 1);
            return new ReadResult(new ChannelsResponse(false, List.of()), 1);
        }

        // Validar header de fragments
        if (buf.readableBytes() < 1 + HEADER_SIZE) return null;
        long fragments = Binary.read(buf, start + 1, ValueSize.UINT64);
        int offset = start + 1 + HEADER_SIZE;

        List<ChannelsItem> channels = new ArrayList<>();

        for (long f = 0; f < fragments; f++) {
            // Validar fragment ID + Q
            if (buf.readableBytes() < offset - start + FRAGMENT_HEADER_SIZE) return null;

            offset += 8; // fragment ID (ignored)
            long q = Binary.read(buf, offset, ValueSize.UINT64);
            offset += 8;

            // Validar entradas
            int entriesSize = Math.toIntExact(q) * ENTRY_SIZE;
            if (buf.readableBytes() < offset - start + entriesSize) return null;

            List<Byte> sizes = new ArrayList<>();
            List<Long> read = new ArrayList<>();
            List<Long> write = new ArrayList<>();
            List<Long> subs = new ArrayList<>();
            int totalQL = 0;

            for (int c = 0; c < q; c++) {
                byte ql = buf.getByte(offset); offset++;
                sizes.add(ql);
                totalQL += Byte.toUnsignedInt(ql);

                read.add(Binary.read(buf, offset, ValueSize.UINT64)); offset += 8;
                write.add(Binary.read(buf, offset, ValueSize.UINT64)); offset += 8;
                subs.add(Binary.read(buf, offset, ValueSize.UINT64)); offset += 8;
            }

            // Validar que todos los nombres estén
            if (buf.readableBytes() < offset - start + totalQL) return null;

            for (int c = 0; c < q; c++) {
                int len = Byte.toUnsignedInt(sizes.get(c));
                byte[] nameBytes = new byte[len];
                buf.getBytes(offset, nameBytes); offset += len;

                String name = new String(nameBytes, StandardCharsets.UTF_8);

                channels.add(new ChannelsItem(
                        name,
                        read.get(c),
                        write.get(c),
                        subs.get(c)
                ));
            }
        }

        int totalRead = offset - start;
        return new ReadResult(new ChannelsResponse(true, channels), totalRead);
    }
}
