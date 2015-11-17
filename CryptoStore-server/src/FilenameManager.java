import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class FilenameManager {
    public static final String HASHMAP_PATH = "./MAPPED_ENCRYPTION";

    public static boolean makeHashmapFile(File file) {
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(new HashMap<>());
            objectOutputStream.close();

            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
