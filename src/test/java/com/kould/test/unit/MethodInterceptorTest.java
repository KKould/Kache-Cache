package com.kould.test.unit;

import com.kould.properties.KeyProperties;
import com.kould.properties.ListenerProperties;
import com.kould.core.CacheHandler;
import com.kould.encoder.CacheEncoder;
import com.kould.entity.KeyEntity;
import com.kould.strategy.Strategy;
import com.kould.interceptor.CacheMethodInterceptor;
import com.kould.manager.IBaseCacheManager;
import com.kould.test.entity.TestEntity;
import com.kould.test.mapper.TestMapper;
import com.kould.test.mapper.impl.TestMapperImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Proxy;

public class MethodInterceptorTest {

    private static final TestEntity MOCK_SELECT_VALUE = new TestEntity(10L,"1");

    private static final TestEntity MOCK_INSERT_VALUE = new TestEntity(20L,"2");

    private static final Long MOCK_DELETE_VALUE = 30L;

    private static final TestEntity MOCK_UPDATE_VALUE = new TestEntity(40L,"4");

    private final TestMapper testMapper = new TestMapperImpl();

    private final TestEntity testEntity = new TestEntity();

    private final CacheHandler handlerMock = Mockito.mock(CacheHandler.class);

    private final IBaseCacheManager managerMock = Mockito.mock(IBaseCacheManager.class);

    private final CacheEncoder encoderMock = Mockito.mock(CacheEncoder.class);

    private final Strategy strategyMock = Mockito.mock(Strategy.class);

    public static final String DEFAULT_SELECT_KEY = "select";

    public static final String DEFAULT_INSERT_KEY = "insert";

    public static final String DEFAULT_DELETE_KEY = "delete";

    public static final String DEFAULT_UPDATE_KEY = "update";

    public static final String DEFAULT_SELECT_BY_ID_KEY = "selectById";

    @Before
    public void init() throws Exception {
        Mockito.when(handlerMock.load(Mockito.any(),Mockito.anyBoolean(),Mockito.any(),Mockito.any()
                        ,Mockito.any(),Mockito.anyString(),Mockito.any()))
                .thenReturn(MOCK_SELECT_VALUE);
        Mockito.when(managerMock.daoRead(Mockito.anyString(),Mockito.anyString()))
                .thenReturn(null);
        Mockito.when(managerMock.daoWrite(Mockito.anyString(),Mockito.any(),Mockito.anyString()))
                .thenReturn(null);
        Mockito.when(encoderMock.getDaoKey(Mockito.any(),Mockito.anyString(),Mockito.anyString(),Mockito.any()))
                .thenReturn(null);
        Mockito.when(strategyMock.insert(Mockito.any(), Mockito.any()))
                .thenReturn(MOCK_INSERT_VALUE);
        Mockito.when(strategyMock.delete(Mockito.any(),Mockito.any()))
                .thenReturn(MOCK_DELETE_VALUE);
        Mockito.when(strategyMock.update(Mockito.any(),Mockito.any()))
                .thenReturn(MOCK_UPDATE_VALUE);
    }

    //正则表达式方法名匹配测试
    @Test
    public void regexTest() throws Exception {
        KeyProperties keyProperties = new KeyProperties(DEFAULT_SELECT_KEY,DEFAULT_INSERT_KEY,DEFAULT_DELETE_KEY
                , DEFAULT_UPDATE_KEY,DEFAULT_SELECT_BY_ID_KEY, true);
        CacheMethodInterceptor methodInterceptor = new CacheMethodInterceptor(testMapper, TestEntity.class
                ,managerMock, strategyMock,new ListenerProperties(),handlerMock,encoderMock
                , new KeyEntity(keyProperties));
        TestMapper testMapperProxy = (TestMapper) Proxy.newProxyInstance(testMapper.getClass().getClassLoader(), testMapper.getClass().getInterfaces(), methodInterceptor);
        TestEntity testEntitySelect = testMapperProxy.selectTestById(1L);
        TestEntity testEntityInsert = testMapperProxy.insertTest(this.testEntity);
        Long longDelete = testMapperProxy.deleteTest(this.testEntity);
        TestEntity testEntityUpdate = testMapperProxy.updateTest(this.testEntity);

        Assert.assertSame(MOCK_SELECT_VALUE, testEntitySelect);
        Assert.assertSame(MOCK_INSERT_VALUE, testEntityInsert);
        Assert.assertSame(MOCK_DELETE_VALUE, longDelete);
        Assert.assertSame(MOCK_UPDATE_VALUE, testEntityUpdate);
    }
}
