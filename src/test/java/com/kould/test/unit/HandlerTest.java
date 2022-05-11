package com.kould.test.unit;

import com.kould.entity.Status;
import com.kould.core.CacheHandler;
import com.kould.core.impl.BaseCacheHandler;
import com.kould.proxy.MethodPoint;
import com.kould.test.mapper.TestMapper;
import com.kould.test.mapper.impl.TestMapperImpl;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;


public class HandlerTest {

    private final CacheHandler cacheHandler = new BaseCacheHandler();

    private final TestMapper testMapper = new TestMapperImpl();

    private static final String TEST_ARGS_1 = "“民族主义”在基本性质上已经彻底沦为反动派维护其统治的意识形态工具";

    private static final String TEST_KEY_1 = "KACHE:$NI:com.kould.test.TestType141496750210321511";

    private static final String TEST_TYPE_1 = "com.kould.test.TestType";

    private static final int THREAD_NUM = 3000;

    private final Method selectTest = testMapper.getClass()
            .getDeclaredMethod(TestMapperImpl.METHOD_NAME_SELECT_TEST, Long.class);

    private final MethodPoint methodPoint = new MethodPoint(testMapper, new Object[]{TEST_ARGS_1},selectTest);

    //线程不安全变量，用于模拟数据库修改
    public int testCount = 0;

    public HandlerTest() throws NoSuchMethodException {
    }

    @Test
    public void loadWriteTest() throws Exception {

        HandlerTest handlerTest = new HandlerTest();
        // 测试并发写时是否为同步操作
        handlerTest.startTaskAllInOnce(THREAD_NUM, () -> {
            try {
                cacheHandler.load(methodPoint, false
                        , (key, types) -> null
                        , (key, point, types) -> {
                            handlerTest.testCount ++;
                            return testMapper.selectTestById(0L);
                        }
                        , (point, methodName, types, methodStatus) -> TEST_KEY_1
                        , TEST_TYPE_1, Status.BY_FIELD);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // 判断相同类型参数方法并发是否同步
        Assert.assertEquals(THREAD_NUM, handlerTest.testCount);
    }

    @Test
    public void loadReadTest() throws Exception {

        HandlerTest handlerTest = new HandlerTest();
        // 测试并发读时是否为非同步操作
        handlerTest.startTaskAllInOnce(THREAD_NUM, () -> {
            try {
                cacheHandler.load(methodPoint, false
                        , (key, types) -> {
                            handlerTest.testCount ++;
                            return testMapper.selectTestById(0L);
                        }
                        , (key, point, types) -> testMapper.selectTestById(0L)
                        , (point, methodName, types, methodStatus) -> TEST_KEY_1
                        , TEST_TYPE_1, Status.BY_FIELD);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // 判断并发读不应该同步
        Assert.assertFalse(THREAD_NUM == handlerTest.testCount);
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
