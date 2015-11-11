import java.io.*;
import java.util.HashMap;
import java.util.UUID;

public class FilenameManager {
    private static final String HASHMAP_PATH = "./hashMap";

    public static String randomiseAndStore(String path) throws IOException, ClassNotFoundException {
        String[] components = decomposePath(path);
        String[] encryptedComponent = new String[components.length];

        for (int i=0; i<components.length-1; i++) {
            encryptedComponent[i] = generateRandomName(5);
        }
        encryptedComponent[encryptedComponent.length-1] = generateRandomName(5)+'.'+generateRandomName(3);

        String newPath = ".";
        for (String s : encryptedComponent) {
            newPath = newPath.concat('/'+s);
        }

        HashMap<String, String> hashMap = getHashMap();
        if (hashMap.containsValue(newPath)) {
            return randomiseAndStore(path);

        } else {
            if (!hashMap.containsKey(path)) {
                hashMap.put(path, newPath);

                if (storeToFile(hashMap)) {
                    return newPath;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    /** Returns null if not exists **/
    public static String fileLookup(String path) {
        try {
            return getHashMap().get(path);
        } catch (IOException e) {
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static HashMap<String, String> getHashMap() throws IOException, ClassNotFoundException {
        try{
            File file = new File(HASHMAP_PATH);
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            HashMap<String, String> map = (HashMap<String, String>) ois.readObject();
            ois.close();

            return map;
        } catch (FileNotFoundException e) {
            new File(HASHMAP_PATH).createNewFile();

            return new HashMap<>(); //can safely return empty map as lookup will behave like the mapping was not found
        }
    }

    private static boolean storeToFile(HashMap<String, String> hashMap) {
        try {
            File file = new File(HASHMAP_PATH);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(hashMap);
            objectOutputStream.close();

            return true;

        } catch (IOException e) {
            return false;
        }
    }

    private static String[] decomposePath(String path) {
        path = path.replace("./", " ");
        path = path.replace("/", " ");

        String[] pathComponents  = path.trim().split(" ");

        generateRandomName(5);

        return pathComponents;
    }

    /** Max value is 10 and Min value is 3 ELSE defaults to 5 **/
    private static String generateRandomName(int length) {
        if (length>10 || length<3) {
            length = 5;
        }

        String randName = UUID.randomUUID().toString();
        randName = randName.replace("-", "").substring(0, length);

        return randName;
    }
}
