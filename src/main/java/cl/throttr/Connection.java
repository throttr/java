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
import cl.throttr.requests.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Connection implements AutoCloseable {
    private final ValueSize size;
    private final Channel channel;
    private final EventLoopGroup group;
    private final Queue<PendingRequest> pending = new ConcurrentLinkedQueue<>();
    private final ByteBufAccumulator accumulator;

    public Connection(String host, int port, ValueSize size) throws IOException {
        this.size = size;
        this.accumulator = new ByteBufAccumulator(this.pending, size);
        this.group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new LoggingHandler(LogLevel.ERROR))
                                    .addLast(accumulator);
                        }
                    });

            ChannelFuture future = bootstrap.connect(host, port).sync();
            this.channel = future.channel();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Failed to connect", e);
        }
    }

    public Object send(Object request) throws IOException {
        // Detect if connection is alive
        if (!channel.isActive()) {
            throw new IOException("Socket is already closed");
        }

        // If is a batch of <T>
        if (request instanceof List<?> list) {
            // Create a buffer to merge requests
            ByteArrayOutputStream totalBuffer = new ByteArrayOutputStream();

            // Capture the types
            List<Integer> types = new ArrayList<>();

            // Per request
            for (Object req : list) {
                // Build his buffer
                byte[] buffer = getRequestBuffer(req, size);
                // Write that buffer inside merged buffer
                totalBuffer.writeBytes(buffer);
                // Push type
                types.add(Byte.toUnsignedInt(buffer[0]));
            }

            // This convert totalBuffer into a consumable array of bytes
            byte[] finalBuffer = totalBuffer.toByteArray();

            // Build a futures to resolve here
            List<CompletableFuture<Object>> futures = new ArrayList<>();

            // By request
            for (int type : types) {
                // Build a completable future of Object
                CompletableFuture<Object> f = new CompletableFuture<>();

                // Add pending function as pending request
                pending.add(new PendingRequest(f, type));
                // Push this function to the local promises array
                futures.add(f);
            }

            // Write
            channel.writeAndFlush(Unpooled.wrappedBuffer(finalBuffer)).syncUninterruptibly();

            // Generate a responses as a list
            List<Object> responses = new ArrayList<>();

            // One by one on pending requests
            for (CompletableFuture<Object> f : futures) {
                try {
                    // Try to resolve and push as response
                    responses.add(f.get());
                } catch (Exception e) {
                    throw new IOException("Failed while awaiting response", e);
                }
            }
            // Return the response batch
            return responses;
        }

        // Build the buffer
        byte[] buffer = getRequestBuffer(request, size);

        // Make a completable future for the response object
        CompletableFuture<Object> future = new CompletableFuture<>();

        // Add the future to the pending queue
        int type = Byte.toUnsignedInt(buffer[0]);
        pending.add(new PendingRequest(future, type));

        // Write
        channel.writeAndFlush(Unpooled.wrappedBuffer(buffer)).syncUninterruptibly();

        try {
            // Return the response object
            return future.get();
        } catch (Exception e) {
            throw new IOException("Failed while awaiting response", e);
        }
    }

    public static byte[] getRequestBuffer(Object request, ValueSize size) {
        return switch (request) {
            case InsertRequest insert -> insert.toBytes(size);
            case QueryRequest query -> query.toBytes();
            case UpdateRequest update -> update.toBytes(size);
            case PurgeRequest purge -> purge.toBytes();
            case SetRequest set -> set.toBytes(size);
            case GetRequest get -> get.toBytes();
            case ListRequest list -> list.toBytes();
            case InfoRequest info -> info.toBytes();
            case StatRequest stat -> stat.toBytes();
            case StatsRequest stats -> stats.toBytes();
            case ConnectionsRequest connections -> connections.toBytes();
            case ConnectionRequest connection -> connection.toBytes();
            case ChannelsRequest channels -> channels.toBytes();
            case ChannelRequest channel -> channel.toBytes();
            case WhoAmiRequest whoami -> whoami.toBytes();
            case null, default -> throw new IllegalArgumentException("Unsupported request type");
        };
    }

    @Override
    public void close() {
        try {
            if (channel != null) channel.close().sync();
        } catch (InterruptedException ignored) {
        }
        group.shutdownGracefully();
    }
}
