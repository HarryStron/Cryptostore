package com.cryptostore.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FileMap implements Serializable {
    private ArrayList<String> original;
    private ArrayList<String> encrypted;
    private Map<String, Boolean> stegan;

    public FileMap () {
        original = new ArrayList<>();
        encrypted = new ArrayList<>();
        stegan = new HashMap<>();
    }

    public void addMapping (String originalPath, String encryptedPath, boolean steg) {
        original.add(originalPath);
        encrypted.add(encryptedPath);
        stegan.put(originalPath, steg);
    }

    public void removeMapping (String originalPath, String encryptedPath) {
        original.remove(originalPath);
        encrypted.remove(encryptedPath);
        stegan.remove(originalPath);
    }

    public boolean containsOriginal (String path) {
        return original.contains(path);
    }

    public boolean containsEncrypted (String path) {
        return encrypted.contains(path);
    }

    public String getOriginalFromEncrypted (String path) {
        if (encrypted.indexOf(path) < 0) {
            return null;
        } else {
            return original.get(encrypted.indexOf(path));
        }
    }

    public String getEncryptedFromOriginal (String path) {
        if (original.indexOf(path) < 0) {
            return null;
        } else {
            return encrypted.get(original.indexOf(path));
        }
    }

    public boolean isStegOn(String path) {
        return stegan.get(path);
    }

    public int numberOfElements() {
        return original.size();
    }
}
