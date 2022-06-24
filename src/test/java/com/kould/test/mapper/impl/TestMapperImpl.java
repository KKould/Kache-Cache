package com.kould.test.mapper.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kould.test.entity.TestEntity;

import java.util.*;

public class TestMapperImpl implements com.kould.test.mapper.TestMapper {

    public static final String METHOD_NAME_SELECT_TEST = "selectTestById";

    public static final List<TestEntity> DATA_BASE_TEST = new ArrayList<>();

    static {
        DATA_BASE_TEST.add(new TestEntity(0L,"1"));
        DATA_BASE_TEST.add(new TestEntity(1L,"2"));
        DATA_BASE_TEST.add(new TestEntity(2L,"3"));
    }

    @Override
    public TestEntity selectTestById(Long id) throws InterruptedException {
        //模拟网络延迟
        Thread.sleep(1);
        return DATA_BASE_TEST.get(Math.toIntExact(id));
    }

    @Override
    public List<TestEntity> selectTestAll() throws InterruptedException {
        //模拟网络延迟
        Thread.sleep(1);
        return DATA_BASE_TEST;
    }

    @Override
    public Set<TestEntity> selectTestSet() throws InterruptedException {
        //模拟网络延迟
        Thread.sleep(1);
        return new HashSet<>(DATA_BASE_TEST);
    }

    @Override
    public Queue<TestEntity> selectTestQueue() throws InterruptedException {
        //模拟网络延迟
        Thread.sleep(1);
        return new ArrayDeque<>(DATA_BASE_TEST);
    }

    @Override
    public Page<TestEntity> selectTestPage() throws InterruptedException {
        //模拟网络延迟
        Thread.sleep(1);
        return new Page<TestEntity>().setRecords(DATA_BASE_TEST);
    }

    @Override
    public TestEntity insertTest(TestEntity testEntity) throws InterruptedException {
        //模拟网络延迟
        Thread.sleep(1);
        DATA_BASE_TEST.add(testEntity);
        return testEntity;
    }

    @Override
    public Long deleteTest(TestEntity testEntity) throws InterruptedException {
        //模拟网络延迟
        Thread.sleep(1);
        DATA_BASE_TEST.remove(testEntity);
        return 1L;
    }

    @Override
    public TestEntity updateTest(TestEntity testEntity) throws InterruptedException {
        int index = Math.toIntExact(testEntity.getId());
        //模拟网络延迟
        Thread.sleep(1);
        DATA_BASE_TEST.remove(index);
        DATA_BASE_TEST.add(index, testEntity);
        return testEntity;
    }
}
