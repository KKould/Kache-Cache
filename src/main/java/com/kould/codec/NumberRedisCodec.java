package com.kould.codec;

import io.lettuce.core.codec.RedisCodec;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;

class NumberRedisCodec implements RedisCodec<String, Number> {

    @Override
    public String decodeKey(ByteBuffer bytes) {
        return new String(bytes.array(), StandardCharsets.UTF_8);
    }

    @Override
    public Number decodeValue(ByteBuffer bytes) {
        NumberClassEnum enumValue = NumberClassEnum.forID(bytes.get(0));
        switch (enumValue) {
            case BYTE:
                return bytes.get(1);
            case SHORT:
                return bytes.getShort(1);
            case INT:
                return bytes.getInt(1);
            case LONG:
                return bytes.getLong(1);
            case FLOAT:
                return bytes.getFloat(1);
            case DOUBLE:
                return bytes.getDouble(1);
            default:
                throw new IllegalArgumentException("Number type not supported"); //should be unreachable
        }
    }

    @Override
    public ByteBuffer encodeKey(String key) {
        return ByteBuffer.wrap(key.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public ByteBuffer encodeValue(Number value) {
        NumberClassEnum enumValue = NumberClassEnum.forClass(value.getClass());
        if (Objects.isNull(enumValue)) {
            throw new IllegalArgumentException("Number type not supported");
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(enumValue.numBytes + 1);
        byteBuffer.put(0, enumValue.id);
        switch (enumValue) {
            case BYTE:
                byteBuffer.put(value.byteValue());
                break;
            case SHORT:
                byteBuffer.putShort(value.shortValue());
                break;
            case INT:
                byteBuffer.putInt(value.intValue());
                break;
            case LONG:
                byteBuffer.putLong(value.longValue());
                break;
            case FLOAT:
                byteBuffer.putFloat(value.floatValue());
                break;
            case DOUBLE:
                byteBuffer.putDouble(value.doubleValue());
                break;
            default:
                throw new IllegalArgumentException("type not supported"); //should be unreachable
        }
        return byteBuffer;
    }

    private enum NumberClassEnum {
        BYTE(Byte.class, 1, (byte) 1),
        SHORT(Short.class, 2, (byte) 2),
        INT(Integer.class, 4, (byte) 3),
        LONG(Long.class, 8, (byte) 4),
        FLOAT(Float.class, 4, (byte) 5),
        DOUBLE(Double.class, 8, (byte) 6);

        public final Class<?> numberClass;
        public final int numBytes;
        public final byte id;

        private static final HashMap<Byte, NumberClassEnum> enumIDMap;
        private static final HashMap<Class<?>, NumberClassEnum> enumClassMap;

        static {
            enumIDMap = new HashMap<>();
            enumIDMap.put(BYTE.id, BYTE);
            enumIDMap.put(SHORT.id, SHORT);
            enumIDMap.put(INT.id, INT);
            enumIDMap.put(LONG.id, LONG);
            enumIDMap.put(FLOAT.id, FLOAT);
            enumIDMap.put(DOUBLE.id, DOUBLE);

            enumClassMap = new HashMap<>();
            enumClassMap.put(BYTE.numberClass, BYTE);
            enumClassMap.put(SHORT.numberClass, SHORT);
            enumClassMap.put(INT.numberClass, INT);
            enumClassMap.put(LONG.numberClass, LONG);
            enumClassMap.put(FLOAT.numberClass, FLOAT);
            enumClassMap.put(DOUBLE.numberClass, DOUBLE);
        }

        public static NumberClassEnum forID(byte id) {
            return enumIDMap.get(id);
        }

        public static NumberClassEnum forClass(Class<? extends Number> clazz) {
            return enumClassMap.get(clazz);
        }

        NumberClassEnum(Class<? extends Number> numberClass, int numBytes, byte id) {
            this.numberClass = numberClass;
            this.numBytes = numBytes;
            this.id = id;
        }
    }
}
