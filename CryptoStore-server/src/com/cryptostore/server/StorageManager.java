package com.cryptostore.server;

import java.io.*;

public class StorageManager {

    public static boolean createDirAndStore(String path, Object fileToWrite) throws IOException {
        File syncFile = new File(path);
        syncFile.getParentFile().mkdirs();
        syncFile.createNewFile();

        return store(path, fileToWrite);
    }

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

    public static void createDirAndStore(String filename, byte[] buffer) throws IOException {
        File file = new File(filename);
        file.getParentFile().mkdirs();
        store(filename, buffer);
    }

    public static void store(String filename, byte[] buffer) throws IOException {
        FileOutputStream fos = new FileOutputStream(new File(filename));
        fos.write(buffer, 0, buffer.length);
        fos.close();
    }

    public static boolean delete(String filename) {
        return new File(filename).delete();
    }

    public static Object getFile(String path) throws IOException, ClassNotFoundException {
        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object out = ois.readObject();
        ois.close();

        return out;
    }
}
