package com.kould.encoder.impl;

import com.kould.annotation.DaoSelect;
import com.kould.config.KacheAutoConfig;
import com.kould.config.Status;
import com.kould.encoder.CacheEncoder;
import com.kould.utils.KryoUtil;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.Method;

public class BaseCacheEncoder extends CacheEncoder {

    private static final BaseCacheEncoder INSTANCE = new BaseCacheEncoder() ;

    private BaseCacheEncoder() {}

    public static BaseCacheEncoder getInstance() {
        return INSTANCE ;
    }

    private static final long P = 31L;

    private static final long HASH = 0L ;


    @Override
    public String encode(String MethodStatus, String daoEnityName, String methodName, String args) {
        return KacheAutoConfig.NO_ID_TAG +
                MethodStatus +
                daoEnityName +
                methodName +
                args ;

    }

    @Override
    public String argsEncode(Object... args) {
        //此处可以返回空但是空会导致同一Service方法内调用同一Dao方法且Dao方法的参数不一致时会导致缓存误差
        //需要寻找循环依赖序列化方案-》Mybatis-Plus的Wrapper
        //new:使用Kryo序列化
        return String.valueOf(getHash(KryoUtil.writeToByteArray(args)));
    }

    @Override
    public String getPattern(String poName) {
        return "*" + poName + "*" ;
    }

    @Override
    public String getDaoKey(ProceedingJoinPoint point, String methodName, Method method, Object args, String types) {
        //判断serviceMethod的是否为通过id获取数据
        //  若是则直接使用id进行获取
        //  若否则经过编码后进行获取
        //信息摘要收集
        //获取DAO方法签名
        if (methodName.equals(KacheAutoConfig.MYBATIS_PLUS_MAPPER_SELECT_BY_ID)) {
            return setKey2Id(point, types);
        }
        DaoSelect daoSelect = method.getAnnotation(DaoSelect.class);
        String methodStatus = null ;
        if (daoSelect != null) {
            methodStatus = daoSelect.status().getValue();
        } else {
            methodStatus = Status.BY_FIELD.getValue() ;
        }
        if (methodStatus.equals(KacheAutoConfig.SERVICE_BY_ID)) {
            //使Key为ID
            return setKey2Id(point,types);
        }else {
            String argsCode = argsEncode(args);
            //使Key为各个参数编码后的一个特殊值
            return encode(methodStatus, types, methodName, argsCode) ;
        }
    }

    @Override
    public String getServiceKey(ProceedingJoinPoint point, String methodName, Method method, Object args, String types) {
        String argsCode = argsEncode(point.getArgs());
        return encode(Status.BY_FIELD.getValue(), types, methodName, argsCode) ;
    }

    private String setKey2Id(ProceedingJoinPoint point, String type) {
        //使Key为ID
        Object[] args = point.getArgs();
        return type + args[0].toString();
    }

    private Object readResolve() {
        return INSTANCE;
    }

    private static long getHash(byte[] bytes){
        long hash = HASH;
        for (byte datum : bytes) {
            hash = P * hash + datum ;
        }
        return hash + bytes.length;
    }

    public static class SHA1 {
        private final int[] abcde = {
                0x67452301, 0xefcdab89, 0x98badcfe, 0x10325476, 0xc3d2e1f0
        };

        // 摘要数据存储数组
        private int[] digestInt = new int[5];

        // 计算过程中的临时数据存储数组
        private int[] tmpData = new int[80];

        // 计算sha-1摘要
        private int process_input_bytes(byte[] bytedata) {
            // 初试化常量
            System.arraycopy(abcde, 0, digestInt, 0, abcde.length);

            // 格式化输入字节数组，补10及长度数据
            byte[] newbyte = byteArrayFormatData(bytedata);

            // 获取数据摘要计算的数据单元个数
            int MCount = newbyte.length / 64;

            // 循环对每个数据单元进行摘要计算
            for (int pos = 0; pos < MCount; pos++) {
                // 将每个单元的数据转换成16个整型数据，并保存到tmpData的前16个数组元素中
                for (int j = 0; j < 16; j++) {
                    tmpData[j] = byteArrayToInt(newbyte, (pos * 64) + (j * 4));
                }

                // 摘要计算函数
                encrypt();
            }

            return 20;
        }

