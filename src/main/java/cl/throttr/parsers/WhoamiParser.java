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
import cl.throttr.responses.WhoamiResponse;
import io.netty.buffer.ByteBuf;

public class WhoamiParser implements ResponseParser {
    private static final int TOTAL_SIZE = 1 + 16;

    @Override
    public ReadResult tryParse(ByteBuf buf) {
        int index = buf.readerIndex();
        if (buf.readableBytes() < TOTAL_SIZE) return null;

        byte[] uuid = new byte[16];
        buf.getBytes(index + 1, uuid);
        return new ReadResult(new WhoamiResponse(true, uuid), TOTAL_SIZE);
    }
}