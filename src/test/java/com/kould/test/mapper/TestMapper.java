package com.kould.test.mapper;

import com.kould.test.entity.TestEntity;

import java.util.ArrayList;
import java.util.List;

public class TestMapper {

    private static final String TEST_MESSAGE = "Hello World";

    public static final String METHOD_NAME_SELECT_TEST = "selectTest";

    public List<TestEntity> selectTest(String args) {
        List<TestEntity> testEntities = new ArrayList<>();
        testEntities.add(new TestEntity(TEST_MESSAGE));
        return testEntities;
    }
}
