import java.io.File;
import java.io.IOException;

public class Client {
    private static final String encryptionPassword = "password";

    public static void main(String args[]) throws IOException {
        ClientManager clientManager1 = new ClientManager("Admin1", "P4$$w0rd", "localhost", 5555);
//        ClientManager clientManager2 = new ClientManager("Admin2", "P4$$w0rd", "localhost", 5555);
//        ClientManager clientManager3 = new ClientManager("Harry", "P4$$w0rd", "localhost", 5555);

//        ClientManager.getAllUserFiles((new File("./Admin1/")).toPath()).forEach(System.out::println);
        clientManager1.connect(encryptionPassword);
//        clientManager1.download(encryptionPassword, "./Admin1/test1.txt");
//        clientManager1.upload(encryptionPassword, "./Admin1/test1.txt");
//        clientManager1.download(encryptionPassword, "./Admin1/test1.txt");
//        clientManager1.deleteFile(encryptionPassword, "./Admin1/test1.txt");
        clientManager1.closeConnection();

//        clientManager2.connect(encryptionPassword);
//        clientManager2.upload(encryptionPassword, "./Admin2/test2.txt");
//        clientManager2.deleteFile(encryptionPassword, "./Admin2/test2.txt");
//        clientManager2.closeConnection();

//        clientManager3.connect(encryptionPassword);
//        clientManager3.upload(encryptionPassword, "./Admin1/test1.txt");
//        clientManager3.download(encryptionPassword, "./Admin1/test1.txt");
//        clientManager3.closeConnection();

    }
}