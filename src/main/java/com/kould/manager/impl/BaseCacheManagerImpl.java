package com.kould.manager.impl;

import com.kould.api.Kache;
import com.kould.api.KacheEntity;
import com.kould.entity.KacheMessage;
import com.kould.manager.IBaseCacheManager;
import com.kould.entity.MethodPoint;

import java.io.Serializable;

public class BaseCacheManagerImpl extends IBaseCacheManager {

    @Override
    public Object daoWrite(String key, MethodPoint point, String type) throws Throwable {
        Object result = remoteCacheManager.put(key, type, point, this.pageDetails);
        if (interprocessCacheProperties.isEnable()) {
            interprocessCacheManager.put(key, result, type) ;
        }
        return result;
    }

    @Override
    public Object daoRead(String key, String type) throws Throwable {
        if (interprocessCacheProperties.isEnable()) {
            Object result =interprocessCacheManager.get(key, type) ;
            if (result == null) {
                result = remoteCacheManager.get(key, this.pageDetails) ;
                if (result != null) {
                    interprocessCacheManager.put(key, result, type);
                }
            }
            return result;
        } else {
            return remoteCacheManager.get(key, this.pageDetails);
        }
    }

    @Override
    public void deleteCache(KacheMessage msg) throws Throwable {
        deleteCacheByKey(msg);
    }

    @Override
    public void updateCache(KacheMessage msg) throws Throwable {
        deleteCacheByKey(msg);
    }

    @Override
    public void insertCache(KacheMessage msg) throws Throwable {
        deleteCacheByKey(msg);
    }

    /**
     * 无锁删除索引缓存与元缓存方法
     *
     * 元缓存删除会导致到索引缓存收集，以此原理对元缓存优先处理使其未删除的索引缓存能够返回空而走数据库，
     * 实现幂等，防止远程缓存重复删除
     *
     * @param msg 方法摘要
     * @throws RuntimeException 删除时异常
     */
    private void deleteCacheByKey(KacheMessage msg) throws Throwable {
        String typeName = msg.getType();
        // 若远程缓存CAS失败则仅清空进程缓存
        if (!remoteCacheManager.cas(msg.getId())) {
            interprocessCacheManager.clear(typeName);
            return;
        }
        Class<? extends KacheEntity> resultClass = msg.getCacheClazz();
        Object arg = msg.getArgs()[0];
        Class<?> argClass = arg.getClass();
        if (arg instanceof Serializable) {
            remoteCacheManager.del(cacheEncoder.getId2Key(arg.toString(), typeName));
        } else if (resultClass.isAssignableFrom(argClass)){
            String idStr = resultClass.cast(arg).getPrimaryKey();
            remoteCacheManager.del(cacheEncoder.getId2Key(idStr, typeName));
        }
        interprocessCacheManager.clear(typeName);
        // INDEX为前缀表示只批量删除索引缓存
        // 表达式中加*号使类型可以匹配多类型
        remoteCacheManager.delKeys(cacheEncoder.getPattern(Kache.INDEX_TAG + "*" + resultClass.getName()));
    }
}
