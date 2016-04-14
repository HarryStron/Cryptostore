package com.cryptostore.server;

import java.io.*;
import java.lang.*;

public class SyncManager {
    public String SYNC_PATH;

    public SyncManager(String username) {
        SYNC_PATH = "UserFiles/"+username+"/SYNC_INFO";
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

            return StorageManager.store(SYNC_PATH, syncFile);
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

            return StorageManager.store(SYNC_PATH, syncFile);
        } catch (Exception e) {
            return false;
        }
    }

    public void createFileIfNotExists() throws Exception {
        File syncFile = new File(SYNC_PATH);
        if (!syncFile.exists()) {
            try {
                StorageManager.createDirAndStore(SYNC_PATH, new SyncFile());

                System.out.println("New synchronisation file created!");
            } catch (Exception e) {
                throw new Exception(com.cryptostore.server.Error.CANNOT_SAVE_FILE.getDescription());
            }
        }
    }

    public SyncFile getSyncFile() throws Exception {
        try {
            return (SyncFile) StorageManager.getFile(SYNC_PATH);
        } catch (Exception e) {
            createFileIfNotExists();
            return new SyncFile();
        }
    }
}
