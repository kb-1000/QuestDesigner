package questdesigner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Supplier;

public class BinaryFormatWriterTest {
    @Test
    public void test1ByteInt() throws IOException {
        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            try (final DataOutputStream dataOutputStream = new DataOutputStream(bos)) {
                BinaryFormatWriter.writeInt(dataOutputStream, 1);
            }
            Assertions.assertArrayEquals(bos.toByteArray(), new byte[]{1});
        }

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            try (final DataOutputStream dataOutputStream = new DataOutputStream(bos)) {
                BinaryFormatWriter.writeInt(dataOutputStream, 0b01111111);
            }
            Assertions.assertArrayEquals(bos.toByteArray(), new byte[]{0b01111111});
        }
    }

    @Test
    public void test2ByteInt() throws IOException {
        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            try (final DataOutputStream dataOutputStream = new DataOutputStream(bos)) {
                BinaryFormatWriter.writeInt(dataOutputStream, 0b11111111);
            }
            Assertions.assertArrayEquals(new byte[]{(byte) 0b10000001, (byte) 0b01111111}, bos.toByteArray());
        }
        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            try (final DataOutputStream dataOutputStream = new DataOutputStream(bos)) {
                BinaryFormatWriter.writeInt(dataOutputStream, 0b0011111111111111);
            }
            Assertions.assertArrayEquals(new byte[]{(byte) 0xFF, 0b01111111}, bos.toByteArray());
        }
    }

    @Test
    public void testWrite() throws IOException {
        {
            final TextItem textItem1 = new TextItem();
            final TextItem textItem2 = new TextItem();
            textItem1.getSubItems().add(textItem2);
            try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                try (final DataOutputStream dataOutputStream = new DataOutputStream(bos)) {
                    BinaryFormatWriter.write(dataOutputStream, textItem1);
                }
                Assertions.assertArrayEquals(new byte[]{0, 0, 1, 0, 0, 0, 0}, bos.toByteArray());
            }
        }
    }

    @Test
    public void testWriteNegativeInt() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                try (final DataOutputStream dataOutputStream = new DataOutputStream(bos)) {
                    BinaryFormatWriter.writeInt(dataOutputStream, -1);
                }
            }
        });
    }
}
