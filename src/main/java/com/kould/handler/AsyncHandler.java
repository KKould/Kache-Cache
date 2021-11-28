package com.kould.handler;

import com.kould.message.KacheMessage;

public abstract class AsyncHandler extends StrategyHandler {

    public abstract void listen2DeleteRemote(KacheMessage msg) throws Exception ;
    public abstract void listen2DeleteInterprocess(KacheMessage msg) throws Exception;
    public abstract void listen2UpdateRemote(KacheMessage msg) throws Exception;
    public abstract void listen2UpdateInterprocess(KacheMessage msg) throws Exception;
    public abstract void listen2InsertRemote(KacheMessage msg) throws Exception;
    public abstract void listen2InsertInterprocess(KacheMessage msg) throws Exception;
}
