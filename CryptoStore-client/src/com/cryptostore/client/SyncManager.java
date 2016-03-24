package com.cryptostore.client;

import java.io.*;
import java.lang.*;

public class SyncManager {
    public String SYNC_PATH;

    public SyncManager(String username) {
        SYNC_PATH = username+File.separator+"SYNC_INFO";
    }

    //boolean set to true when adding a new fle or editing and to false when deleting
    public boolean updateEntry(String encFilename, byte[] encFileBytes, boolean addFile) {
        try {
            SyncFile syncFile = getSyncFile();

            if (addFile) {
                syncFile.addPair(encFilename, HashGenerator.getSHA256(encFileBytes));
            } else {
                syncFile.removepair(encFilename);
            }

            return store(syncFile);
        } catch (Exception e) {
            return false;
        }
    }

    public int getVersion() {
        try {
            return getSyncFile().getVersion();
        } catch (Exception e) {
            return -1;
        }
    }

    public boolean setVersion(int v) {
        try {
            SyncFile syncFile = getSyncFile();
            syncFile.setVersion(v);

            return store(syncFile);
        } catch (Exception e) {
            return false;
        }
    }

    public void createFileIfNotExists() throws Exception {
        File syncFile = new File(SYNC_PATH);
        if (!syncFile.exists()) {
            try {
                syncFile.getParentFile().mkdirs();
                syncFile.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream(syncFile);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(new SyncFile());
                objectOutputStream.close();

                System.out.println("New synchronisation file created!");
            } catch (Exception e) {
                throw new Exception(Error.CANNOT_SAVE_FILE.getDescription());
            }
        }
    }

    public SyncFile getSyncFile() throws Exception {
        try {
            File file = new File(SYNC_PATH);
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            SyncFile syncFile = (SyncFile) ois.readObject();
            ois.close();

            return syncFile;
        } catch (Exception e) {
            createFileIfNotExists();
            return new SyncFile();
        }
    }

    private boolean store(SyncFile syncFile) {
        try {
            File file = new File(SYNC_PATH);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(syncFile);
            objectOutputStream.close();

            return true;

        } catch (Exception e) {
            return false;
        }
    }
}
