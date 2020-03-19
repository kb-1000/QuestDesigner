package questdesigner;

import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.IllegalFormatException;

public final class BinaryFormatReader {
    private BinaryFormatReader() {
        throw new UnsupportedOperationException("no instances for you!");
    }

    public static TextItem read(DataInput in) throws IOException {
        int i;
        switch (i = readInt(in)) {
            default: throw new UnsupportedOperationException("Format version " + i + " unsupported");
            case 0:
                final TextItem textItem = new TextItem();
                byte[] responseBytes = new byte[readInt(in)];
                in.readFully(responseBytes);
                textItem.setResponse(responseBytes.length == 0 ? null : new String(responseBytes, StandardCharsets.UTF_8));
                return textItem;
        }
    }

    static int readInt(DataInput in) throws IOException {
        int i = in.readByte() & 0xFF;
        if ((i & 0b10000000) == 0) {
            return i;
        } else {
            int i2 = in.readByte() & 0xFF;
            if ((i2 & 0b10000000) == 0) {
                return ((i & 0b0111111) << 7) | i2;
            } else {
                throw new UnsupportedOperationException("reading 3-byte ints not supported yet");
            }
        }
    }
}
