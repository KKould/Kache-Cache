package com.kould;

import com.kould.json.GsonUtil;
import com.kould.utils.KryoUtil;

public class TestDemo {

    static class Test {
        public Test(String wqe, int qwe, long qwee, String qwed, String wqe1, int qwe1, long qwee1, String qwed1, String wqe2, int qwe2, long qwee2, String qwed2, String wqe3, int qwe3, long qwee3, String qwed3, String wqe13, int qwe13, long qwee13, String qwed13, String wqe23, int qwe23, long qwee23, String qwed23) {
            this.wqe = wqe;
            this.qwe = qwe;
            this.qwee = qwee;
            this.qwed = qwed;
            this.wqe1 = wqe1;
            this.qwe1 = qwe1;
            this.qwee1 = qwee1;
            this.qwed1 = qwed1;
            this.wqe2 = wqe2;
            this.qwe2 = qwe2;
            this.qwee2 = qwee2;
            this.qwed2 = qwed2;
            this.wqe3 = wqe3;
            this.qwe3 = qwe3;
            this.qwee3 = qwee3;
            this.qwed3 = qwed3;
            this.wqe13 = wqe13;
            this.qwe13 = qwe13;
            this.qwee13 = qwee13;
            this.qwed13 = qwed13;
            this.wqe23 = wqe23;
            this.qwe23 = qwe23;
            this.qwee23 = qwee23;
            this.qwed23 = qwed23;
        }

        private String wqe;
        private int qwe ;
        private long qwee ;
        private String qwed ;
        private String wqe1;
        private int qwe1 ;
        private long qwee1 ;
        private String qwed1 ;
        private String wqe2;
        private int qwe2 ;
        private long qwee2 ;
        private String qwed2 ;
        private String wqe3;
        private int qwe3 ;
        private long qwee3 ;
        private String qwed3 ;
        private String wqe13;
        private int qwe13 ;
        private long qwee13 ;
        private String qwed13 ;
        private String wqe23;
        private int qwe23 ;
        private long qwee23 ;
        private String qwed23 ;
    }

    public static void main(String[] args) {
        Test test = new Test("wqeqwe",123,1230L,"qioweyhoqwehoqwheoiqw","wqeqwe",123,1230L,"qioweyhoqwehoqwheoiqw","wqeqwe",123,1230L,"qioweyhoqwehoqwheoiqw","wqeqwe",123,1230L,"qioweyhoqwehoqwheoiqw","wqeqwe",123,1230L,"qioweyhoqwehoqwheoiqw","wqeqwe",123,1230L,"qioweyhoqwehoqwheoiqw");

        Object test1 = new TimeTestAgent(new TimeTest() {
            @Override
            public Object test() {
                for (int i = 0; i < 10000; i++) {
//                    byte[] bytes = KryoUtil.writeToByteArray(test);
//                    Object o = KryoUtil.readFromByteArray(bytes);
                    String s = KryoUtil.writeToString(test);
                    Object o = KryoUtil.readFromString(s);
                }
//                byte[] bytes = KryoUtil.writeToByteArray(test);
//                Object o = KryoUtil.readFromByteArray(bytes);
                String s = KryoUtil.writeToString(test);
                Object o = KryoUtil.readFromString(s);
                return o;
            }
        }).test();

        Object test2 = new TimeTestAgent(new TimeTest() {
            @Override
            public Object test() {
                GsonUtil gsonUtil = new GsonUtil();
                for (int i = 0; i < 10000; i++) {
                    String s = gsonUtil.obj2Str(test);
                    Object o = gsonUtil.str2Obj(s,Test.class);
                }
                String s = gsonUtil.obj2Str(test);
                Object o = gsonUtil.str2Obj(s,Test.class);
                return o;
            }
        }).test();
        System.out.println(test);
        System.out.println(test1);
        System.out.println(test2);
    }
}
