package com.kould.test;

import com.google.gson.Gson;
import com.kould.api.Kache;
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
            .build();

    private final Gson gson = new Gson();

    private final TestMapper testMapperTarget = new TestMapperImpl();

    private final TestMapper testMapperProxy = kache.getProxy(testMapperTarget, TestEntity.class);

    public static final TestEntity UPDATE_ENTITY_TEST = new TestEntity(0L,"$1");

    public static final TestEntity INSERT_ENTITY_TEST = new TestEntity(3L,"$4");

    /**
     * 测试前对该框架进行初始化
     * @throws Exception
     */
    @Before
    public void init() throws Exception {
        kache.init();
    }

    /**
     * 镜像测试
     * 通过使用原本的Mapper与加强后的Mapper进行CRUD的对比测试，对比结果是否一致
     * @throws InterruptedException
     */
    @Test
    public void mirrorTest() throws InterruptedException {

        List<TestEntity> extractedReal = crud(testMapperTarget);
        List<TestEntity> extractedProxy = crud(testMapperProxy);

        //写时数据返回，对象应该一致
        Assert.assertSame(extractedReal, extractedProxy);
        //提供用于debug打点查看信息
        String s1 = gson.toJson(testMapperTarget.selectTestAll());
        String s2 = gson.toJson(testMapperProxy.selectTestAll());

        //读时序列化数据返回，内容应该一致
        Assert.assertEquals(gson.toJson(testMapperTarget.selectTestAll()), gson.toJson(testMapperProxy.selectTestAll()));
        Assert.assertEquals(gson.toJson(testMapperTarget.selectTestAll()), gson.toJson(testMapperProxy.selectTestAll()));
        Assert.assertEquals(gson.toJson(testMapperTarget.selectTestAll()), gson.toJson(testMapperProxy.selectTestAll()));
    }

    /**
     * 测试后该框架进行资源关闭
     * @throws Exception
     */
    @After
    public void destroy() throws Exception {
        kache.destroy();
    }


    private List<TestEntity> crud(TestMapper testMapper) throws InterruptedException {
        testMapper.insertTest(INSERT_ENTITY_TEST);
        testMapper.deleteTest(INSERT_ENTITY_TEST);
        testMapper.updateTest(UPDATE_ENTITY_TEST);
        return testMapper.selectTestAll();
    }
}
