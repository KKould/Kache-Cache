package com.kould.test.mapper;

import com.kould.annotation.DaoMethod;
import com.kould.entity.Status;
import com.kould.entity.Type;
import com.kould.test.entity.TestEntity;
import com.kould.test.entity.TestOtherEntity;

import java.util.List;

public interface TestMapper {
    @DaoMethod(value = Type.SELECT,status = Status.BY_ID)
    TestEntity selectTestById(Long id) throws InterruptedException;

    // 新增TestOtherEntity作为关联Bean
    @DaoMethod(value = Type.SELECT, involve = TestOtherEntity.class, status = Status.BY_FIELD)
    List<TestEntity> selectTestAll() throws InterruptedException;

    @DaoMethod(Type.INSERT)
    TestEntity insertTest(TestEntity testEntity) throws InterruptedException;

    @DaoMethod(Type.DELETE)
    Long deleteTest(TestEntity testEntity) throws InterruptedException;

    @DaoMethod(Type.UPDATE)
    TestEntity updateTest(TestEntity testEntity) throws InterruptedException;
}
