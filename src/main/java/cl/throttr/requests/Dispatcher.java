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

package cl.throttr.requests;

import cl.throttr.enums.ValueSize;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class Dispatcher {

    private Dispatcher() {}

    public static Object dispatch(Channel channel, Queue<PendingRequest> pending, Object request, ValueSize size) throws IOException, InterruptedException, ExecutionException {
        if (!channel.isActive()) {
            throw new IOException("Socket is already closed");
        }

        if (request instanceof List<?> list) {
            return dispatchBatch(channel, pending, list, size);
        }

        return dispatchSingle(channel, pending, request, size);
    }

    private static Object dispatchBatch(Channel channel, Queue<PendingRequest> pending, List<?> list, ValueSize size) throws InterruptedException, ExecutionException {
        ByteArrayOutputStream totalBuffer = new ByteArrayOutputStream();
        List<Integer> types = new ArrayList<>();

        for (Object req : list) {
            byte[] buffer = Serializer.invoke(req, size);
            totalBuffer.writeBytes(buffer);
            types.add(Byte.toUnsignedInt(buffer[0]));
        }

        byte[] finalBuffer = totalBuffer.toByteArray();
        List<CompletableFuture<Object>> futures = new ArrayList<>();

        for (int type : types) {
            CompletableFuture<Object> f = new CompletableFuture<>();
            pending.add(new PendingRequest(f, type));
            futures.add(f);
        }

        channel.writeAndFlush(Unpooled.wrappedBuffer(finalBuffer)).syncUninterruptibly();

        List<Object> responses = new ArrayList<>();
        for (CompletableFuture<Object> f : futures) {
            responses.add(f.get());
        }

        return responses;
    }

    private static Object dispatchSingle(Channel channel, Queue<PendingRequest> pending, Object request, ValueSize size) throws InterruptedException, ExecutionException {
        byte[] buffer = Serializer.invoke(request, size);
        CompletableFuture<Object> future = new CompletableFuture<>();
        int type = Byte.toUnsignedInt(buffer[0]);
        pending.add(new PendingRequest(future, type));

        channel.writeAndFlush(Unpooled.wrappedBuffer(buffer)).syncUninterruptibly();

        return future.get();
    }
}
