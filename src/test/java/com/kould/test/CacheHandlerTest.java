package com.kould.test;

import com.kould.config.Status;
import com.kould.core.CacheHandler;
import com.kould.core.impl.BaseCacheHandler;
import com.kould.proxy.MethodPoint;
import com.kould.test.mapper.TestMapper;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;


public class CacheHandlerTest {

    private final CacheHandler cacheHandler = new BaseCacheHandler();

    private final TestMapper testMapper = new TestMapper();

    private static final String TEST_ARGS_1 = "1111";

    private static final String TEST_KEY_1 = "key1";

    private static final String TEST_TYPE_1 = "type1";

    private static final int THREAD_NUM = 1000;

    private final Method selectTest = testMapper.getClass()
            .getDeclaredMethod(TestMapper.METHOD_NAME_SELECT_TEST, String.class);

    private final MethodPoint methodPoint = new MethodPoint(testMapper, new Object[]{TEST_ARGS_1},selectTest);

    //线程不安全变量，用于模拟数据库修改
    public int testCount = 0;

    public CacheHandlerTest() throws NoSuchMethodException {
    }

    @Test
    public void loadWriteTest() throws Exception {

        CacheHandlerTest cacheHandlerTest = new CacheHandlerTest();
        // 测试并发写时是否为同步操作
        cacheHandlerTest.startTaskAllInOnce(THREAD_NUM, () -> {
            try {
                cacheHandler.load(methodPoint, false
                        , (key, types) -> null
                        , (key, point, types) -> {
                            cacheHandlerTest.testCount ++;
                            return testMapper.selectTest(TEST_ARGS_1);
                        }
                        , (point, methodName, types, methodStatus) -> TEST_KEY_1
                        , TEST_TYPE_1, Status.BY_FIELD);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // 判断相同类型参数方法并发是否同步
        Assert.assertEquals(THREAD_NUM, cacheHandlerTest.testCount);
    }

    @Test
    public void loadReadTest() throws Exception {

        CacheHandlerTest cacheHandlerTest = new CacheHandlerTest();
        // 测试并发读时是否为同步操作
        cacheHandlerTest.startTaskAllInOnce(THREAD_NUM, () -> {
            try {
                cacheHandler.load(methodPoint, false
                        , (key, types) -> {
                            cacheHandlerTest.testCount ++;
                            return testMapper.selectTest(TEST_ARGS_1);
                        }
                        , (key, point, types) -> testMapper.selectTest(TEST_ARGS_1)
                        , (point, methodName, types, methodStatus) -> TEST_KEY_1
                        , TEST_TYPE_1, Status.BY_FIELD);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // 判断并发读不应该同步
        Assert.assertFalse(THREAD_NUM == cacheHandlerTest.testCount);
    }

    public void startTaskAllInOnce(int threadNums, final Runnable task) throws InterruptedException {
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
    }
}
