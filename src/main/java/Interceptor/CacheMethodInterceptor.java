package Interceptor;

import com.kould.annotation.*;
import com.kould.config.ListenerProperties;
import com.kould.core.CacheHandler;
import com.kould.encoder.CacheEncoder;
import com.kould.enity.RegexEntity;
import com.kould.handler.StrategyHandler;
import com.kould.manager.IBaseCacheManager;
import com.kould.enity.KacheMessage;
import com.kould.proxy.MethodPoint;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public final class CacheMethodInterceptor implements MethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(CacheMethodInterceptor.class) ;

    private final Object target;

    private final Class<?> entityClass;

    private final IBaseCacheManager baseCacheManager;

    private final StrategyHandler strategyHandler;

    private final ListenerProperties listenerProperties;

    private final CacheHandler cacheHandler;

    private final CacheEncoder cacheEncoder;

    private final RegexEntity regexEntity;

    public CacheMethodInterceptor(Object target, Class<?> entityClass, IBaseCacheManager baseCacheManager, StrategyHandler strategyHandler,
                                  ListenerProperties listenerProperties, CacheHandler cacheHandler, CacheEncoder cacheEncoder, RegexEntity regexEntity) {
        this.target = target;
        this.entityClass = entityClass;
        this.baseCacheManager = baseCacheManager;
        this.strategyHandler = strategyHandler;
        this.listenerProperties = listenerProperties;
        this.cacheHandler = cacheHandler;
        this.cacheEncoder = cacheEncoder;
        this.regexEntity = regexEntity;
    }


    /**
     *
     * @param target 表示要进行增强的对象
     * @param method 表示拦截的方法
     * @param args 数组表示参数列表，基本数据类型需要传入其包装类型，如int-->Integer、long-Long、double-->Double
     * @param methodProxy 表示对方法的代理，invokeSuper方法表示对被代理对象方法的调用
     * @return 执行结果
     * @throws Throwable 异常
     */
    @Override
    public Object intercept(Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        Class<?> mapperEntityClass = this.entityClass;
        //此处使用的Target为该类实例化时注入的target，以实现二次加工（比如MyBatis生成的实例再次由Kache实例化）
        MethodPoint methodPoint = new MethodPoint(methodProxy, this.target, args, method);
        String methodName = method.getName();
        if (method.isAnnotationPresent(DaoSelect.class) || Pattern.matches(regexEntity.getSelectRegex(),methodName)) {
            return cacheHandler.load(methodPoint, listenerProperties.isEnable(), baseCacheManager::daoRead
                    , baseCacheManager::daoWrite, cacheEncoder::getDaoKey, mapperEntityClass.getTypeName()
                    , method.getAnnotation(DaoSelect.class).status());
        }
        if (method.isAnnotationPresent(DaoInsert.class) || Pattern.matches(regexEntity.getInsertRegex(),methodName)) {
            return strategyHandler.insert(methodPoint
                    ,getKacheMessage(method, mapperEntityClass, args));
        }
        if (method.isAnnotationPresent(DaoDelete.class) || Pattern.matches(regexEntity.getDeleteRegex(),methodName)) {
            return strategyHandler.delete(methodPoint
                    ,getKacheMessage(method, mapperEntityClass, args));
        }
        if (method.isAnnotationPresent(DaoUpdate.class) || Pattern.matches(regexEntity.getUpdateRegex(),methodName)) {
            return strategyHandler.update(methodPoint
                    ,getKacheMessage(method, mapperEntityClass, args));
        }
        return methodPoint.execute();
    }

    private KacheMessage getKacheMessage(Method method, Class<?> beanClass, Object[] args) {
        String daoMethodName = method.getName() ;
        return KacheMessage.builder()
                .arg(args)
                .cacheClazz(beanClass)
                .methodName(daoMethodName)
                .build();
    }

}
