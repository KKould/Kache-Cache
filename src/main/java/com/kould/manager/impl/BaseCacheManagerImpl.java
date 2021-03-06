package com.kould.manager.impl;

import com.kould.api.Kache;
import com.kould.api.KacheEntity;
import com.kould.entity.KacheMessage;
import com.kould.entity.Status;
import com.kould.manager.IBaseCacheManager;
import com.kould.entity.MethodPoint;

public class BaseCacheManagerImpl extends IBaseCacheManager {

    @Override
    public Object daoWrite(String key, MethodPoint point, String type) throws Exception {
        Object result = remoteCacheManager.put(key, type, point, this.pageDetails);
        if (localCacheProperties.isEnable()) {
            localCacheManager.put(key, result, type) ;
        }
        return result;
    }

    @Override
    public Object daoRead(String key, String type) throws Exception {
        if (localCacheProperties.isEnable()) {
            Object result = localCacheManager.get(key, type) ;
            if (result == null) {
                result = remoteCacheManager.get(key, this.pageDetails) ;
                if (result != null) {
                    localCacheManager.put(key, result, type);
                }
            }
            return result;
        } else {
            return remoteCacheManager.get(key, this.pageDetails);
        }
    }

    @Override
    public void deleteCache(KacheMessage msg) throws Exception {
        kacheLock.syncFunction(msg.getType(), value -> {
            deleteCacheByKey(value);
            return true;
        }, msg);
    }

    @Override
    public void updateCache(KacheMessage msg) throws Exception {
        kacheLock.syncFunction(msg.getType(), value -> {
            deleteCacheByKey(value);
            return true;
        }, msg);
    }

    @Override
    public void insertCache(KacheMessage msg) throws Exception {
        kacheLock.syncFunction(msg.getType(), value -> {
            deleteCacheByKey(value);
            return true;
        }, msg);
    }

    /**
     * 无锁删除索引缓存与元缓存方法
     *
     * 元缓存删除会导致到索引缓存收集，以此原理对元缓存优先处理使其未删除的索引缓存能够返回空而走数据库，
     * 实现幂等，防止远程缓存重复删除
     *
     * @param msg 方法摘要
     * @throws Exception 删除时异常
     */
    private void deleteCacheByKey(KacheMessage msg) throws Exception {
        String typeName = msg.getType();
        // 若远程缓存CAS失败则仅清空进程缓存
        if (!remoteCacheManager.cas(msg.getId())) {
            localCacheManager.clear(typeName);
            return;
        }
        Class<? extends KacheEntity> resultClass = msg.getCacheClazz();
        Object args = msg.getArgs()[0];
        Class<?> argClass = args.getClass();
        if (Status.BY_ID.equals(msg.getStatus())) {
            remoteCacheManager.del(cacheEncoder.getId2Key(args.toString(), typeName));
        } else if (resultClass.isAssignableFrom(argClass)) {
            String idStr = resultClass.cast(args).getPrimaryKey();
            remoteCacheManager.del(cacheEncoder.getId2Key(idStr, typeName));
        }
        // INDEX为前缀表示只批量删除索引缓存
        // 表达式中加*号使类型可以匹配多类型
        remoteCacheManager.delKeys(cacheEncoder.getPattern(Kache.INDEX_TAG + "*" + resultClass.getName()));
        localCacheManager.clear(typeName);
    }
}
