package com.cryptostore.server;

public abstract class Data extends Packets {

    public Data(char type, byte[] dataBytes) {
        super((byte) type);
        setData(dataBytes);
    }
}

