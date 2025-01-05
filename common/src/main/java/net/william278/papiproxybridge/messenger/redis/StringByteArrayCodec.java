package net.william278.papiproxybridge.messenger.redis;

import io.lettuce.core.codec.RedisCodec;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class StringByteArrayCodec implements RedisCodec<String, byte[]> {

    public static final StringByteArrayCodec INSTANCE = new StringByteArrayCodec();
    private static final byte[] EMPTY = new byte[0];
    private final Charset charset = Charset.forName("UTF-8");

    @Override
    public String decodeKey(final ByteBuffer bytes) {
        return charset.decode(bytes).toString();
    }

    @Override
    public byte[] decodeValue(final ByteBuffer bytes) {
        return getBytes(bytes);
    }

    @Override
    public ByteBuffer encodeKey(final String key) {
        return charset.encode(key);
    }

    @Override
    public ByteBuffer encodeValue(final byte[] value) {
        if (value == null) {
            return ByteBuffer.wrap(EMPTY);
        }

        return ByteBuffer.wrap(value);
    }

    private static byte[] getBytes(final ByteBuffer buffer) {
        final byte[] b = new byte[buffer.remaining()];
        buffer.get(b);
        return b;
    }
}
