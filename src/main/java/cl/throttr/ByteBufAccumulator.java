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
import cl.throttr.utils.Binary;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;

public class ByteBufAccumulator extends SimpleChannelInboundHandler<ByteBuf> {
    private final Queue<PendingRequest> pending;
    private final Map<String, Consumer<String>> subscriptions;
    private final ValueSize size;
    private ByteBuf buffer;

    private final Map<Integer, ResponseParser> parsers;

    public ByteBufAccumulator(Queue<PendingRequest> pending, Map<String, Consumer<String>> subscriptions, ValueSize size) {
        this.pending = pending;
        this.subscriptions = subscriptions;
        this.size = size;
        this.parsers = Map.ofEntries(
                Map.entry(0x01, new StatusParser()),
                Map.entry(0x02, new QueryParser(size)),
                Map.entry(0x03, new StatusParser()),
                Map.entry(0x04, new StatusParser()),
                Map.entry(0x05, new StatusParser()),
                Map.entry(0x06, new GetParser(size)),
                Map.entry(0x07, new ListParser(size)),
                Map.entry(0x08, new InfoParser()),
                Map.entry(0x09, new StatParser()),
                Map.entry(0x10, new StatsParser()),
                Map.entry(0x11, new StatusParser()), // SUBSCRIBE
                Map.entry(0x12, new StatusParser()), // UNSUBSCRIBE
                Map.entry(0x13, new StatusParser()), // PUBLISH
                Map.entry(0x14, new ConnectionsParser()),
                Map.entry(0x15, new ConnectionParser()),
                Map.entry(0x16, new ChannelsParser()),
                Map.entry(0x17, new ChannelParser()),
                Map.entry(0x18, new WhoamiParser())
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
            int type = buffer.getUnsignedByte(buffer.readerIndex());

            if (type == 0x19) {
                if (!handleChannelMessage()) return;
                continue;
            }

            if (!handlePendingRequest()) return;
        }
    }

    private boolean handleChannelMessage() {
        int readerIndex = buffer.readerIndex();

        if (buffer.readableBytes() < 1 + size.getValue()) return false;

        int channelSize = Byte.toUnsignedInt(buffer.getByte(readerIndex + 1));
        int headerSize = 1 + 1 + size.getValue() + channelSize;

        if (buffer.readableBytes() < headerSize) return false;

        long payloadLength = Binary.read(buffer, readerIndex + 2, size);
        if (buffer.readableBytes() < headerSize + payloadLength) return false;

        byte[] channelBytes = new byte[channelSize];
        buffer.getBytes(readerIndex + 2 + size.getValue(), channelBytes);
        String channel = new String(channelBytes);

        byte[] payloadBytes = new byte[(int) payloadLength];
        buffer.getBytes(readerIndex + headerSize, payloadBytes);
        String payload = new String(payloadBytes);

        buffer.readerIndex(readerIndex + headerSize + (int) payloadLength);

        Consumer<String> callback = subscriptions.get(channel);
        if (callback != null) {
            callback.accept(payload);
        }

        return true;
    }

    private boolean handlePendingRequest() {
        PendingRequest pendingRequest = pending.peek();
        if (pendingRequest == null) {
            buffer.resetReaderIndex();
            return false;
        }

        int expectedType = pendingRequest.type();
        ResponseParser parser = parsers.get(expectedType);
        ReadResult result = parser.tryParse(buffer);
        if (result == null) {
            buffer.resetReaderIndex();
            return false;
        }

        buffer.skipBytes(result.consumed());
        pending.poll().future().complete(result.value());
        return true;
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        if (buffer != null) buffer.release();
    }
}
