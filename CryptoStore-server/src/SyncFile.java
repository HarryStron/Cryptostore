import java.io.Serializable;
import java.util.ArrayList;

public class SyncFile implements Serializable {
    private int version;
    private ArrayList<String> fileList;
    private ArrayList<String> hashList;

    public SyncFile() {
        version = 0;
        fileList = new ArrayList<>();
        hashList = new ArrayList<>();
    }

    public int getVersion() {
        return version;
    }

    public ArrayList<String> getFiles() {
        return fileList;
    }

    public void setVersion(int v) {
        version = v;
    }

    public void addPair(String filename, String hash) {
        if (fileList.contains(filename)) {
            //remove old
            hashList.remove(fileList.indexOf(filename));
            fileList.remove(filename);
        }

        //add new
        fileList.add(filename);
        hashList.add(hash);

        version++;
    }

    public void removepair(String filename) {
        if (fileList.contains(filename)) {
            hashList.remove(fileList.indexOf(filename));
            fileList.remove(filename);

            version++;
        }
    }

    public String getHashOfFile (String filename) {
        if (fileList.contains(filename)) {
            return hashList.get(fileList.indexOf(filename));
        } else {
            return null;
        }
    }
}
