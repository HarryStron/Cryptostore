public class Client {

    public static void main(String args[]) {
        ClientManager clientManager = new ClientManager("Admin2", "P4$$p0rd", "localhost", 5556);

        clientManager.sendFile("test.txt");
        clientManager.retrieveFile("test.txt");
    }
}