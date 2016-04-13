package com.cryptostore.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class StorageManager {

    public static boolean store(String path, Object fileToWrite) {
        try {
            File file = new File(path);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(fileToWrite);
            objectOutputStream.close();

            return true;

        } catch (Exception e) {
            return false;
        }
    }
}
