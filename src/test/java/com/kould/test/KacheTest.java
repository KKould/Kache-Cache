package com.kould.test;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.kould.api.Kache;
import com.kould.entity.PageDetails;
import com.kould.test.entity.TestEntity;
import com.kould.test.mapper.TestMapper;
import com.kould.test.mapper.impl.TestMapperImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class KacheTest {

    private final Kache kache = Kache.builder()
            .load(PageDetails.class, new PageDetails<>(Page.class, "records", List.class))
            .build();

    private final Gson gson = new Gson();

    private final TestMapper testMapperTarget = new TestMapperImpl();

    private TestMapper testMapperProxy;

    public static final TestEntity UPDATE_ENTITY_TEST = new TestEntity(0L,"$1");

    public static final TestEntity INSERT_ENTITY_TEST = new TestEntity(3L,"$4");

    public KacheTest() throws NoSuchFieldException, IllegalAccessException {
    }

    /**
     * 测试前对该框架进行初始化
     * @throws Exception
     */
    @Before
    public void init() throws Throwable {
        kache.init();
        testMapperProxy = kache.getProxy(testMapperTarget, TestEntity.class);
    }

    /**
     * 镜像测试
     * 通过使用原本的Mapper与加强后的Mapper进行CRUD的对比测试，对比结果是否一致
     * @throws InterruptedException
     */
    @Test
    public void mirrorTest() throws InterruptedException {

        testMapperProxy.selectTestAll();

        // 测试每次缓存变动时数据是否保持一致
        testMapperTarget.insertTest(INSERT_ENTITY_TEST);
        testMapperProxy.insertTest(INSERT_ENTITY_TEST);
        Assert.assertEquals(gson.toJson(testMapperTarget.selectTestAll()), gson.toJson(testMapperProxy.selectTestAll()));
        testMapperTarget.updateTest(UPDATE_ENTITY_TEST);
        testMapperProxy.updateTest(UPDATE_ENTITY_TEST);
        Assert.assertEquals(gson.toJson(testMapperTarget.selectTestAll()), gson.toJson(testMapperProxy.selectTestAll()));
        testMapperTarget.deleteTest(INSERT_ENTITY_TEST);
        testMapperProxy.deleteTest(INSERT_ENTITY_TEST);
        Assert.assertEquals(gson.toJson(testMapperTarget.selectTestAll()), gson.toJson(testMapperProxy.selectTestAll()));

        // 测试多次读取
        // 读时序列化数据返回，内容应该一致
        Assert.assertEquals(gson.toJson(testMapperTarget.selectTestAll()), gson.toJson(testMapperProxy.selectTestAll()));
        Assert.assertEquals(gson.toJson(testMapperTarget.selectTestAll()), gson.toJson(testMapperProxy.selectTestAll()));
        Assert.assertEquals(gson.toJson(testMapperTarget.selectTestAll()), gson.toJson(testMapperProxy.selectTestAll()));

        // 测试修改时返回结果是否为同一对象
        Assert.assertEquals(testMapperTarget.insertTest(INSERT_ENTITY_TEST),testMapperProxy.insertTest(INSERT_ENTITY_TEST));
        Assert.assertEquals(testMapperTarget.updateTest(UPDATE_ENTITY_TEST),testMapperProxy.updateTest(UPDATE_ENTITY_TEST));
        Assert.assertEquals(testMapperTarget.deleteTest(INSERT_ENTITY_TEST),testMapperProxy.deleteTest(INSERT_ENTITY_TEST));
    }

    /**
     * 测试后该框架进行资源关闭
     * @throws Exception
     */
    @After
    public void destroy() throws Throwable {
        kache.destroy();
    }

}
