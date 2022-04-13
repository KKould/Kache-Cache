package com.kould.utils;

import java.util.Arrays;

public class SHA1 {

    //准备工作

    public static final int[] abcde = {
            0x67452301,
            0xEFCDAB89,
            0x98BADCFE,
            0x10325476,
            0xC3D2E1F0
    };

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

    //4字节数组转换成int  i个byte合成byteData[] 中
    public static int byteArrayToInt(byte[] byteData, int i) {
        return ((byteData[i] & 0xff) << 24) | ((byteData[i + 1] & 0xff) << 16) | ((byteData[i + 2] & 0xff) << 8) | (byteData[i + 3] & 0xff);
    }

    //整数转成4字节数组，int 分解到byte数组中
    public static void inToByteArray(int intValue, byte[] byteData, int i) {
        byteData[i] = (byte) ((intValue >>> 24) & 0xff);
        byteData[i + 1] = (byte) ((intValue >>> 16) & 0xff);
        byteData[i + 2] = (byte) ((intValue >>> 8) & 0xff);
        byteData[i + 3] = (byte) (intValue & 0xff);
    }

    /**
     *
     */
    public static int f1(int x, int y, int z) {
        return (x & y) | (~x & z);
    }

    public static int f2(int x, int y, int z) {
        return x ^ y ^ z;
    }

    public static int f3(int x, int y, int z) {
        return (x & y) | (x & z) | (y & z);
    }

    public static int f4(int x, int y, int z) {
        return x ^ y ^ z;
    }

    //开始逻辑
    //进行对原数据的补位
    public static byte[] byteArrayFormatData(byte[] byteData) {
        //补0的个数
        int fill = 0;
        //补位后的总位数，64的倍数
        int size = 0;
        //原数据的长度
        int srcLength = byteData.length;
        //对64求余      n%512      56数据     8长度
        int m = srcLength % 64;
        if (m < 56) {
            fill = 55 - m;
            size = srcLength - m + 64;//数据只有一块

        } else if (m == 56) {
            fill = 63;
            size = srcLength + 8 + 64;
        } else {
            fill = 63 - m + 56; // 58    60+56    116-64=52     55-52=3
            size = (srcLength + 64) - m + 64;
        }
        //补位后生成的新的数组的内容
        byte[] newnyte = new byte[size];
        System.arraycopy(byteData, 0, newnyte, 0, srcLength);

        //补1
        int startLocation = srcLength;
        newnyte[startLocation++] = (byte) 0x80;
        //补0
        for (int i = 0; i < fill; i++) {
            newnyte[startLocation++] = (byte) 0x00;
        }
        //处理长度的位置   字节 *8=？位 512-468=64位    ，用来存放长度
        long n = (long) srcLength * 8;
        byte h8 = (byte) (n & 0xff);
        byte h7 = (byte) ((n >> 8) & 0xff);
        byte h6 = (byte) ((n >> 16) & 0xff);
        byte h5 = (byte) ((n >> 24) & 0xff);
        byte h4 = (byte) ((n >> 32) & 0xff);
        byte h3 = (byte) ((n >> 40) & 0xff);
        byte h2 = (byte) ((n >> 48) & 0xff);
        byte h1 = (byte) (n >> 56);
        newnyte[startLocation++] = h1;
        newnyte[startLocation++] = h2;
        newnyte[startLocation++] = h3;
        newnyte[startLocation++] = h4;
        newnyte[startLocation++] = h5;
        newnyte[startLocation++] = h6;
        newnyte[startLocation++] = h7;
        newnyte[startLocation++] = h8;


        return newnyte;
    }

    //开始计算密文   算摘要
    public static void process_input_bytes(byte[] byteData, int[] h) {
        //计算过程中需要用到的临时数据存储数组
        int[] m = new int[80];
        System.arraycopy(abcde, 0, h, 0, abcde.length);
        //格式化数据
        byte[] newByte = byteArrayFormatData(byteData);
        //计算有多少个大块
        int mCount = newByte.length / 64;
        //循环计算每一块的内容
        for (int pos = 0; pos < mCount; pos++) {
            //对每一块进行加密计算
            //（1）将 Mi 分成 16 个字节 W0，W1。。。。w15 ,  W0 是最右边的字
            for (int i = 0; i < 16; i++) {
                m[i] = byteArrayToInt(newByte, (pos * 6) + (i * 4));
            }
            //计算
            encrpy(h, m);

        }
    }

