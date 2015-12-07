package com.cryptostore.server;

public class FileData extends Data {
    public FileData(byte[] file) {
        super('F', file);
    }
}
