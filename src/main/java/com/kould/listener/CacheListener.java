package com.kould.listener;

import com.kould.enity.KacheMessage;

import java.lang.reflect.InvocationTargetException;

//允许多例
public abstract class CacheListener {

    /*
    该注册方法默认通过无参方法进行实例化
    必须使用ListenerHandler的register方法进行注册
    若子类中使用了单例模式等，需要覆写该方法进行监听器注册
     */
    protected void register() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        ListenerHandler.register(this.getClass().getDeclaredConstructor().newInstance());
    }

    /*
    ！！！！！！！！！！
    该三个方法非线程安全（为保证即使的动作处理速度）
     */
    public abstract void hit(String key, KacheMessage msg) ;
    public abstract void notHit(String key, KacheMessage msg) ;
    public abstract Object details() ;

}
