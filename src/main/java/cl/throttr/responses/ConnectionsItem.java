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
            String id, byte type, byte kind, byte ipVersion, byte[] ip, int port, long[] v
    ) {
        this.id = id;
        this.type = type;
        this.kind = kind;
        this.ipVersion = ipVersion;
        this.ip = ip;
        this.port = port;
        this.connectedAt = v[0];
        this.readBytes = v[1];
        this.writeBytes = v[2];
        this.publishedBytes = v[3];
        this.receivedBytes = v[4];
        this.allocatedBytes = v[5];
        this.consumedBytes = v[6];
        this.insertRequests = v[7];
        this.setRequests = v[8];
        this.queryRequests = v[9];
        this.getRequests = v[10];
        this.updateRequests = v[11];
        this.purgeRequests = v[12];
        this.listRequests = v[13];
        this.infoRequests = v[14];
        this.statRequests = v[15];
        this.statsRequests = v[16];
        this.publishRequests = v[17];
        this.subscribeRequests = v[18];
        this.unsubscribeRequests = v[19];
        this.connectionsRequests = v[20];
        this.connectionRequests = v[21];
        this.channelsRequests = v[22];
        this.channelRequests = v[23];
        this.whoamiRequests = v[24];
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

        long[] v = new long[25];
        long connectedAt     = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        v[0] = connectedAt;
        long readBytes       = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        v[1] = readBytes;
        long writeBytes      = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        v[2] = writeBytes;
        long publishedBytes  = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        v[3] = publishedBytes;
        long receivedBytes   = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        v[4] = receivedBytes;
        long allocatedBytes  = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        v[5] = allocatedBytes;
        long consumedBytes   = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        v[6] = consumedBytes;
        long insertRequests  = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        v[7] = insertRequests;
        long setRequests     = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        v[8] = setRequests;
        long queryRequests   = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        v[9] = queryRequests;
        long getRequests     = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        v[10] = getRequests;
        long updateRequests  = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        v[11] = updateRequests;
        long purgeRequests   = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        v[12] = purgeRequests;
        long listRequests    = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        v[13] = listRequests;
        long infoRequests    = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        v[14] = infoRequests;
        long statRequests    = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        v[15] = statRequests;
        long statsRequests   = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        v[16] = statsRequests;
        long publishRequests = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        v[17] = publishRequests;
        long subscribeRequests = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        v[18] = subscribeRequests;
        long unsubscribeRequests = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        v[19] = unsubscribeRequests;
        long connectionsRequests = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        v[20] = connectionsRequests;
        long connectionRequests = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        v[21] = connectionRequests;
        long channelsRequests = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        v[22] = channelsRequests;
        long channelRequests = Binary.read(buf, i, ValueSize.UINT64); i += 8;
        v[23] = channelRequests;
        long whoamiRequests = Binary.read(buf, i, ValueSize.UINT64);
        v[24] = whoamiRequests;

        return new ConnectionsItem(id, type, kind, ipVersion, ip, port, v);
    }
}
