package com.kould.bean;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@Component
@RefreshScope
public class KacheConfig {
    @Value("${kache.dao.lock-time:2}")
    private int lockTime ;

    @Value("${kache.service.keyword.all:All}")
    private String methodAllKeyword ;

    @Value("${kache.service.keyword.like:Like}")
    private String methodLikeKeyword ;

    //只需要为唯一的标识，能够判断含有该标签唯一即可
    @Value("${kache.service.no-arg-tag:River}")
    private String methodNoArgTag ;

    @Value("${kache.dao.base-time:300}")
    private int baseTime ;

    @Value("${kache.dao.random-time:120}")
    private int randomTime ;

    @Value("${kache.interprocess-cache.enable:true}")
    private boolean enableInterprocessCache ;

    @Value("${kache.interprocess-cache.size:50}")
    private int interprocessCacheSize ;

    public int getCacheTime() {
        return (int)(baseTime + Math.random() * randomTime) ;
    }

    public static final String POINTCUT_EXPRESSION_DAO_FIND = "execution(* *.*.mapper..*.select*(..))";

    public static final String POINTCUT_EXPRESSION_DAO_ADD = "execution(* *.*.mapper..*.insert*(..))";

    public static final String POINTCUT_EXPRESSION_DAO_REMOVE = "execution(* *.*.mapper..*.delete*(..))";

    public static final String POINTCUT_EXPRESSION_DAO_EDIT = "execution(* *.*.mapper..*.update*(..))";

    public static final String POINTCUT_EXPRESSION_SERVICE = "execution(* *.*.service..*.*(..))";

}
