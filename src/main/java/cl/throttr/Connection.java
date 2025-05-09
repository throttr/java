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

import cl.throttr.enums.RequestType;
import cl.throttr.enums.ValueSize;
import cl.throttr.requests.*;
import cl.throttr.responses.FullResponse;
import cl.throttr.responses.SimpleResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class Connection implements AutoCloseable {
    private final String host;
    private final int port;
    private final ValueSize size;
    private final EventLoopGroup group;
    private Channel channel;
    private final Queue<PendingRequest> pending = new ConcurrentLinkedQueue<>();

    private static class PendingRequest {
        final boolean expectFull;
        final CompletableFuture<Object> future;

        PendingRequest(boolean expectFull, CompletableFuture<Object> future) {
            this.expectFull = expectFull;
            this.future = future;
        }
    }

    public Connection(String host, int port, ValueSize size) throws InterruptedException {
        this.host = host;
        this.port = port;
        this.size = size;
        this.group = new NioEventLoopGroup(1);

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new ReadTimeoutHandler(30));
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                                byte[] data = new byte[msg.readableBytes()];
                                msg.readBytes(data);

                                PendingRequest req = pending.poll();
                                if (req == null) {
                                    System.err.println("⚠️ Netty error: unexpected response without pending request");
                                    return;
                                }
                                try {
                                    if (req.expectFull && data[0] == 0x01) {
                                        req.future.complete(FullResponse.fromBytes(data, size));
                                    } else {
                                        req.future.complete(new SimpleResponse(data[0] == 0x01));
                                    }
                                } catch (Throwable t) {
                                    req.future.completeExceptionally(t);
                                }
                            }
                        });
                    }
                });

        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port)).sync();
        this.channel = future.channel();
    }

    public Object send(Object request) throws Exception {
        byte[] buffer;
        boolean expectFull;

        switch (request) {
            case InsertRequest insert -> {
                buffer = insert.toBytes(size);
                expectFull = false;
            }
            case QueryRequest query -> {
                buffer = query.toBytes();
                expectFull = true;
            }
            case UpdateRequest update -> {
                buffer = update.toBytes(size);
                expectFull = false;
            }
            case PurgeRequest purge -> {
                buffer = purge.toBytes();
                expectFull = false;
            }
            default -> throw new IllegalArgumentException("Unsupported request: " + request.getClass());
        }

        CompletableFuture<Object> future = new CompletableFuture<>();
        pending.add(new PendingRequest(expectFull, future));
        channel.writeAndFlush(Unpooled.wrappedBuffer(buffer)).sync();
        return future.get(30, TimeUnit.SECONDS);
    }

    @Override
    public void close() {
        if (channel != null) {
            channel.close();
        }
        group.shutdownGracefully();
    }
}