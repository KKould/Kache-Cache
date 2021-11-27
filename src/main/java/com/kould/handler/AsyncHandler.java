package com.kould.handler;

import com.kould.message.KacheMessage;

public interface AsyncHandler {

    void listen2DeleteRemote(KacheMessage msg) throws Exception ;
    void listen2DeleteInterprocess(KacheMessage msg) throws Exception;
    void listen2UpdateRemote(KacheMessage msg) throws Exception;
    void listen2UpdateInterprocess(KacheMessage msg) throws Exception;
    void listen2InsertRemote(KacheMessage msg) throws Exception;
    void listen2InsertInterprocess(KacheMessage msg) throws Exception;
}
