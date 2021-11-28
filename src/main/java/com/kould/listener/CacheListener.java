package com.kould.listener;

import com.kould.message.KacheMessage;

import java.lang.reflect.InvocationTargetException;

public abstract class CacheListener {
    //构造时注册
    {
        try {
            this.register();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    该注册方法默认通过无参方法进行实例化
    必须使用ListenerHandler的register方法进行注册
    若子类中使用了单例模式等，需要覆写该方法进行监听器注册
     */
    private void register() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        CacheListener cacheListener = this.getClass().getDeclaredConstructor().newInstance();
        ListenerHandler.register(cacheListener);
    }

    /*
    ！！！！！！！！！！
    该三个方法非线程安全（为保证即使的动作处理速度）
     */
    public abstract void hit(String key, KacheMessage msg) ;
    public abstract void notHit(String key,KacheMessage msg) ;
    public abstract Object details() ;

}
