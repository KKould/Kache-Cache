package com.kould.locator;

import cn.hutool.core.util.StrUtil;
import com.kould.annotation.DaoClass;
import com.kould.aspect.DaoCacheAop;
import com.kould.config.DaoProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * MyBatis的持久层通过动态代理进行了Dao的实例生成，导致实例中可能无法获取到注解从而无法获取对应的实体类Class类型
 * 该组件用于在初始化时像CLASS_MAP中注入对应的Dao实例与Class类型作为映射去得到对应的Class类型。
 */
public class DaoLocator implements ApplicationContextAware {

    @Autowired
    private DaoProperties daoProperties;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        String mapperPackage = daoProperties.getMapperPackage();
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(DaoClass.class);
        for (Map.Entry<String, Object> stringObjectEntry : beansWithAnnotation.entrySet()) {
            try {
                Class<?> aClass = Class.forName(
                        mapperPackage + "." + StrUtil.upperFirst(stringObjectEntry.getKey()));
                DaoClass annotation = aClass.getAnnotation(DaoClass.class);
                Class<?> clazz = annotation.value();
                DaoCacheAop.CLASS_MAP.put(stringObjectEntry.getValue().toString(), clazz);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
