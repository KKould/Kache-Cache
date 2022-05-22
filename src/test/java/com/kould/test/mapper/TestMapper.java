package com.kould.test.mapper;

import com.kould.annotation.DaoSelect;
import com.kould.test.entity.TestEntity;
import com.kould.test.entity.TestOtherEntity;

import java.util.List;

public interface TestMapper {
    TestEntity selectTestById(Long id) throws InterruptedException;

    // 新增TestOtherEntity作为关联Bean
//    @DaoSelect(involve = {TestOtherEntity.class})
    @DaoSelect
    List<TestEntity> selectTestAll() throws InterruptedException;

    TestEntity insertTest(TestEntity testEntity) throws InterruptedException;

    Long deleteTest(TestEntity testEntity) throws InterruptedException;

    TestEntity updateTest(TestEntity testEntity) throws InterruptedException;
}
