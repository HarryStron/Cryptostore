package com.cryptostore.client;

public class FileData extends Data {
    public FileData(byte[] file) {
        super('F', file);
    }
}
