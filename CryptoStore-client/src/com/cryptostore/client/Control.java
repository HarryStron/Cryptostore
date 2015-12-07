package com.cryptostore.client;

public class Control extends Packets {

    public Control(byte[] controlBytes) {
        super((byte) 'C');
        setData(controlBytes);
    }
}
