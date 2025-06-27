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
import cl.throttr.enums.TTLType;
import cl.throttr.enums.ValueSize;
import cl.throttr.requests.*;
import cl.throttr.responses.*;
import cl.throttr.utils.Binary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Connection
 */
public class Connection implements AutoCloseable {
    /**
     * Socket
     */
    private final Socket socket;

    /**
     * Size
     */
    private final ValueSize size;

    /**
     * Out
     */
    private final OutputStream out;

    /**
     * In
     */
    private final InputStream in;

    /**
     * Constructor
     *
     * @param host
     * @param port
     * @param size
     * @throws IOException
     */
    public Connection(String host, int port, ValueSize size) throws IOException {
        this.socket = new Socket(host, port);
        this.socket.setTcpNoDelay(true);
        this.out = socket.getOutputStream();
        this.in = socket.getInputStream();
        this.size = size;
    }

    /**
     * Send
     *
     * @param request
     * @return
     * @throws IOException
     */
    public Object send(Object request) throws IOException {
        if (socket.isClosed()) {
            throw new IOException("Socket is already closed");
        }

        if (request instanceof List<?> list) {
            ByteArrayOutputStream totalBuffer = new ByteArrayOutputStream();
            List<Integer> types = new ArrayList<>();

            for (Object req : list) {
                byte[] buffer = getRequestBuffer(req, size);
                totalBuffer.write(buffer);
                types.add(Byte.toUnsignedInt(buffer[0]));
            }

            byte[] finalBuffer = totalBuffer.toByteArray();
            out.write(finalBuffer);
            out.flush();

            List<Object> responses = new ArrayList<>();
            for (int type : types) {
                int head = in.read();
                if (head == -1) {
                    throw new IOException("Connection closed while reading response.");
                }

                Object response = switch (type) {
                    case 0x02 -> readQueryResponse(head);
                    case 0x06 -> readGetResponse(head);
                    case 0x07 -> readListResponse(head);
                    case 0x08 -> readInfoResponse(head);
                    case 0x09 -> readStatResponse(head);
                    case 0x10 -> readStatsResponse(head);
                    default -> readStatusResponse(head);
                };
                responses.add(response);
            }

            return responses;
        }

        byte[] buffer = getRequestBuffer(request, size);

        out.write(buffer);
        out.flush();

        int head = in.read();
        if (head == -1) {
            throw new IOException("Connection closed while reading response head.");
        }

        int type = Byte.toUnsignedInt(buffer[0]);

        return switch (type) {
            case 0x02 -> readQueryResponse(head);
            case 0x06 -> readGetResponse(head);
            case 0x07 -> readListResponse(head);
            case 0x08 -> readInfoResponse(head);
            case 0x09 -> readStatResponse(head);
            case 0x10 -> readStatsResponse(head);
            default -> readStatusResponse(head);
        };
    }

    private ListResponse readListResponse(int head) throws IOException {
        if (head != 0x01) {
            return new ListResponse(false, new ArrayList<>());
        }

        List<ListItem> items = new ArrayList<>();

        byte[] header = new byte[8];
        int offset = 0;
        while (offset < 8) {
            int read = in.read(header, offset, 8 - offset);
            if (read == -1) throw new IOException("Unexpected EOF while reading fragments count");
            offset += read;
        }

        ByteBuffer hb = ByteBuffer.wrap(header).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        long fragments = hb.getLong();

        for (long f = 0; f < fragments; f++) {
            byte[] skip = new byte[8];
            in.read(skip);

            // Leer cantidad de claves
            byte[] keysHeader = new byte[8];
            in.read(keysHeader);
            ByteBuffer khb = ByteBuffer.wrap(keysHeader).order(java.nio.ByteOrder.LITTLE_ENDIAN);
            long keysInFragment = khb.getLong();

            // Leer headers de claves
            List<ListItem> scopedItems = new ArrayList<>();
            List<Integer> keyLengths = new ArrayList<>();
            int perKeyHeader = 3 + 8 + size.getValue(); // key_length, key_type, ttl_type, expires_at, bytes_used

            byte[] keyHeaderBuffer = new byte[(int) keysInFragment * perKeyHeader];
            offset = 0;
            while (offset < keyHeaderBuffer.length) {
                int read = in.read(keyHeaderBuffer, offset, keyHeaderBuffer.length - offset);
                if (read == -1) throw new IOException("Unexpected EOF while reading key headers");
                offset += read;
            }

            ByteBuffer kb = ByteBuffer.wrap(keyHeaderBuffer).order(java.nio.ByteOrder.LITTLE_ENDIAN);

            for (int i = 0; i < keysInFragment; i++) {
                int keyLength = Byte.toUnsignedInt(kb.get());
                int keyType = Byte.toUnsignedInt(kb.get());
                int ttlType = Byte.toUnsignedInt(kb.get());
                long expiresAt = kb.getLong();
                long bytesUsed = Binary.read(kb, size);

                scopedItems.add(new ListItem(
                        "", keyLength, keyType, ttlType, expiresAt, bytesUsed
                ));
                keyLengths.add(keyLength);
            }

            // Leer claves reales
            for (int i = 0; i < scopedItems.size(); i++) {
                int len = keyLengths.get(i);
                byte[] key = new byte[len];
                offset = 0;
                while (offset < len) {
                    int read = in.read(key, offset, len - offset);
                    if (read == -1) throw new IOException("Unexpected EOF while reading key string");
                    offset += read;
                }
                scopedItems.get(i).setKey(new String(key));
            }

            items.addAll(scopedItems);
        }

        return new ListResponse(true, items);
    }

