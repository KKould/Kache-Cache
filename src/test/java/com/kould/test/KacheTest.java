package com.kould.test;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.kould.api.Kache;
import com.kould.properties.LocalCacheProperties;
import com.kould.test.entity.TestEntity;
import com.kould.test.mapper.TestMapper;
import com.kould.test.mapper.impl.TestMapperImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class KacheTest {

    private final Kache kache = Kache.builder()
            .page(Page.class, "records", List.class)
            .load(LocalCacheProperties.class, new LocalCacheProperties(false, 0))
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
        testMapperProxy.insertTest(INSERT_ENTITY_TEST);
        collectionSort(testMapperTarget.selectTestAll(), testMapperProxy.selectTestAll());
        testMapperProxy.updateTest(UPDATE_ENTITY_TEST);
        collectionSort(testMapperTarget.selectTestAll(), testMapperProxy.selectTestAll());
        testMapperProxy.deleteTest(INSERT_ENTITY_TEST);
        collectionSort(testMapperTarget.selectTestAll(), testMapperProxy.selectTestAll());

        // 用于id为0的缓存清空
        testMapperProxy.updateTest(UPDATE_ENTITY_TEST);

        // 测试Page对象是否顺利解析
        // 测试多次读取
        // 读时序列化数据返回，内容应该一致
        Assert.assertEquals(gson.toJson(testMapperTarget.selectTestById(0L)), gson.toJson(testMapperProxy.selectTestById(0L)));
        Assert.assertEquals(gson.toJson(testMapperTarget.selectTestById(0L)), gson.toJson(testMapperProxy.selectTestById(0L)));
        Assert.assertEquals(gson.toJson(testMapperTarget.selectTestById(0L)), gson.toJson(testMapperProxy.selectTestById(0L)));


        // 测试Page对象是否顺利解析
        // 测试多次读取
        // 读时序列化数据返回，内容应该一致
        Assert.assertEquals(gson.toJson(testMapperTarget.selectTestPage()), gson.toJson(testMapperProxy.selectTestPage()));
        Assert.assertEquals(gson.toJson(testMapperTarget.selectTestPage()), gson.toJson(testMapperProxy.selectTestPage()));
        Assert.assertEquals(gson.toJson(testMapperTarget.selectTestPage()), gson.toJson(testMapperProxy.selectTestPage()));

        // 测试多次读取
        // 读时序列化数据返回，内容应该一致
        collectionSort(testMapperTarget.selectTestAll(), testMapperProxy.selectTestAll());
        collectionSort(testMapperTarget.selectTestAll(), testMapperProxy.selectTestAll());
        collectionSort(testMapperTarget.selectTestAll(), testMapperProxy.selectTestAll());

        // 测试Set对象是否顺利解析
        // 测试多次读取
        // 读时序列化数据返回，内容应该一致
        collectionSort(testMapperTarget.selectTestSet(), testMapperProxy.selectTestSet());
        collectionSort(testMapperTarget.selectTestSet(), testMapperProxy.selectTestSet());
        collectionSort(testMapperTarget.selectTestSet(), testMapperProxy.selectTestSet());


        // 测试Queue对象是否顺利解析
        // 测试多次读取
        // 读时序列化数据返回，内容应该一致
        collectionSort(testMapperTarget.selectTestQueue(), testMapperProxy.selectTestQueue());
        collectionSort(testMapperTarget.selectTestQueue(), testMapperProxy.selectTestQueue());
        collectionSort(testMapperTarget.selectTestQueue(), testMapperProxy.selectTestQueue());

        // 测试修改时返回结果是否为同一对象
        Assert.assertEquals(testMapperTarget.insertTest(INSERT_ENTITY_TEST),testMapperProxy.insertTest(INSERT_ENTITY_TEST));
        Assert.assertEquals(testMapperTarget.updateTest(UPDATE_ENTITY_TEST),testMapperProxy.updateTest(UPDATE_ENTITY_TEST));
        Assert.assertEquals(testMapperTarget.deleteTest(INSERT_ENTITY_TEST),testMapperProxy.deleteTest(INSERT_ENTITY_TEST));
        Assert.assertEquals(testMapperTarget.deleteTest(UPDATE_ENTITY_TEST),testMapperProxy.deleteTest(UPDATE_ENTITY_TEST));

        startTaskAllInOnce(5000, () -> {
            try {
                collectionSort(testMapperTarget.selectTestAll(), testMapperProxy.selectTestAll());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 测试后该框架进行资源关闭
     * @throws Exception
     */
    @After
    public void destroy() throws Throwable {
        kache.destroy();
    }

    private void collectionSort (Collection<TestEntity> collection1, Collection<TestEntity> collection2) {
        List<String> collect1 = collection1.stream().sorted(Comparator.comparing(TestEntity::getId)).map(gson::toJson).collect(Collectors.toList());
        List<String> collect2 = collection2.stream().sorted(Comparator.comparing(TestEntity::getId)).map(gson::toJson).collect(Collectors.toList());
        Assert.assertEquals(collect1, collect2);
    }

    private void startTaskAllInOnce(int threadNums, final Runnable task) throws InterruptedException {
        long start = System.currentTimeMillis();
        final CountDownLatch startGate = new CountDownLatch(1);
        final CountDownLatch endGate = new CountDownLatch(threadNums);
        for(int i = 0; i < threadNums; i++) {
            Thread t = new Thread(() -> {
                try {
                    // 使线程在此等待，当开始门打开时，一起涌入门中
                    startGate.await();
                    try {
                        task.run();
                    } finally {
                        // 将结束门减1，减到0时，就可以开启结束门了
                        endGate.countDown();
                    }
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            });
            t.start();
        }
        // 因开启门只需一个开关，所以立马就开启开始门
        startGate.countDown();
        // 等等结束门开启
        endGate.await();
        long end = System.currentTimeMillis();
        System.out.println(start - end);
    }

}
