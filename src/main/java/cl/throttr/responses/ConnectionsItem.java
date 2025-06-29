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

package cl.throttr.responses;

import cl.throttr.enums.ValueSize;
import cl.throttr.utils.Binary;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ConnectionsItem {
    public final String id;
    public final byte type;
    public final byte kind;
    public final byte ipVersion;
    public final byte[] ip;
    public final int port;
    public final long connectedAt;
    public final long readBytes;
    public final long writeBytes;
    public final long publishedBytes;
    public final long receivedBytes;
    public final long allocatedBytes;
    public final long consumedBytes;
    public final long insertRequests;
    public final long setRequests;
    public final long queryRequests;
    public final long getRequests;
    public final long updateRequests;
    public final long purgeRequests;
    public final long listRequests;
    public final long infoRequests;
    public final long statRequests;
    public final long statsRequests;
    public final long publishRequests;
    public final long subscribeRequests;
    public final long unsubscribeRequests;
    public final long connectionsRequests;
    public final long connectionRequests;
    public final long channelsRequests;
    public final long channelRequests;
    public final long whoamiRequests;

    public ConnectionsItem(
            String id, byte type, byte kind, byte ipVersion, byte[] ip, int port, long connectedAt,
            long readBytes, long writeBytes, long publishedBytes, long receivedBytes,
            long allocatedBytes, long consumedBytes,
            long insertRequests, long setRequests, long queryRequests, long getRequests,
            long updateRequests, long purgeRequests, long listRequests, long infoRequests,
            long statRequests, long statsRequests, long publishRequests, long subscribeRequests,
            long unsubscribeRequests, long connectionsRequests, long connectionRequests,
            long channelsRequests, long channelRequests, long whoamiRequests
    ) {
        this.id = id;
        this.type = type;
        this.kind = kind;
        this.ipVersion = ipVersion;
        this.ip = ip;
        this.port = port;
        this.connectedAt = connectedAt;
        this.readBytes = readBytes;
        this.writeBytes = writeBytes;
        this.publishedBytes = publishedBytes;
        this.receivedBytes = receivedBytes;
        this.allocatedBytes = allocatedBytes;
        this.consumedBytes = consumedBytes;
        this.insertRequests = insertRequests;
        this.setRequests = setRequests;
        this.queryRequests = queryRequests;
        this.getRequests = getRequests;
        this.updateRequests = updateRequests;
        this.purgeRequests = purgeRequests;
        this.listRequests = listRequests;
        this.infoRequests = infoRequests;
        this.statRequests = statRequests;
        this.statsRequests = statsRequests;
        this.publishRequests = publishRequests;
        this.subscribeRequests = subscribeRequests;
        this.unsubscribeRequests = unsubscribeRequests;
        this.connectionsRequests = connectionsRequests;
        this.connectionRequests = connectionRequests;
        this.channelsRequests = channelsRequests;
        this.channelRequests = channelRequests;
        this.whoamiRequests = whoamiRequests;
    }

    public static ConnectionsItem fromBytes(byte[] data) {
        ByteBuf buf = Unpooled.wrappedBuffer(data);
        int i = 0;

        byte[] idBytes = new byte[16];
        buf.getBytes(i, idBytes); i += 16;
        StringBuilder idBuilder = new StringBuilder(32);
        for (byte b : idBytes) {
            idBuilder.append(String.format("%02x", b));
        }
        String id = idBuilder.toString();

        byte type = buf.getByte(i++);
        byte kind = buf.getByte(i++);
        byte ipVersion = buf.getByte(i++);

        byte[] ip = new byte[16];
        buf.getBytes(i, ip); i += 16;

        int port = buf.getUnsignedShortLE(i); i += 2;

        long connectedAt     = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        long readBytes       = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        long writeBytes      = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        long publishedBytes  = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        long receivedBytes   = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        long allocatedBytes  = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        long consumedBytes   = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        long insertRequests  = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        long setRequests     = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        long queryRequests   = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        long getRequests     = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        long updateRequests  = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        long purgeRequests   = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        long listRequests    = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        long infoRequests    = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        long statRequests    = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        long statsRequests   = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        long publishRequests = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        long subscribeRequests = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        long unsubscribeRequests = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        long connectionsRequests = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        long connectionRequests = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        long channelsRequests = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        long channelRequests = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        long whoamiRequests = Binary.read(buf, i, ValueSize.UINT64);

        return new ConnectionsItem(
                id, type, kind, ipVersion, ip, port, connectedAt,
                readBytes, writeBytes, publishedBytes, receivedBytes,
                allocatedBytes, consumedBytes,
                insertRequests, setRequests, queryRequests, getRequests,
                updateRequests, purgeRequests, listRequests, infoRequests,
                statRequests, statsRequests, publishRequests, subscribeRequests,
                unsubscribeRequests, connectionsRequests, connectionRequests,
                channelsRequests, channelRequests, whoamiRequests
        );
    }
}
