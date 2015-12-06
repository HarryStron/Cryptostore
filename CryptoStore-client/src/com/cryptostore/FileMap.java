package com.cryptostore;

import java.io.Serializable;
import java.util.ArrayList;

public class FileMap implements Serializable {
    private ArrayList<String> original;
    private ArrayList<String> encrypted;

    public FileMap () {
        original = new ArrayList<>();
        encrypted = new ArrayList<>();
    }

    public void addMapping (String originalPath, String encryptedPath) {
        original.add(originalPath);
        encrypted.add(encryptedPath);
    }

    public void removeMapping (String originalPath, String encryptedPath) {
        original.remove(originalPath);
        encrypted.remove(encryptedPath);
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

    public int numberOfElements() {
        return original.size();
    }
}
