public class Client {
    private static final String password = "password";

    public static void main(String args[]) {
        ClientManager clientManager = new ClientManager("Admin1", "P4$$w0rd", "localhost", 5556);


        clientManager.sendFile(password, "./testDir/test.txt");
        clientManager.getFile(password, "./testDir/test.txt");
    }
}