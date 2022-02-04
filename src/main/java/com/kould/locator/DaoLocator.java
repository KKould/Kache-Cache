package com.kould.locator;

import cn.hutool.core.util.StrUtil;
import com.kould.annotation.CacheClass;
import com.kould.aspect.DaoCacheAop;
import com.kould.config.DaoProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

public class DaoLocator implements ApplicationContextAware {

    @Autowired
    private DaoProperties daoProperties;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        String mapperPackage = daoProperties.getMapperPackage();
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(CacheClass.class);
        for (Map.Entry<String, Object> stringObjectEntry : beansWithAnnotation.entrySet()) {
            try {
                Class<?> aClass = Class.forName(
                        mapperPackage + "." + StrUtil.upperFirst(stringObjectEntry.getKey()));
                CacheClass annotation = aClass.getAnnotation(CacheClass.class);
                Class<?> clazz = annotation.value();
                DaoCacheAop.CLASS_MAP.put(stringObjectEntry.getValue().toString(), clazz);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
