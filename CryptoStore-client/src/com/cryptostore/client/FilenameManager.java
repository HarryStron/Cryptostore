package com.cryptostore.client;

import java.io.*;
import java.lang.*;
import java.util.UUID;

public class FilenameManager {
    public String MAP_PATH;
    public final String HEX_MAP_PATH = "0000000000";

    public FilenameManager(String username) {
        MAP_PATH = username+File.separator+"ENCRYPTION_MAPPING";
    }

    /** Returns the newly generated encryption for the path given or the previously generated encryption if path already exists **/
    public String randomisePath(String path) throws IOException, ClassNotFoundException {
        path = path.replace('\\', '/');
        String encryptedPath = generateRandomName();

        FileMap map = getMap();
        if (map.containsEncrypted(encryptedPath)) {
            return randomisePath(path);

        } else {
            if (!map.containsOriginal(path)) {
                return encryptedPath;
            } else {
                return map.getEncryptedFromOriginal(path);
            }
        }
    }

    /** Returns null if file does not exist **/
    public String getEncryptedPath(String path) {
        path = path.replace('\\', '/');
        return getMap().getEncryptedFromOriginal(path);
    }

    /** Returns null if file does not exist **/
    public String getOriginalPath(String path) {
        String out = getMap().getOriginalFromEncrypted(path);
        if (out == null) {
            return out;
        } else {
            return out.replace('/', File.separatorChar);
        }
    }

    public boolean isStegOn(String path) {
        return getMap().isStegOn(path.replace('\\', '/'));
    }

    private FileMap getMap() {
        try {
            return (FileMap) StorageManager.getFile(MAP_PATH);
        } catch (IOException e) {
            return new FileMap();
        } catch (ClassNotFoundException e) {
            return new FileMap();
        }
    }

    public boolean containsOriginal(String filename) {
        FileMap map = getMap();

        return map.containsOriginal(filename.replace('\\', '/'));
    }

    public boolean addToMap(String filename, String encryptedFilename, boolean steg) {
        FileMap map = getMap();
        map.addMapping(filename.replace('\\', '/'), encryptedFilename, steg);

        return StorageManager.store(MAP_PATH, map);
    }

    public boolean removeFromMap(String filename) {
        FileMap map = getMap();
        map.removeMapping(filename.replace('\\', '/'), map.getEncryptedFromOriginal(filename.replace('\\', '/')));

        return StorageManager.store(MAP_PATH, map);
    }

    /** returns true if it exists false otherwise **/
    public boolean createMapIfNotExists () throws Exception {
        File mapFile = new File(MAP_PATH);
        if (!mapFile.exists()) {
            try {
                FileMap fileMap = new FileMap();
                fileMap.addMapping(MAP_PATH.replace('\\', '/'), HEX_MAP_PATH, false);
                StorageManager.createDirAndStore(MAP_PATH, fileMap);

                System.out.println("New encryption-mapping created!");
                return false;
            } catch (Exception e) {
                throw new Exception(Error.CANNOT_SAVE_FILE.getDescription());
            }
        }
        return true;
    }

    /** Max value is 10 and Min value is 3 ELSE defaults to 5 **/
    private String generateRandomName() {
        String randName = UUID.randomUUID().toString();
        randName = randName.replace("-", "").substring(0, 10);

        return randName;
    }

    public int numberOfFiles() { //for testing
        return getMap().numberOfElements();
    }
}
