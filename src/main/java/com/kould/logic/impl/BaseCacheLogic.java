package com.kould.logic.impl;

import com.kould.config.KacheAutoConfig;
import com.kould.lock.KacheLock;
import com.kould.logic.CacheLogic;
import com.kould.manager.InterprocessCacheManager;
import com.kould.message.KacheMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;

//默认数据插入逻辑与数据删除逻辑一致
public class BaseCacheLogic extends CacheLogic {

    private static final Logger log = LoggerFactory.getLogger(BaseCacheLogic.class) ;

    private static final String METHOD_GET_ID = "getId" ;

    private static final BaseCacheLogic INSTANCE = new BaseCacheLogic() ;

    private BaseCacheLogic() {

    }

    public static BaseCacheLogic getInstance() {
        return INSTANCE ;
    }

    public void deleteRemoteCache(KacheMessage msg) throws Throwable {
        deleteCacheByKey(msg);
    }

    public void deleteInterprocessCache(KacheMessage msg) throws ExecutionException {
        interprocessCacheClear(msg,kacheLock,interprocessCacheManager);
    }

    public void updateRemoteCache(KacheMessage msg) throws Throwable {
        Class<?> resultClass = msg.getCacheClazz();
        String type = resultClass.getTypeName();
        Object arg = msg.getArg()[0];
        Class<?> argClass = arg.getClass();
        Lock writeLock = null;
        try {
            writeLock = kacheLock.writeLock(type);
            //==========上为重复代码
            //无法进行抽取的原因是因为lambda表达式无法抛出异常
            if (resultClass.isAssignableFrom(argClass)){
                Method methodGetId = argClass.getMethod(METHOD_GET_ID, null);
                log.info("\r\nKache:+++++++++Redis缓存更新缓存....");
                remoteCacheManager.updateById(methodGetId.invoke(arg).toString(), type, arg) ;
            }
            //==========下为重复代码
            remoteCacheManager.delKeys(cacheEncoder.getPattern(resultClass.getName()));
            kacheLock.unLock(writeLock);
        } catch (Exception e){
            if (kacheLock.isLockedByThisThread(writeLock)) {
                kacheLock.unLock(writeLock);
            }
            e.printStackTrace();
            throw e ;
        }
    }

    public void updateInterprocessCache(KacheMessage msg) throws ExecutionException {
        interprocessCacheClear(msg,kacheLock,interprocessCacheManager);
    }

    @Override
    public void insertRemoteCache(KacheMessage msg) throws Throwable {
        deleteCacheByKey(msg);
    }

    @Override
    public void insertInterprocessCache(KacheMessage msg) throws Exception {
        interprocessCacheClear(msg,kacheLock,interprocessCacheManager);
    }

    private void deleteCacheByKey(KacheMessage msg) throws Throwable {
        Class<?> resultClass = msg.getCacheClazz();
        String lockKey = msg.getCacheClazz().getTypeName();
        Object arg = msg.getArg()[0];
        Lock writeLock = null;
        try {
            writeLock = kacheLock.writeLock(lockKey);
            //==========上为重复代码
            //无法进行抽取的原因是因为lambda表达式无法抛出异常
            log.info("\r\nKache:+++++++++Redis缓存删除检测....");
            if (arg instanceof String) {
                remoteCacheManager.del((String) arg);
            } else if (resultClass.isAssignableFrom(arg.getClass())){
                Method methodGetId = arg.getClass().getMethod(METHOD_GET_ID, null);
                remoteCacheManager.del(methodGetId.invoke(arg).toString());
            }
            remoteCacheManager.delKeys(cacheEncoder.getPattern(resultClass.getName()));
            kacheLock.unLock(writeLock);
        } catch (Exception e){
            if (kacheLock.isLockedByThisThread(writeLock)) {
                kacheLock.unLock(writeLock);
            }
            e.printStackTrace();
            throw e ;
        }
    }

    private void interprocessCacheClear(KacheMessage msg, KacheLock kacheLock, InterprocessCacheManager interprocessCacheManager) throws ExecutionException {
        String lockKey = msg.getCacheClazz().getTypeName();
        Lock writeLock = null ;
        try {
            writeLock = kacheLock.writeLock(lockKey);
            interprocessCacheManager.clear(lockKey); ;
            kacheLock.unLock(writeLock);
        } catch (Exception e) {
            if (kacheLock.isLockedByThisThread(writeLock)) {
                kacheLock.unLock(writeLock);
            }
            e.printStackTrace();
            throw e ;
        }
    }
}