    private StatsResponse readStatsResponse(int head) throws IOException {
        if (head != 0x01) {
            return new StatsResponse(false, new ArrayList<>());
        }

        List<StatsItem> items = new ArrayList<>();

        // Leer fragment count
        byte[] header = new byte[8];
        int offset = 0;
        while (offset < 8) {
            int read = in.read(header, offset, 8 - offset);
            if (read == -1) throw new IOException("Unexpected EOF while reading fragments count");
            offset += read;
        }

        ByteBuffer hb = ByteBuffer.wrap(header).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        long fragments = hb.getLong();

        for (long f = 0; f < fragments; f++) {
            // Saltar timestamp
            in.read(new byte[8]);

            // Leer cantidad de claves
            byte[] countBuf = new byte[8];
            in.read(countBuf);
            ByteBuffer cb = ByteBuffer.wrap(countBuf).order(java.nio.ByteOrder.LITTLE_ENDIAN);
            long keysInFragment = cb.getLong();

            List<StatsItem> scopedItems = new ArrayList<>();
            List<Integer> keyLengths = new ArrayList<>();

            int perKeyHeader = 33; // 1 + 4 * 8

            byte[] keyHeaderBuffer = new byte[(int) keysInFragment * perKeyHeader];
            offset = 0;
            while (offset < keyHeaderBuffer.length) {
                int read = in.read(keyHeaderBuffer, offset, keyHeaderBuffer.length - offset);
                if (read == -1) throw new IOException("Unexpected EOF while reading stats headers");
                offset += read;
            }

            ByteBuffer kb = ByteBuffer.wrap(keyHeaderBuffer).order(java.nio.ByteOrder.LITTLE_ENDIAN);

            for (int i = 0; i < keysInFragment; i++) {
                int keyLength = Byte.toUnsignedInt(kb.get());
                long readsPerMin = kb.getLong();
                long writesPerMin = kb.getLong();
                long totalReads = kb.getLong();
                long totalWrites = kb.getLong();

                scopedItems.add(new StatsItem(
                        "", keyLength, readsPerMin, writesPerMin, totalReads, totalWrites
                ));
                keyLengths.add(keyLength);
            }

            for (int i = 0; i < scopedItems.size(); i++) {
                int len = keyLengths.get(i);
                byte[] key = new byte[len];
                offset = 0;
                while (offset < len) {
                    int read = in.read(key, offset, len - offset);
                    if (read == -1) throw new IOException("Unexpected EOF while reading key string");
                    offset += read;
                }
                scopedItems.get(i).setKey(new String(key));
            }

            items.addAll(scopedItems);
        }

        return new StatsResponse(true, items);
    }

