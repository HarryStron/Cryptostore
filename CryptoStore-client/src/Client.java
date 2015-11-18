public class Client {
    private static final String encryptionPassword = "password";

    public static void main(String args[]) {
        ClientManager clientManager = new ClientManager("Admin1", "P4$$w0rd", "localhost", 5556);

        clientManager.sendFile(encryptionPassword, "./Admin1/test1.txt");
        clientManager.getFile(encryptionPassword, "./Admin1/test1.txt");

        ClientManager clientManager2 = new ClientManager("Admin2", "P4$$w0rd", "localhost", 5556);

//        clientManager2.sendFile(encryptionPassword, "./Admin2/test1.txt");
        clientManager2.getFile(encryptionPassword, "./Admin2/test1.txt");
    }
}