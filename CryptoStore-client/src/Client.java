public class Client {

    public static void main(String args[]) {
        ClientManager clientManager = new ClientManager("localhost", 5556);

        clientManager.sendFile("test.txt");
        clientManager.retrieveFile("test.txt");
    }
}