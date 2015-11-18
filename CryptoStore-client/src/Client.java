public class Client {
    private static final String encryptionPassword = "password";

    public static void main(String args[]) {
        ClientManager clientManager = new ClientManager("Admin1", "P4$$w0rd", "localhost", 5556);

        clientManager.sendFile(encryptionPassword, "./testDir/test1.txt");
        clientManager.getFile(encryptionPassword, "./testDir/test1.txt");
    }
}