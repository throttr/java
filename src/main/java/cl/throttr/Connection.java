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

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class Connection implements AutoCloseable {
    private final ValueSize size;
    private final Channel channel;
    private final EventLoopGroup group;
    private final Queue<PendingRequest> pending = new ConcurrentLinkedQueue<>();
    private final Map<String, Consumer<String>> subscriptions = new ConcurrentHashMap<>();
    private final ByteBufAccumulator accumulator;

    public Connection(String host, int port, ValueSize size) throws InterruptedException {
        this.size = size;
        this.accumulator = new ByteBufAccumulator(this.pending, this.subscriptions, size);
        this.group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(accumulator);
                    }
                });

        ChannelFuture future = bootstrap.connect(host, port).sync();
        this.channel = future.channel();
    }

    public Object send(Object request) throws IOException, InterruptedException, ExecutionException {
        return Dispatcher.dispatch(channel, pending, request, size);
    }

    public void subscribe(String name, Consumer<String> callback) {
        subscriptions.put(name, callback);

        SubscribeRequest request = new SubscribeRequest(name);
        byte[] buffer = request.toBytes();

        channel.writeAndFlush(Unpooled.wrappedBuffer(buffer)).syncUninterruptibly();
    }

    public void unsubscribe(String name) {
        subscriptions.remove(name);

        UnsubscribeRequest request = new UnsubscribeRequest(name);
        byte[] buffer = request.toBytes();

        channel.writeAndFlush(Unpooled.wrappedBuffer(buffer)).syncUninterruptibly();
    }

    @Override
    public void close() throws InterruptedException {
        if (channel != null) channel.close().sync();
        group.shutdownGracefully();
    }
}
