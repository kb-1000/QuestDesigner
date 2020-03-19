package questdesigner;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.TestOnly;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class BinaryFormatWriter {
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private BinaryFormatWriter() {
        throw new UnsupportedOperationException("no instances for you!");
    }

    public static void write(@NotNull DataOutput out, @NotNull TextItem textItem) throws IOException {
        writeInt(out, 0); // format version
        writeSingle(out, textItem);
    }

    static void writeSingle(@NotNull DataOutput out, @NotNull TextItem textItem) throws IOException {
        @Nullable String response = textItem.getResponse();
        byte[] responseBytes = response != null ? response.getBytes(StandardCharsets.UTF_8) : EMPTY_BYTE_ARRAY;
        writeInt(out, responseBytes.length);
        out.write(responseBytes);
        writeInt(out, textItem.subItemsProperty().size());
        for (TextItem item : textItem.subItemsProperty()) {
            writeSingle(out, item);
        }
        @Nullable String text = textItem.getText();
        byte[] textBytes = text != null ? text.getBytes(StandardCharsets.UTF_8) : EMPTY_BYTE_ARRAY;
        writeInt(out, textBytes.length);
        out.write(textBytes);
    }

    static void writeInt(@NotNull DataOutput out, @Range(from = 0, to = Integer.MAX_VALUE) int value) throws IOException {
        if (value < 0) {
            throw new IllegalArgumentException();
        }
        if (((byte) value) == value) {
            out.writeByte(value);
        } else {
            switch (Integer.bitCount(value)) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    out.writeByte(value);
                    break;
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                    out.writeByte(0b10000000 | (0b01111111 & (value >>> 7)));
                    out.writeByte(0b01111111 & value);
                    break;
                default:
                    throw new UnsupportedOperationException("Number too big: " + value);
            }
        }
    }
}
