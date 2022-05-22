package com.kould.test.mapper;

import com.kould.test.entity.TestOtherEntity;

public interface TestOtherMapper {

    TestOtherEntity insertTestOther(TestOtherEntity TestOtherEntity) throws InterruptedException;

    Long deleteTestOther(TestOtherEntity TestOtherEntity) throws InterruptedException;

    TestOtherEntity updateTestOther(TestOtherEntity TestOtherEntity) throws InterruptedException;
}
