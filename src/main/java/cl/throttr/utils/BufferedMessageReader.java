package cl.throttr.utils;

import java.io.IOException;
import java.io.InputStream;

public class BufferedMessageReader {
    private final InputStream in;

    public BufferedMessageReader(InputStream in) {
        this.in = in;
    }

    public byte[] readFully(int totalBytes) throws IOException {
        byte[] buffer = new byte[totalBytes];
        int offset = 0;

        while (offset < totalBytes) {
            int r = in.read(buffer, offset, totalBytes - offset);
            if (r == -1) {
                throw new IOException("Socket closed before reading full message (" + offset + "/" + totalBytes + ")");
            }
            offset += r;
            System.out.println("Leyó " + r + " bytes (acumulado: " + offset + "/" + totalBytes + ")");
        }

        return buffer;
    }

    public byte[] readWithHeadAndTail(int tailSize) throws IOException {
        byte[] head = readFully(1); // siempre 1 byte primero
        byte[] tail = readFully(tailSize); // luego el resto
        byte[] combined = new byte[1 + tailSize];

        combined[0] = head[0];
        System.arraycopy(tail, 0, combined, 1, tailSize);

        return combined;
    }
}