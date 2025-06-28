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

package cl.throttr;

import cl.throttr.enums.ValueSize;
import cl.throttr.parsers.*;
import cl.throttr.requests.PendingRequest;
import cl.throttr.responses.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

public class ByteBufAccumulator extends SimpleChannelInboundHandler<ByteBuf> {
    private final Queue<PendingRequest> pending;
    private ByteBuf buffer;

    private final Map<Integer, ResponseParser> parsers;

    public ByteBufAccumulator(Queue<PendingRequest> pending, ValueSize size) {
        this.pending = pending;
        this.parsers = Map.of(
                0x01, new StatusParser(), // INSERT
                0x02, new QueryParser(size), // QUERY
                0x03, new StatusParser(), // UPDATE
                0x04, new StatusParser(), // PURGE
                0x05, new StatusParser(), // SET
                0x06, new GetParser(size), // GET
                0x07, new ListParser(size), // LIST
                0x08, new InfoParser(), // INFO
                0x09, new StatParser(), // STAT
                0x10, new StatsParser() // STATS
        );
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf incoming) {
        if (buffer == null) {
            buffer = incoming.alloc().buffer();
        }
        buffer.writeBytes(incoming);

        while (true) {
            if (buffer.readableBytes() < 1) return;

            buffer.markReaderIndex();

            PendingRequest pendingRequest = pending.peek();
            if (pendingRequest == null) {
                buffer.resetReaderIndex();
                return;
            }

            int expectedType = pendingRequest.type();
            ResponseParser parser = parsers.get(expectedType);
            if (parser == null) {
                buffer.resetReaderIndex();
                throw new IllegalArgumentException("Unknown response type: " + expectedType);
            }

            ReadResult result = parser.tryParse(buffer);
            if (result == null) {
                buffer.resetReaderIndex();
                return;
            }

            buffer.skipBytes(result.consumed());
            pending.poll().future().complete(result.value());
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        if (buffer != null) buffer.release();
    }
}