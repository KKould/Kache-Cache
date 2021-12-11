package com.kould.container;

import com.kould.utils.KryoUtil;

import java.util.*;

public class KryoList {
    private final List<byte[]> list = new ArrayList<>() ;

    public boolean add(Object o) {
        return list.add(KryoUtil.writeToByteArray(o));
    }

    public List<byte[]> getList() {
        return list;
    }
}
