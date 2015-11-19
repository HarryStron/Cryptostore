import java.io.*;
import java.util.UUID;

public class FilenameManager {
    public String MAP_PATH;

    public FilenameManager(String username) {
        MAP_PATH = "./"+username+"/ENCRYPTION_MAPPING";
    }

    /** Returns the newly generated encryption for the path given or the previously generated encryption
     * if path already exists **/
    public String randomisePath(String path) throws IOException, ClassNotFoundException {
        String encryptedPath = "./"+generateRandomName();

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

    private FileMap getMap() {
        try {
            File file = new File(MAP_PATH);
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            FileMap map = (FileMap) ois.readObject();
            ois.close();

            return map;
        } catch (IOException e) {
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public boolean storeToFile(String filename, String encryptedFilename) {
        try {
            FileMap map = getMap();
            map.addMapping(filename, encryptedFilename);

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
}
