package com.kould.entity;

public final class NullValue {

    private NullValue() {

    }

    private static final NullValue NULL_VALUE = new NullValue();

    public static NullValue getInstance() {
        return NULL_VALUE;
    }

    private static final long serialVersionUID = -4470362330115322213L;

//    private final String MESSAGE = "I never told anyone,but I've always thought they are lighthouses.\n" +
//            "\n" +
//            "Billions of lighthouses...stuck at the far end of the sky.";
}
