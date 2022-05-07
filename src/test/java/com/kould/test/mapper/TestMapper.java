package com.kould.test.mapper;

import com.kould.test.entity.TestEntity;

import java.util.List;

public interface TestMapper {
    TestEntity selectTestById(Long id) throws InterruptedException;

    List<TestEntity> selectTestAll() throws InterruptedException;

    TestEntity insertTest(TestEntity testEntity) throws InterruptedException;

    Long deleteTest(TestEntity testEntity) throws InterruptedException;

    TestEntity updateTest(TestEntity testEntity) throws InterruptedException;
}
