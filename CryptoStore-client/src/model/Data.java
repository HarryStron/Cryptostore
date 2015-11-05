package model;

public abstract class Data extends Packets {

    public Data(char type, byte[] dataBytes) {
        super((byte) type);
        setData(dataBytes);
    }
}