    private static void encrpy(int[] h, int[] m) {
        // (2)  c\对于 t= 16 到79 令 Wt = S1(Wt-3 XOR Wt-8 XOR Wt-14 XOR-16)
        for (int t = 16; t <= 79; t++) {
            m[t] = s(m[t - 3] ^ m[t - 8] ^ m[t - 14] ^ m[t - 16], 1);
        }
        //(3) 令 A = H0, b = H1 , c = H2 , D = H3 , E = H4
        int[] tempabcde = new int[5];
        System.arraycopy(h, 0, tempabcde, 0, tempabcde.length);

        // 4 对于 t = 0 到 79 ，执行下面的循环
        // ....
        for (int i = 0; i <= 19; i++) {
            int temp = s(tempabcde[0], 5)
                    + f1(tempabcde[1], tempabcde[2], tempabcde[3])
                    + tempabcde[4]
                    + m[i] + 0x5A827999;

            tempabcde[4] = tempabcde[3];
            tempabcde[3] = tempabcde[2];
            tempabcde[2] = s(tempabcde[1], 30);
            tempabcde[1] = tempabcde[0];
            tempabcde[0] = temp;

        }
        for (int i = 20; i <= 39; i++) {
            int temp = s(tempabcde[0], 5)
                    + f2(tempabcde[1], tempabcde[2], tempabcde[3])
                    + tempabcde[4]
                    + m[i] + 0x6ED9EBA1;

            tempabcde[4] = tempabcde[3];
            tempabcde[3] = tempabcde[2];
            tempabcde[2] = s(tempabcde[1], 30);
            tempabcde[1] = tempabcde[0];
            tempabcde[0] = temp;

        }


        for (int i = 40; i <= 59; i++) {
            int temp = s(tempabcde[0], 5)
                    + f3(tempabcde[1], tempabcde[2], tempabcde[3])
                    + tempabcde[4]
                    + m[i] + 0x8F1BBCDC;

            tempabcde[4] = tempabcde[3];
            tempabcde[3] = tempabcde[2];
            tempabcde[2] = s(tempabcde[1], 30);
            tempabcde[1] = tempabcde[0];
            tempabcde[0] = temp;

        }

        for (int i = 60; i <= 79; i++) {
            int temp = s(tempabcde[0], 5)
                    + f4(tempabcde[1], tempabcde[2], tempabcde[3])
                    + tempabcde[4]
                    + m[i] + 0xCA62C1D6;

            tempabcde[4] = tempabcde[3];
            tempabcde[3] = tempabcde[2];
            tempabcde[2] = s(tempabcde[1], 30);
            tempabcde[1] = tempabcde[0];
            tempabcde[0] = temp;

        }

        for (int i = 0; i < tempabcde.length; i++) {
            h[i] = h[i] + tempabcde[i];

        }
        //完成了一次操作
        //消除之前的操作，开始下一块的计算
        Arrays.fill(m, 0);

    }

    //n是一个整数且 0<=n<=32  Sn(X) = (X<<n)OR(X>>32-n)
    public static int s(int x, int i) {
        return (x << i) | (x >>> (32 - i));
    }

    //把byte数组转16进制显示出来
    public static byte[] getDigetOfBytes(byte[] byteData) {
        //摘要数据存储用的数组 （存放密文的） 20个字节 * 8 = 160
        int[] h = new int[5];
        process_input_bytes(byteData, h);
        byte[] digest = new byte[20];
        for (int i = 0; i < h.length; i++) {
            inToByteArray(h[i], digest, i * 4);
        }
        return digest;
    }

    public static String getDigestOfString(byte[] byteData) {
        return byteArrayToHexString(getDigetOfBytes(byteData));
    }
}
