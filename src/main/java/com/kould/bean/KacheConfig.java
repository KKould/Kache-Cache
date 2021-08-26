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

    public static final String SERVICE_LIKE = "KACHE_SERVICE_LIKE" ;

    public static final String SERVICE_IS = "KACHE_SERVICE_IS" ;

    public static final String SERVICE_NOARG = "KACHE_SERVICE_NOARG" ;

    public static final String SERVICE_ALL = "KACHE_SERVICE_ALL" ;

    public enum Status {
        LIKE(KacheConfig.SERVICE_LIKE), IS(KacheConfig.SERVICE_IS), ALL(KacheConfig.SERVICE_ALL), NOARG(KacheConfig.SERVICE_NOARG);


        private final String value ;

        Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
