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

    /** Returns the newly generated encryption for the path given or the previously generated encryption
     * if path already exists **/
    public String randomisePath(String path) throws IOException, ClassNotFoundException {
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
        return getMap().getEncryptedFromOriginal(path);
    }

    /** Returns null if file does not exist **/
    public String getOriginalPath(String path) {
        return getMap().getOriginalFromEncrypted(path);
    }

    public boolean isStegOn(String path) {
        return getMap().isStegOn(path);
    }

    private FileMap getMap() {
        try {
            File file = new File(MAP_PATH);
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            FileMap map = (FileMap) ois.readObject();
            ois.close();

            return map;
        } catch (IOException e) {
            return new FileMap();
        } catch (ClassNotFoundException e) {
            return new FileMap();
        }
    }

    public boolean containsOriginal(String filename) {
        FileMap map = getMap();

        return map.containsOriginal(filename);
    }

    public boolean addToMap(String filename, String encryptedFilename, boolean steg) {
        FileMap map = getMap();
        map.addMapping(filename, encryptedFilename, steg);

        return store(map);
    }

    public boolean removeFromMap(String filename) {
        FileMap map = getMap();
        map.removeMapping(filename, map.getEncryptedFromOriginal(filename));

        return store(map);
    }

    /** returns true if it exists false otherwise **/
    public boolean createMapIfNotExists () throws Exception {
        File mapFile = new File(MAP_PATH);
        if (!mapFile.exists()) {
            try {
                mapFile.getParentFile().mkdirs();
                mapFile.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream(mapFile);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                FileMap fileMap = new FileMap();
                fileMap.addMapping(MAP_PATH, HEX_MAP_PATH, false);
                objectOutputStream.writeObject(fileMap);
                objectOutputStream.close();

                System.out.println("New encryption-mapping created!");
                return false;
            } catch (Exception e) {
                throw new Exception(Error.CANNOT_SAVE_FILE.getDescription());
            }
        }
        return true;
    }

    private boolean store(FileMap map) {
        try {
            File file = new File(MAP_PATH);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(map);
            objectOutputStream.close();

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /** Max value is 10 and Min value is 3 ELSE defaults to 5 **/
    private String generateRandomName() {
        String randName = UUID.randomUUID().toString();
        randName = randName.replace("-", "").substring(0, 10);

        return randName;
    }

    public int numberOfFiles() {
        return getMap().numberOfElements();
    }
}
