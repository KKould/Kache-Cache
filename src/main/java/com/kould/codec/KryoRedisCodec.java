package com.kould.codec;

import com.kould.utils.KryoUtil;
import io.lettuce.core.codec.RedisCodec;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class KryoRedisCodec implements RedisCodec<String, Object> {

    @Override
    public String decodeKey(ByteBuffer byteBuffer) {
        return StandardCharsets.UTF_8.decode(byteBuffer).toString();
    }

    @Override
    public Object decodeValue(ByteBuffer byteBuffer) {
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        return KryoUtil.readFromByteArray(bytes);
    }

    @Override
    public ByteBuffer encodeKey(String s) {
        return StandardCharsets.UTF_8.encode(s);
    }

    @Override
    public ByteBuffer encodeValue(Object o) {
        return ByteBuffer.wrap(KryoUtil.writeToByteArray(o));
    }

}