        // 格式化输入字节数组格式
        private byte[] byteArrayFormatData(byte[] bytedata) {
            // 补0数量
            int zeros = 0;

            // 补位后总位数
            int size = 0;

            // 原始数据长度
            int n = bytedata.length;

            // 模64后的剩余位数
            int m = n % 64;

            // 计算添加0的个数以及添加10后的总长度
            if (m < 56) {
                zeros = 55 - m;
                size = n - m + 64;
            } else if (m == 56) {
                zeros = 63;
                size = n + 8 + 64;
            } else {
                zeros = 63 - m + 56;
                size = (n + 64) - m + 64;
            }

            // 补位后生成的新数组内容
            byte[] newbyte = new byte[size];
            // 复制数组的前面部分
            System.arraycopy(bytedata, 0, newbyte, 0, n);

            // 获得数组Append数据元素的位置
            int l = n;
            // 补1操作
            newbyte[l++] = (byte) 0x80;

            // 补0操作
            for (int i = 0; i < zeros; i++) {
                newbyte[l++] = (byte) 0x00;
            }

            // 计算数据长度，补数据长度位共8字节，长整型
            long N = (long) n * 8;
            byte h8 = (byte) (N & 0xFF);
            byte h7 = (byte) ((N >> 8) & 0xFF);
            byte h6 = (byte) ((N >> 16) & 0xFF);
            byte h5 = (byte) ((N >> 24) & 0xFF);
            byte h4 = (byte) ((N >> 32) & 0xFF);
            byte h3 = (byte) ((N >> 40) & 0xFF);
            byte h2 = (byte) ((N >> 48) & 0xFF);
            byte h1 = (byte) (N >> 56);
            newbyte[l++] = h1;
            newbyte[l++] = h2;
            newbyte[l++] = h3;
            newbyte[l++] = h4;
            newbyte[l++] = h5;
            newbyte[l++] = h6;
            newbyte[l++] = h7;
            newbyte[l++] = h8;

            return newbyte;
        }

        private int f1(int x, int y, int z) {
            return (x & y) | (~x & z);
        }

        private int f2(int x, int y, int z) {
            return x ^ y ^ z;
        }

        private int f3(int x, int y, int z) {
            return (x & y) | (x & z) | (y & z);
        }

        private int f4(int x, int y) {
            return (x << y) | x >>> (32 - y);
        }

        // 单元摘要计算函数
        private void encrypt() {
            for (int i = 16; i <= 79; i++) {
                tmpData[i] = f4(tmpData[i - 3] ^ tmpData[i - 8] ^ tmpData[i - 14] ^
                        tmpData[i - 16], 1);
            }

            int[] tmpabcde = new int[5];

            for (int i1 = 0; i1 < tmpabcde.length; i1++) {
                tmpabcde[i1] = digestInt[i1];
            }

            for (int j = 0; j <= 19; j++) {
                int tmp = f4(tmpabcde[0], 5) +
                        f1(tmpabcde[1], tmpabcde[2], tmpabcde[3]) + tmpabcde[4] +
                        tmpData[j] + 0x5a827999;
                tmpabcde[4] = tmpabcde[3];
                tmpabcde[3] = tmpabcde[2];
                tmpabcde[2] = f4(tmpabcde[1], 30);
                tmpabcde[1] = tmpabcde[0];
                tmpabcde[0] = tmp;
            }

            for (int k = 20; k <= 39; k++) {
                int tmp = f4(tmpabcde[0], 5) +
                        f2(tmpabcde[1], tmpabcde[2], tmpabcde[3]) + tmpabcde[4] +
                        tmpData[k] + 0x6ed9eba1;
                tmpabcde[4] = tmpabcde[3];
                tmpabcde[3] = tmpabcde[2];
                tmpabcde[2] = f4(tmpabcde[1], 30);
                tmpabcde[1] = tmpabcde[0];
                tmpabcde[0] = tmp;
            }

            for (int l = 40; l <= 59; l++) {
                int tmp = f4(tmpabcde[0], 5) +
                        f3(tmpabcde[1], tmpabcde[2], tmpabcde[3]) + tmpabcde[4] +
                        tmpData[l] + 0x8f1bbcdc;
                tmpabcde[4] = tmpabcde[3];
                tmpabcde[3] = tmpabcde[2];
                tmpabcde[2] = f4(tmpabcde[1], 30);
                tmpabcde[1] = tmpabcde[0];
                tmpabcde[0] = tmp;
            }

            for (int m = 60; m <= 79; m++) {
                int tmp = f4(tmpabcde[0], 5) +
                        f2(tmpabcde[1], tmpabcde[2], tmpabcde[3]) + tmpabcde[4] +
                        tmpData[m] + 0xca62c1d6;
                tmpabcde[4] = tmpabcde[3];
                tmpabcde[3] = tmpabcde[2];
                tmpabcde[2] = f4(tmpabcde[1], 30);
                tmpabcde[1] = tmpabcde[0];
                tmpabcde[0] = tmp;
            }

            for (int i2 = 0; i2 < tmpabcde.length; i2++) {
                digestInt[i2] = digestInt[i2] + tmpabcde[i2];
            }

            for (int n = 0; n < tmpData.length; n++) {
                tmpData[n] = 0;
            }
        }

