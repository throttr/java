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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static cl.throttr.utils.Binary.toHex;

public class Connection implements AutoCloseable {
    private final String host;
    private final int port;
    private final ValueSize size;
    private final EventLoopGroup group;
    private Channel channel;
    private final LinkedBlockingQueue<byte[]> responseQueue = new LinkedBlockingQueue<>();

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
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                ByteBuf buf = (ByteBuf) msg;
                                try {
                                    byte[] data = new byte[buf.readableBytes()];
                                    buf.readBytes(data);
                                    responseQueue.offer(data);
                                } finally {
                                    buf.release();
                                }
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                System.err.println("⚠️ Netty error: " + cause.getMessage());
                                ctx.close();
                            }
                        });
                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                        System.err.println("Netty handler exception: " + cause.getMessage());
                        ctx.close();
                    }
                });

        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port)).sync();
        this.channel = future.channel();
    }

    public Object send(Object request) throws Exception {
        byte[] buffer;
        boolean expectFull;
        RequestType type;

        switch (request) {
            case InsertRequest insert -> {
                buffer = insert.toBytes(size);
                expectFull = false;
                type = RequestType.INSERT;
            }
            case QueryRequest query -> {
                buffer = query.toBytes();
                expectFull = true;
                type = RequestType.QUERY;
            }
            case UpdateRequest update -> {
                buffer = update.toBytes(size);
                expectFull = false;
                type = RequestType.UPDATE;
            }
            case PurgeRequest purge -> {
                buffer = purge.toBytes();
                expectFull = false;
                type = RequestType.PURGE;
            }
            default -> throw new IllegalArgumentException("Unsupported request: " + request.getClass());
        }

        if (channel == null || !channel.isActive()) {
            throw new IOException("Channel is not active or was closed");
        }
        responseQueue.clear();

        channel.writeAndFlush(Unpooled.wrappedBuffer(buffer)).sync();
        byte[] response = responseQueue.poll(30, TimeUnit.SECONDS);

        if (response == null) {
            throw new RuntimeException("Timeout waiting for response");
        }

        if (expectFull && response[0] == 0x01) {
            return FullResponse.fromBytes(response, size);
        } else {
            return new SimpleResponse(response[0] == 0x01);
        }
    }

    @Override
    public void close() {
        if (channel != null) {
            channel.close();
        }
        group.shutdownGracefully();
    }
}
