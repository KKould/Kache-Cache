package com.kould.api;

/**
 * Kache初始化依赖注入接口
 */
public interface BeanLoad {
    /**
     * 指定组件内需要注入的属性类型
     * Kache内的组件Boot拥有其中属性的类型时，会自动注入到其中
     *
     * @return 需要注入的组件类型
     */
    Class<?>[] loadArgs();
}
