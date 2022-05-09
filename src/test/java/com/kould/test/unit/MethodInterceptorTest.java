package com.kould.test.unit;

import com.kould.api.Kache;
import com.kould.config.ListenerProperties;
import com.kould.core.CacheHandler;
import com.kould.encoder.CacheEncoder;
import com.kould.entity.RegexEntity;
import com.kould.handler.StrategyHandler;
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

    private static final TestEntity MOCK_SELECT_VALUE = new TestEntity(1L,"1");

    private static final TestEntity MOCK_INSERT_VALUE = new TestEntity(2L,"2");

    private static final Long MOCK_DELETE_VALUE = 3L;

    private static final TestEntity MOCK_UPDATE_VALUE = new TestEntity(4L,"4");

    private final TestMapper testMapper = new TestMapperImpl();

    private final TestEntity testEntity = new TestEntity();

    private final CacheHandler handlerMock = Mockito.mock(CacheHandler.class);

    private final IBaseCacheManager managerMock = Mockito.mock(IBaseCacheManager.class);

    private final CacheEncoder encoderMock = Mockito.mock(CacheEncoder.class);

    private final StrategyHandler strategyHandlerMock = Mockito.mock(StrategyHandler.class);

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
        Mockito.when(strategyHandlerMock.insert(Mockito.any(), Mockito.any()))
                .thenReturn(MOCK_INSERT_VALUE);
        Mockito.when(strategyHandlerMock.delete(Mockito.any(),Mockito.any()))
                .thenReturn(MOCK_DELETE_VALUE);
        Mockito.when(strategyHandlerMock.update(Mockito.any(),Mockito.any()))
                .thenReturn(MOCK_UPDATE_VALUE);
    }

    //正则表达式方法名匹配测试
    @Test
    public void regexTest() throws Exception {
        CacheMethodInterceptor methodInterceptor = new CacheMethodInterceptor(testMapper, TestEntity.class
                ,managerMock,strategyHandlerMock,new ListenerProperties(),handlerMock,encoderMock
                , new RegexEntity(Kache.DEFAULT_SELECT_REGEX,Kache.DEFAULT_INSERT_REGEX,Kache.DEFAULT_DELETE_REGEX
                , Kache.DEFAULT_UPDATE_REGEX,Kache.DEFAULT_SELECT_BY_ID_REGEX));
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