    /**
     * Read info response
     *
     * @param head byte del encabezado
     * @return InfoResponse parseada desde el buffer
     * @throws IOException si ocurre un error al leer desde el stream
     */
    private InfoResponse readInfoResponse(int head) throws IOException {
        if (head != 0x01) {
            throw new IOException("Invalid head for INFO response: " + head);
        }

        int expected = 432;
        byte[] merged = new byte[expected];
        int offset = 0;

        while (offset < expected) {
            int read = in.read(merged, offset, expected - offset);
            if (read == -1) {
                throw new IOException("Unexpected EOF while reading INFO response.");
            }
            offset += read;
        }

        byte[] full = new byte[1 + expected];
        full[0] = (byte) head;
        System.arraycopy(merged, 0, full, 1, expected);

        return InfoResponse.fromBytes(full);
    }

    /**
     * Read stat response
     *
     * @param head
     * @return StatResponse
     * @throws IOException
     */
    private StatResponse readStatResponse(int head) throws IOException {
        if (head != 0x01) {
            return new StatResponse(false, 0, 0, 0, 0);
        }

        int expected = 8 * 4; // 4 campos uint64
        byte[] merged = new byte[expected];
        int offset = 0;

        while (offset < expected) {
            int read = in.read(merged, offset, expected - offset);
            if (read == -1) {
                throw new IOException("Unexpected EOF while reading STAT response.");
            }
            offset += read;
        }

        byte[] full = new byte[1 + expected];
        full[0] = (byte) head;
        System.arraycopy(merged, 0, full, 1, expected);

        return StatResponse.fromBytes(full);
    }

    /**
     * Get request buffer
     *
     * @param request
     * @param size
     * @return byte[]
     */
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
            case null, default -> throw new IllegalArgumentException("Unsupported request type");
        };
    }

    /**
     * Read full response
     *
     * @param head
     * @return StatusResponse
     * @throws IOException
     */
    private QueryResponse readQueryResponse(int head) throws IOException {
        if (head != 0x01) {
            return new QueryResponse(false, 0, TTLType.SECONDS, 0);
        }

        int expected = size.getValue() * 2 + 1;
        byte[] merged = new byte[expected];
        int offset = 0;

        while (offset < expected) {
            int read = in.read(merged, offset, expected - offset);
            if (read == -1) {
                throw new IOException("Unexpected EOF while reading full response.");
            }
            offset += read;
        }

        byte[] full = new byte[1 + expected];
        full[0] = (byte) head;
        System.arraycopy(merged, 0, full, 1, expected);

        return QueryResponse.fromBytes(full, size);
    }

    /**
     * Read get response
     *
     * @param head
     * @return GetResponse
     * @throws IOException
     */
    private GetResponse readGetResponse(int head) throws IOException {
        if (head != 0x01) {
            return new GetResponse(false, null, 0, null);
        }

        // Total: 1 byte (ttlType) + N bytes (ttl) + N bytes (valueSize)
        int headerSize = 1 + size.getValue() + size.getValue();
        byte[] header = new byte[headerSize];
        int offset = 0;

        while (offset < headerSize) {
            int read = in.read(header, offset, headerSize - offset);
            if (read == -1) {
                throw new IOException("Unexpected EOF while reading GET header");
            }
            offset += read;
        }

        ByteBuffer buffer = ByteBuffer.wrap(header);
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);

        TTLType.fromByte(buffer.get());
        Binary.read(buffer, size);
        long valueSize = Binary.read(buffer, size);

        if (valueSize > Integer.MAX_VALUE) {
            throw new IOException("Value too large to handle in memory: " + valueSize);
        }

        byte[] value = new byte[(int) valueSize];
        offset = 0;
        while (offset < value.length) {
            int read = in.read(value, offset, value.length - offset);
            if (read == -1) {
                throw new IOException("Unexpected EOF while reading GET value");
            }
            offset += read;
        }

        byte[] full = new byte[1 + header.length + value.length];
        full[0] = (byte) head;
        System.arraycopy(header, 0, full, 1, header.length);
        System.arraycopy(value, 0, full, 1 + header.length, value.length);

        return GetResponse.fromBytes(full, size);
    }

    /**
     * Read simple response
     *
     * @param head
     * @return StatusResponse
     * @throws IOException
     */
    private StatusResponse readStatusResponse(int head) {
        return new StatusResponse(head == 0x01);
    }

    /**
     * Close
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        socket.close();
    }
}