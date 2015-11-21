public class Client {
    private static final String encryptionPassword = "password";

    public static void main(String args[]) {
        ClientManager clientManager1 = new ClientManager("Admin1", "P4$$w0rd", "localhost", 5555);
//        ClientManager clientManager2 = new ClientManager("Admin2", "P4$$w0rd", "localhost", 5555);
//        ClientManager clientManager3 = new ClientManager("Harry", "P4$$w0rd", "localhost", 5555);


        clientManager1.connect(encryptionPassword);
        clientManager1.getFile(encryptionPassword, "./Admin1/test1.txt");
        clientManager1.sendFile(encryptionPassword, "./Admin1/test1.txt");
        clientManager1.getFile(encryptionPassword, "./Admin1/test1.txt");
        clientManager1.deleteFile(encryptionPassword, "./Admin1/test1.txt");
        clientManager1.closeConnection();

//        clientManager2.connect(encryptionPassword);
//        clientManager2.sendFile(encryptionPassword, "./Admin2/test2.txt");
//        clientManager2.deleteFile(encryptionPassword, "./Admin2/test2.txt");
//        clientManager2.closeConnection();

//        clientManager3.connect(encryptionPassword);
//        clientManager3.sendFile(encryptionPassword, "./Admin1/test1.txt");
//        clientManager3.getFile(encryptionPassword, "./Admin1/test1.txt");
//        clientManager3.closeConnection();

    }
}