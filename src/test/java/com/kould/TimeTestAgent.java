package com.kould;

public class TimeTestAgent implements TimeTest{

    TimeTest target ;

    public TimeTestAgent(TimeTest target) {
        this.target = target;
    }

    @Override
    public Object test() {
        Long start = System.currentTimeMillis() ;
        Object test = target.test();
        Long end = System.currentTimeMillis() ;
        System.out.println("测试所用时间为：" + (end - start));
        return test ;
    }


}
