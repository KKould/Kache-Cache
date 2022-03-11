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

    /**
     * 可能你会发现一个很奇怪的问题：为什么decodeValue没有对String进行处理，
     * 而encodeValue则会对String进行UTF-8的编码
     *
     * 因为Kache的存储方式的K绝对为String，而Value是String与Object混杂的，而其中获取Value时，
     * 值都不会为String，因为String在lua脚本中就进行处理了，不需要返回String，所以反序列化时则视为Object
     * @param o
     * @return ByteBuffer
     */
    @Override
    public ByteBuffer encodeValue(Object o) {
        if (o instanceof String) {
            return StandardCharsets.UTF_8.encode((String) o);
        } else {
            return ByteBuffer.wrap(KryoUtil.writeToByteArray(o));
        }
    }

}
