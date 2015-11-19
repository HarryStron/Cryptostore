import java.io.*;
import java.util.HashMap;
import java.util.UUID;

public class FilenameManager {
    public String HASHMAP_PATH;

    public FilenameManager(String username) {
        HASHMAP_PATH = "./"+username+"/ENCRYPTION_MAPPING";
    }

    /** Returns the newly generated encryption for the path given or the previously generated encryption
     * if path already exists **/
    public String randomisePath(String path) throws IOException, ClassNotFoundException {
        String encryptedPath = "./"+generateRandomName();

        HashMap<String, String> hashMap = getHashMap();
        if (hashMap.containsValue(encryptedPath)) {
            return randomisePath(path);

        } else {
            if (!hashMap.containsKey(path)) {
                return encryptedPath;
            } else {
                return hashMap.get(path);
            }
        }
    }

    /** Returns null if file does not exist **/
    public String pathLookup(String path) {
        try {
            return getHashMap().get(path);
        } catch (IOException e) {
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private HashMap<String, String> getHashMap() throws IOException, ClassNotFoundException {
        File file = new File(HASHMAP_PATH);
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        HashMap<String, String> map = (HashMap<String, String>) ois.readObject();
        ois.close();

        return map;
    }

    public boolean storeToFile(String filename, String encryptedFilename) {
        try {
            HashMap<String, String> hashMap = getHashMap();
            hashMap.put(filename, encryptedFilename);

            File file = new File(HASHMAP_PATH);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(hashMap);
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
}
