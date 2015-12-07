package com.cryptostore.client;

import java.util.Arrays;

public abstract class Packets {
    private byte prefix;
    private byte[] load;

    public Packets(byte prefix) {
        this.prefix = prefix;
    }

    public void setData(byte[] b) {
        load = new byte[b.length + 1];

        load[0] = prefix;
        System.arraycopy(b, 0, load, 1, b.length);
    }

    public byte[] getData(int startingIndex) {
        return Arrays.copyOfRange(load, startingIndex, load.length);
    }
}
