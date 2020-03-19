package questdesigner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.annotation.Native;

public class BinaryFormatTest {
    @Test
    public void testReadWriteInt() throws IOException {
        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            try (final DataOutputStream dataOutputStream = new DataOutputStream(bos)) {
                BinaryFormatWriter.writeInt(dataOutputStream, 0);
            }
            try (final ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray())) {
                try (final DataInputStream dataInputStream = new DataInputStream(bis)) {
                    Assertions.assertEquals(0, BinaryFormatReader.readInt(dataInputStream));
                }
            }
        }
        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            try (final DataOutputStream dataOutputStream = new DataOutputStream(bos)) {
                BinaryFormatWriter.writeInt(dataOutputStream, 0xFF);
            }
            try (final ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray())) {
                try (final DataInputStream dataInputStream = new DataInputStream(bis)) {
                    Assertions.assertEquals(0xFF, BinaryFormatReader.readInt(dataInputStream));
                }
            }
        }
        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            try (final DataOutputStream dataOutputStream = new DataOutputStream(bos)) {
                BinaryFormatWriter.writeInt(dataOutputStream, 0b01111111);
            }
            try (final ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray())) {
                try (final DataInputStream dataInputStream = new DataInputStream(bis)) {
                    Assertions.assertEquals(0b01111111, BinaryFormatReader.readInt(dataInputStream));
                }
            }
        }
    }

    @Test
    public void test3ByteInt() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                try (final DataOutputStream dataOutputStream = new DataOutputStream(bos)) {
                    BinaryFormatWriter.writeInt(dataOutputStream, 0xFFFF);
                }
                try (final ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray())) {
                    try (final DataInputStream dataInputStream = new DataInputStream(bis)) {
                        Assertions.assertEquals(0xFFFF, BinaryFormatReader.readInt(dataInputStream));
                    }
                }
            }
        });
    }
}
