package com.kould.test;

import com.kould.encoder.CacheEncoder;
import com.kould.encoder.impl.BaseCacheEncoder;
import org.junit.Assert;
import org.junit.Test;

public class CacheEncoderTest {

    private final CacheEncoder cacheEncoder = BaseCacheEncoder.getInstance();

    private static final String TEST_STRING = "“民族主义”在基本性质上已经彻底沦为反动派维护其统治的意识形态工具";

    private static final Integer TEST_INTEGER = 12138;

    private static final Long TEST_LONG = 2815680434L;

    private static final Float TEST_FLOAT = 3.1415F;

    private static final Double TEST_DOUBLE = 3.14159265359D;

    private static final Byte TEST_BYTE = 21;

    private static final Object TEST_NULL = null;

    @Test
    public void argsEncodeTest() {
        String keyStr1 = cacheEncoder.argsEncode(TEST_STRING).intern();
        String keyInt1 = cacheEncoder.argsEncode(TEST_INTEGER).intern();
        String keyLong1 = cacheEncoder.argsEncode(TEST_LONG).intern();
        String keyFloat1 = cacheEncoder.argsEncode(TEST_FLOAT).intern();
        String keyDouble1 = cacheEncoder.argsEncode(TEST_DOUBLE).intern();
        String keyByte1 = cacheEncoder.argsEncode(TEST_BYTE).intern();
        String keyNull1 = cacheEncoder.argsEncode(TEST_NULL).intern();

        //值非空测试
        Assert.assertNotNull(keyStr1);
        Assert.assertNotNull(keyInt1);
        Assert.assertNotNull(keyLong1);
        Assert.assertNotNull(keyFloat1);
        Assert.assertNotNull(keyDouble1);
        Assert.assertNotNull(keyByte1);
        Assert.assertNotNull(keyNull1);

        //值重复匹配测试
        String keyStr2 = cacheEncoder.argsEncode(TEST_STRING).intern();
        String keyInt2 = cacheEncoder.argsEncode(TEST_INTEGER).intern();
        String keyLong2 = cacheEncoder.argsEncode(TEST_LONG).intern();
        String keyFloat2 = cacheEncoder.argsEncode(TEST_FLOAT).intern();
        String keyDouble2 = cacheEncoder.argsEncode(TEST_DOUBLE).intern();
        String keyByte2 = cacheEncoder.argsEncode(TEST_BYTE).intern();
        String keyNull2 = cacheEncoder.argsEncode(TEST_NULL).intern();
        Assert.assertSame(keyStr1, keyStr2);
        Assert.assertSame(keyInt1, keyInt2);
        Assert.assertSame(keyLong1, keyLong2);
        Assert.assertSame(keyFloat1, keyFloat2);
        Assert.assertSame(keyDouble1, keyDouble2);
        Assert.assertSame(keyByte1, keyByte2);
        Assert.assertSame(keyNull2, keyNull2);

    }
}
