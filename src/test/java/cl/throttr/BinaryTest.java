package cl.throttr;

import cl.throttr.enums.ValueSize;
import cl.throttr.utils.Binary;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class BinaryTest {

    @Test
    void shouldWriteAndReadUInt8() {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        long value = 0xAB;
        Binary.put(buffer, value, ValueSize.UINT8);
        buffer.flip();
        long result = Binary.read(buffer, ValueSize.UINT8);
        assertEquals(value, result);
    }

    @Test
    void shouldWriteAndReadUInt16() {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        long value = 0xABCD;
        Binary.put(buffer, value, ValueSize.UINT16);
        buffer.flip();
        long result = Binary.read(buffer, ValueSize.UINT16);
        assertEquals(value, result);
    }

    @Test
    void shouldWriteAndReadUInt32() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        long value = 0xDEADBEEFL;
        Binary.put(buffer, value, ValueSize.UINT32);
        buffer.flip();
        long result = Binary.read(buffer, ValueSize.UINT32);
        assertEquals(value, result);
    }

    @Test
    void shouldWriteAndReadUInt64() {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        long value = 0x0123456789ABCDEFL;
        Binary.put(buffer, value, ValueSize.UINT64);
        buffer.flip();
        long result = Binary.read(buffer, ValueSize.UINT64);
        assertEquals(value, result);
    }

    @Test
    void shouldReadUInt8FromByteBuf() {
        ByteBuf buf = Unpooled.buffer(1);
        buf.writeByte(0xAB);
        long result = Binary.read(buf, 0, ValueSize.UINT8);
        assertEquals(0xAB, result);
    }

    @Test
    void shouldReadUInt16FromByteBuf() {
        ByteBuf buf = Unpooled.buffer(2);
        buf.writeByte(0xCD); // little endian: LSB first
        buf.writeByte(0xAB);
        long result = Binary.read(buf, 0, ValueSize.UINT16);
        assertEquals(0xABCD, result);
    }

    @Test
    void shouldReadUInt32FromByteBuf() {
        ByteBuf buf = Unpooled.buffer(4);
        buf.writeByte(0xEF);
        buf.writeByte(0xBE);
        buf.writeByte(0xAD);
        buf.writeByte(0xDE);
        long result = Binary.read(buf, 0, ValueSize.UINT32);
        assertEquals(0xDEADBEEFL, result);
    }

    @Test
    void shouldReadUInt64FromByteBuf() {
        ByteBuf buf = Unpooled.buffer(8);
        buf.writeLongLE(0x0123456789ABCDEFL);
        long result = Binary.read(buf, 0, ValueSize.UINT64);
        assertEquals(0x0123456789ABCDEFL, result);
    }
}