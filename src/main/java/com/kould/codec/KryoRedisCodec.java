package com.kould.codec;

import com.kould.utils.KryoUtil;
import io.lettuce.core.codec.RedisCodec;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class KryoRedisCodec implements RedisCodec<String, Object> {

    private static final byte OTHER_TYPE = (byte) 1;

    @Override
    public String decodeKey(ByteBuffer byteBuffer) {
        return StandardCharsets.US_ASCII.decode(byteBuffer).toString();
    }

    @Override
    public Object decodeValue(ByteBuffer byteBuffer) {
        byte[] array = byteBuffer.array();
        byte type = array[0];
        byte[] copy = new byte[array.length - 1];
        System.arraycopy(array, 1, copy,0 ,copy.length);
        if (type != OTHER_TYPE) {
            return StandardCharsets.US_ASCII.decode(byteBuffer).toString();
        } else {
            return KryoUtil.readFromByteArray(copy);
        }
    }

    @Override
    public ByteBuffer encodeKey(String s) {
        return StandardCharsets.US_ASCII.encode(s);
    }

    @Override
    public ByteBuffer encodeValue(Object o) {
        if (o instanceof String) {
            return StandardCharsets.US_ASCII.encode((String) o);
        } else {
            byte[] bytes = KryoUtil.writeToByteArray(o);
            byte[] copy = new byte[bytes.length + 1];
            System.arraycopy(bytes, 0, copy,1 ,copy.length);
            copy[0] = OTHER_TYPE;
            return ByteBuffer.wrap(copy);
        }
    }

}
