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

    public void deleteRemoteCache(KacheMessage msg) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        deleteCacheByKey(msg);
    }

    public void deleteInterprocessCache(KacheMessage msg) throws ExecutionException {
        interprocessCacheClear(msg,kacheLock,interprocessCacheManager);
    }

    public void updateRemoteCache(KacheMessage msg) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?> resultClass = msg.getCacheClazz();
        String lockKey = resultClass.getSimpleName();
        Lock writeLock = null;
        try {
            List<String> allKey = remoteCacheManager.keys(cacheEncoder.getPattern(resultClass.getName()));
            List<String> delKeys = new ArrayList<>();
            allKey.parallelStream().forEach(key -> {
                if (key.contains(KacheAutoConfig.SERVICE_BY_FIELD)) {
                    delKeys.add(key);
                }
            });
            writeLock = kacheLock.writeLock(lockKey);
            Method methodGetId = msg.getArg().getClass().getMethod(METHOD_GET_ID, null);
            log.info("\r\nKache:+++++++++Redis缓存更新缓存....");
            remoteCacheManager.updateById(methodGetId.invoke(msg.getArg()).toString(),msg.getArg()) ;
            if (delKeys.size() > 0) {
                remoteCacheManager.del(delKeys.toArray(new String[delKeys.size()]));
            }
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
    public void insertRemoteCache(KacheMessage msg) throws Exception {
        deleteCacheByKey(msg);
    }

    @Override
    public void insertInterprocessCache(KacheMessage msg) throws Exception {
        interprocessCacheClear(msg,kacheLock,interprocessCacheManager);
    }

    private void deleteCacheByKey(KacheMessage msg) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?> resultClass = msg.getCacheClazz();
        String lockKey = resultClass.getSimpleName();
        Lock writeLock = null;
        try {
            log.info("\r\nKache:+++++++++Redis缓存删除检测....");
            List<String> allKey = remoteCacheManager.keys(cacheEncoder.getPattern(resultClass.getName()));
            List<String> delKeys = new ArrayList<>();
            allKey.parallelStream().forEach(key -> {
                if (key.contains(KacheAutoConfig.SERVICE_BY_FIELD)) {
                    delKeys.add(key);
                }
            });
            writeLock = kacheLock.writeLock(lockKey);
            if (msg.getArg() instanceof String) {
                delKeys.add(msg.getArg().toString()) ;
            } else {
                Method methodGetId = msg.getArg().getClass().getMethod(METHOD_GET_ID, null);
                delKeys.add(methodGetId.invoke(msg.getArg()).toString()) ;
            }
            if (delKeys.size() > 0) {
                remoteCacheManager.del(delKeys.toArray(new String[delKeys.size()]));
            }
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
        String lockKey = msg.getCacheClazz().getSimpleName();
        Lock writeLock = null ;
        try {
            writeLock = kacheLock.writeLock(lockKey);
            interprocessCacheManager.clear(msg.getCacheClazz()); ;
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