        // 4字节数组转换为整数
        private int byteArrayToInt(byte[] bytedata, int i) {
            return ((bytedata[i] & 0xff) << 24) | ((bytedata[i + 1] & 0xff) << 16) |
                    ((bytedata[i + 2] & 0xff) << 8) | (bytedata[i + 3] & 0xff);
        }

        // 整数转换为4字节数组
        private void intToByteArray(int intValue, byte[] byteData, int i) {
            byteData[i] = (byte) (intValue >>> 24);
            byteData[i + 1] = (byte) (intValue >>> 16);
            byteData[i + 2] = (byte) (intValue >>> 8);
            byteData[i + 3] = (byte) intValue;
        }

        // 将字节数组转换为十六进制字符串
        private static String byteArrayToHexString(byte[] bytearray) {
            StringBuilder strDigest = new StringBuilder();
            char[] Digit = {
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C',
                    'D', 'E', 'F'
            };
            char[] ob = new char[2];
            for (byte b : bytearray) {
                ob[0] = Digit[(b >>> 4) & 0X0F];
                ob[1] = Digit[b & 0X0F];
                strDigest.append(ob);
            }

            return strDigest.toString();
        }

        // 计算sha-1摘要，返回相应的字节数组
        public byte[] getDigestOfBytes(byte[] byteData) {
            process_input_bytes(byteData);

            byte[] digest = new byte[20];

            for (int i = 0; i < digestInt.length; i++) {
                intToByteArray(digestInt[i], digest, i * 4);
            }

            return digest;
        }

        // 计算sha-1摘要，返回相应的十六进制字符串
        public String getDigestOfString(byte[] byteData) {
            return byteArrayToHexString(getDigestOfBytes(byteData));
        }

        public static void main(String[] args) {
            String test = "12123awiodhiawhsidbwoib1oi3b012b3bb12b3" +
                    "12123awiodhiawhsidbwoib1oi3b012b3bb12b3" +
                    "12123awiodhiawhsidbwoib1oi3b012b3bb12b3" +
                    "12123awiodhiawhsidbwoib1oi3b012b3bb12b3" +
                    "12123awiodhiawhsidbwoib1oi3b012b3bb12b3" +
                    "12123awiodhiawhsidbwoib1oi3b012b3bb12b3" +
                    "12123awiodhiawhsidbwoib1oi3b012b3bb12b3" +
                    "12123awiodhiawhsidbwoib1oi3b012b3bb12b3" +
                    "12123awiodhiawhsidbwoib1oi3b012b3bb12b3" +
                    "12123awiodhiawhsidbwoib1oi3b012b3bb12b3" +
                    "12123awiodhiawhsidbwoib1oi3b012b3bb12b3" +
                    "v12123awiodhiawhsidbwoib1oi3b012b3bb12b3" +
                    "12123awiodhiawhsidbwoib1oi3b012b3bb12b3" +
                    "12123awiodhiawhsidbwoib1oi3b012b3bb12b3" +
                    "12123awiodhiawhsidbwoib1oi3b012b3bb12b3";

            long start1 = System.currentTimeMillis();
            for (int i = 0; i < 100; i++) {
                String str1 = new SHA1().getDigestOfString(KryoUtil.writeToByteArray(test));
            }
            System.out.println(System.currentTimeMillis() - start1);
            long start2 = System.currentTimeMillis();
            for (int i = 0; i < 100; i++) {
                String str2 = String.valueOf(getHash(KryoUtil.writeToByteArray(test)));
            }
            System.out.println(System.currentTimeMillis() - start2);
        }
    }
}
