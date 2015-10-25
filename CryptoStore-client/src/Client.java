import java.io.FileOutputStream;
import java.nio.file.Paths;

public class Client {

    public static void main(String args[]) {
        ClientManager clientManager = new ClientManager("Admin1", "P4$$w0rd", "localhost", 5556);

//        clientManager.sendFile("test.txt");
//        clientManager.retrieveFile("test.txt");

        try {
            byte[] filebytes = Encryption.encryptFile("password".toCharArray(), Paths.get("test.txt"));
            FileOutputStream fos = new FileOutputStream("testEncrypted.txt");
            fos.write(filebytes);
            fos.close();

            byte[] filebytes2 = Encryption.decryptFile("password".toCharArray(), Paths.get("testEncrypted.txt"));
            FileOutputStream fos2 = new FileOutputStream("testDecrypted.txt");
            fos2.write(filebytes2);
            fos2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}